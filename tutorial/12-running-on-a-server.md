# 12 — Running on a Server

Written for someone who has only ever pressed Run in an IDE. By the end you should
understand what actually happens when a service is "deployed", and be able to put this
application on a Linux box and reach it from the internet.

---

## What is actually running?

When you press Run, this happens:

1. The OS starts a **process** — an isolated unit with its own memory, running the `java`
   binary.
2. The JVM loads your classes and calls `main`.
3. Spring builds the application context and starts **embedded Tomcat**.
4. Tomcat asks the OS to **bind** TCP port 8080 and **listen**.
5. The OS now delivers any connection arriving on port 8080 to your process.

That is the entire mystery of "running a server". **There is no separate server program.**
In the old days you installed Tomcat and dropped a `.war` into it; Spring Boot embeds
Tomcat as a library inside your jar, so your application *is* the server. One process. One
jar. That is why the deployable artifact is just a jar and the run command is just
`java -jar`.

Prove it to yourself while the app is running:

```bash
ps aux | grep java                 # your process, with its PID
lsof -i :8080                      # which process owns port 8080
kill <pid>                         # the port is released; the service is gone
```

---

## Ports, IPs, and why `ip:port` works

An IP address identifies a **machine**. A port identifies a **program on that machine**.
Together they identify one endpoint: `192.168.1.50:8080`.

Ports are 16-bit, so 1–65535. Below 1024 are "privileged" and, on Linux, need root or a
specific capability — which is why web servers historically ran as root to bind port 80,
and why we now avoid that (see below).

### The binding detail that trips up everyone

A process does not just choose a port; it chooses **which network interface** to listen on.

```
127.0.0.1  (localhost) — the loopback interface. Only this machine can connect.
192.168.1.50           — a specific network interface (your LAN address).
0.0.0.0                — ALL interfaces. Anyone who can route to this machine can connect.
```

Spring Boot binds `0.0.0.0` by default, so a fresh deployment is reachable from outside
immediately. Restrict it deliberately:

```yaml
server:
  port: 8080
  address: 127.0.0.1     # only local processes — e.g. a reverse proxy on the same host
```

> **This one setting is the difference between "the app is behind my proxy" and "the app is
> on the public internet".** With `0.0.0.0` and an open firewall, your unauthenticated
> `/customer/{id}/history` endpoint is exposed to the world. The usual production pattern:
> the app binds `127.0.0.1`, and only nginx — which handles TLS, rate limiting, and access
> control — can reach it.

Test the difference:

```bash
curl http://127.0.0.1:8080/cinemas        # always works locally
curl http://<your-lan-ip>:8080/cinemas    # only if bound to 0.0.0.0 and the firewall allows
```

### Changing the port

Three ways, in increasing precedence:

```yaml
server:
  port: 9090                              # application.yaml
```
```bash
export SERVER_PORT=9090                   # environment variable
java -jar app.jar --server.port=9090      # command-line argument (wins)
```

`server.port=0` picks a random free port — useful in tests so parallel builds do not
collide.

**"Port already in use"** means another process holds it. Find it:

```bash
lsof -i :8080          # macOS/Linux
sudo ss -lptn 'sport = :8080'    # Linux
```

---

## Building a deployable artifact

Chapter 01 showed `mvn package` failing on the shade plugin, and why the fix is Spring
Boot's own plugin:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

```bash
mvn clean package
ls -lh target/*.jar        # now tens of MB — it contains every dependency
java -jar target/BookMyShow-1.0-SNAPSHOT.jar
```

Remember the second half of that chapter: **the jar will still fail unless you also fix the
`user.dir` filesystem reads** in `ConnectionFactory` and `BookMyShowApplication`. Do
Exercise 6 before attempting a real deployment.

### JVM options you should know

```bash
java -Xms512m -Xmx512m \
     -XX:+UseG1GC \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/bookmyshow/ \
     -Duser.timezone=UTC \
     -Dspring.profiles.active=prod \
     -jar app.jar
```

| Flag | What it does |
|------|--------------|
| `-Xms` / `-Xmx` | initial / maximum heap size |
| `-XX:+UseG1GC` | garbage collector (the default on modern JDKs) |
| `-XX:+HeapDumpOnOutOfMemoryError` | dumps the heap on OOM so you can diagnose it |
| `-Duser.timezone=UTC` | pins the JVM timezone — see chapter 11 |

> **Why set `-Xms` equal to `-Xmx`?** The JVM grows the heap on demand, and each growth is
> a pause. Setting them equal allocates everything upfront: predictable memory usage and no
> resize pauses. Standard practice for servers.
>
> **How big?** Leave room — the JVM uses memory beyond the heap for thread stacks, metaspace,
> and native buffers. On a 1 GB container, a 512 MB heap is a sane starting point. Set
> `-Xmx` too close to the container limit and the **kernel OOM-killer** terminates your
> process with no Java stack trace and no warning, which is a genuinely confusing failure
> to debug the first time.

---

## Getting it onto a Linux server

```bash
# 1. Copy the artifact
scp target/BookMyShow-1.0-SNAPSHOT.jar user@server:/tmp/

# 2. On the server: a dedicated non-root user
sudo useradd --system --no-create-home --shell /usr/sbin/nologin bookmyshow
sudo mkdir -p /opt/bookmyshow /var/log/bookmyshow
sudo mv /tmp/BookMyShow-1.0-SNAPSHOT.jar /opt/bookmyshow/app.jar
sudo chown -R bookmyshow:bookmyshow /opt/bookmyshow /var/log/bookmyshow
```

> **🔴 Never run an application as root.** If someone finds a remote code execution flaw in
> your service — through a dependency CVE, a deserialization bug, anything — they inherit
> whatever privileges the process has. As root, that is the entire machine: they read every
> file, install a backdoor, and pivot to your other systems. As `bookmyshow`, a user with a
> `nologin` shell that owns two directories, the same exploit gets them almost nothing.
>
> This is the **principle of least privilege**, and it is the cheapest security control
> that exists: three commands, and it converts a total compromise into a contained one.

### Why not just run it with `&`?

```bash
java -jar app.jar &        # DON'T
```

It dies when you log out. It does not restart if it crashes. It does not start when the
machine reboots. Its output goes nowhere useful. You have no way to check its status.

Use the OS's **service manager**. On modern Linux that is systemd.

### A systemd unit file

`/etc/systemd/system/bookmyshow.service`:

```ini
[Unit]
Description=BookMyShow Backend
After=network.target mysql.service

[Service]
Type=simple
User=bookmyshow
Group=bookmyshow
WorkingDirectory=/opt/bookmyshow

EnvironmentFile=/etc/bookmyshow/env        # secrets live here, chmod 600, NOT in git
ExecStart=/usr/bin/java -Xms512m -Xmx512m -Duser.timezone=UTC \
          -jar /opt/bookmyshow/app.jar --spring.profiles.active=prod

Restart=on-failure
RestartSec=5s

# stdout/stderr → journald (chapter 11: log to stdout, let the platform collect)
StandardOutput=journal
StandardError=journal

# Hardening: the app cannot write anywhere it does not need to
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/var/log/bookmyshow

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable bookmyshow      # start automatically on boot
sudo systemctl start bookmyshow
sudo systemctl status bookmyshow
sudo journalctl -u bookmyshow -f      # follow the logs
```

What you just got for free: automatic start on boot, automatic restart on crash, log
collection and rotation, a standard way for anyone on the team to check status, and
filesystem isolation. This is why service managers exist.

`/etc/bookmyshow/env` (mode `600`, owned by root, never committed):

```
DB_URL=jdbc:mysql://db.internal:3306/movie_booking?useSSL=true&serverTimezone=UTC
DB_USER=bookmyshow_app
DB_PASSWORD=...
```

---

## Firewalls: the step people forget

Your app is running and bound to `0.0.0.0:8080`, but `curl http://server-ip:8080` from
your laptop times out. Almost always the firewall.

There are usually **two** layers, and both must allow the traffic:

**1. The host firewall** (`ufw`, `firewalld`, `iptables`):

```bash
sudo ufw status
sudo ufw allow 22/tcp       # SSH — do this BEFORE enabling, or you lock yourself out
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

**2. The cloud provider's firewall** — AWS Security Groups, GCP firewall rules, Azure NSGs.
These sit outside the machine, so `ufw` shows nothing and the packets never arrive.

> **Debugging "I cannot connect", in order.** Work outward; each step tells you where to
> look next:
>
> ```bash
> # On the server — is it even running and listening?
> sudo systemctl status bookmyshow
> sudo ss -lptn 'sport = :8080'         # is it on 127.0.0.1 or 0.0.0.0?
> curl http://127.0.0.1:8080/cinemas    # works? then the app is fine; it is a network issue
>
> # From your laptop
> ping <server-ip>                       # routable at all?
> nc -zv <server-ip> 8080                # port reachable? (this is the firewall test)
> curl -v http://<server-ip>:8080/cinemas
> ```
>
> Localhost works but remote does not → binding or firewall. `nc` hangs → firewall. `nc`
> gives "connection refused" → nothing is listening on that port. **Learning to bisect a
> network path like this will save you more hours than almost anything else in this
> tutorial.**

---

## A reverse proxy, and why you want one

Do not expose the Java process directly. Put nginx in front:

```
  internet ──443──▶ nginx ──8080──▶ Spring Boot (bound to 127.0.0.1)
                     │
                     ├── terminates TLS
                     ├── serves static files
                     ├── rate limits
                     ├── gzip
                     └── load balances across instances
```

`/etc/nginx/sites-available/bookmyshow`:

```nginx
upstream bookmyshow {
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;          # a second instance
    keepalive 32;
}

server {
    listen 80;
    server_name api.example.com;
    return 301 https://$host$request_uri;      # force HTTPS
}

server {
    listen 443 ssl http2;
    server_name api.example.com;

    ssl_certificate     /etc/letsencrypt/live/api.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.example.com/privkey.pem;

    location / {
        proxy_pass http://bookmyshow;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_connect_timeout 5s;
        proxy_read_timeout   30s;
    }
}
```

Why bother, concretely:

- **TLS in one place.** Certificates are renewed and configured by nginx; your app knows
  nothing about them.
- **The app binds `127.0.0.1`**, so it is unreachable except through the proxy.
- **Port 80/443 without root.** nginx handles privileged ports; your app stays on 8080 as
  an unprivileged user.
- **Load balancing and zero-downtime deploys.** With two instances, restart one at a time
  and nginx routes around it.
- **Timeouts and rate limits** are enforced before traffic touches your thread pool —
  chapter 09's "shed load early".

> **The `X-Forwarded-*` headers matter.** Behind a proxy, your app sees every request as
> coming from `127.0.0.1`, which breaks logging, rate limiting, and anything IP-based.
> nginx passes the real values in these headers; tell Spring to trust them with
> `server.forward-headers-strategy=native`.

### HTTPS is not optional

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d api.example.com     # free cert, auto-renewing, edits nginx for you
```

Over plain HTTP, everything is readable by anyone on the network path — including this
project's login endpoint, which sends a password in a JSON body. There is no longer any
excuse: certificates are free and automated.

---

## Containers, briefly

You will meet Docker. The idea: package the application *and* its runtime into an image, so
what runs in production is byte-identical to what you tested.

```dockerfile
FROM eclipse-temurin:11-jre-alpine
RUN addgroup -S app && adduser -S app -G app        # non-root, same principle as above
WORKDIR /app
COPY target/BookMyShow-1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
```

```bash
docker build -t bookmyshow:1.0 .
docker run -p 8080:8080 -e DB_URL=... -e DB_PASSWORD=... bookmyshow:1.0
```

`-p 8080:8080` maps a host port to a container port — the same `ip:port` idea, one layer
in. `MaxRAMPercentage` tells the JVM to size its heap from the *container's* memory limit
rather than the host's, which is essential: without it the JVM sees the whole machine's
RAM, sizes a huge heap, and gets OOM-killed by the container runtime.

Containers do not change any principle in this chapter — non-root, environment config,
health checks, graceful shutdown, log to stdout all still apply. They change the packaging
and make orchestration (Kubernetes) possible.

---

## Zero-downtime deployment

With one instance, deployment is an outage. With two and a load balancer, a **rolling
deploy**:

```
1. Take instance 1 out of the LB (readiness check starts failing)
2. Wait for its in-flight requests to finish  ← graceful shutdown, chapter 11
3. Stop it, install the new jar, start it
4. Wait for its health check to pass
5. Put it back in the LB
6. Repeat for instance 2
```

Two consequences people miss:

> **Both versions run simultaneously during the rollout.** So version N and N+1 must be
> compatible — you cannot rename a JSON field or drop a database column in one deploy. The
> standard technique is **expand/contract**: deploy code that reads both old and new
> shapes, migrate the data, then remove the old handling in a *later* deploy. Three deploys
> where you wanted one, and it is what makes deploys safe.
>
> **Database migrations must be backward-compatible with the running version.** Adding a
> nullable column is safe. Dropping a column while the old version still selects it is an
> outage.

---

## What to do when it breaks at 3 AM

A rough order of operations:

```bash
sudo systemctl status bookmyshow             # running? restarting in a loop?
sudo journalctl -u bookmyshow -n 200 --no-pager    # what did it say before dying?
curl -s localhost:8080/actuator/health       # what does it think of itself?

df -h                                        # disk full? (very common — usually logs)
free -m                                      # memory?
top                                          # CPU? which process?

sudo ss -lptn                                # is it listening?
mysql -h db.internal -u app -p -e "SELECT 1" # can the DB be reached from here?

jcmd <pid> Thread.print > /tmp/threads.txt   # all threads stuck on the same lock?
jcmd <pid> GC.heap_info                      # heap exhausted?
```

> **The most common causes, roughly in order:** disk full from unrotated logs; database
> connection exhaustion; a dependency (the database, a payment gateway) being down or slow;
> an out-of-memory kill; and a bad deploy. Check the boring ones first — it is almost never
> the exotic explanation, and `df -h` takes two seconds.
>
> **And: fix the immediate problem first, understand it second.** Restore service, then do
> the post-mortem. A blameless post-mortem that asks "what made this failure possible?"
> rather than "who broke it" is how teams actually get more reliable — the answer is
> usually a missing guardrail, not a careless person.

---

## Checkpoint

- [ ] Explain what "the server is running" means at the process/port level
- [ ] Explain the difference between binding `127.0.0.1` and `0.0.0.0`, and when to use each
- [ ] Explain why the app must not run as root
- [ ] Write a systemd unit and start the app with it
- [ ] Debug an unreachable port with `ss`, `curl`, and `nc`, in the right order
- [ ] Explain three things a reverse proxy gives you
- [ ] Explain why both versions run at once during a rolling deploy, and what that forbids

Next: [13-testing.md](13-testing.md).
