# 11 — Production Readiness

The gap between "runs on my laptop" and "runs for paying customers". Everything here is a
concrete change to this project, ordered roughly by how badly it would hurt you.

---

## The 10-minute audit

If you learn one thing from this chapter, learn the questions:

| Question | This project's answer |
|----------|----------------------|
| Where do secrets come from? | Env vars with committed defaults — better, still not production-safe |
| What happens to data on restart? | Preserved — Flyway migrations, forward-only ✅ |
| How do you change config without redeploying? | DB settings yes; hold duration and catalogue window still hardcoded |
| What happens on an unhandled exception? | Stack trace to the client, or a 200 with an error body |
| Can you trace one user's request through the logs? | No — no correlation ID |
| Is input validated? | No |
| Are passwords hashed? | No — plaintext, and logged |
| Is there a health check? | No |
| Does it shut down gracefully? | No |
| Can you tell if it is broken right now? | No metrics, no alerts |

Ten questions. This project started by failing all ten; two and a half are now addressed.
That ratio is normal for a prototype and unacceptable for anything real — and each row
below is a fixable item, not a criticism.

---

## 1. Secrets and configuration

### The problem

```bash
grep -rn "ubona123" src/ pom.xml
```

There used to be four hits: `application.yaml`, `db.properties`, `generatorConfig.xml`, and
a hardcoded `DriverManager.getConnection(...)` inside `BookMyShowApplication`. The last is
gone and the first two are now `${DB_PASSWORD:ubona123}`-style defaults, but **a default is
still a committed secret**. All of them are in every clone, every fork, every CI log, and —
permanently — in `git log -p`, even if you delete them tomorrow.

### Two distinct ideas people conflate

**Configuration** is anything that differs between environments: database URL, port, log
level, hold duration. **Secrets** are the subset that must never be readable: passwords,
API keys, signing keys.

Config may live in the repo (with environment-specific overrides). Secrets never may.

### The fix

The **twelve-factor** rule is: *store config in the environment*. Spring Boot supports this
natively — any property can be overridden by an environment variable, with dots becoming
underscores and letters uppercased.

```yaml
# application.yaml — defaults for local development only
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/movie_booking}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}          # no default → must be supplied
```

```bash
export DB_URL='jdbc:mysql://prod-db.internal:3306/movie_booking?useSSL=true'
export DB_USER='bookmyshow_app'
export DB_PASSWORD='...'               # injected by the deployment system, never typed
java -jar app.jar
```

`${VAR:default}` uses the environment variable if set, otherwise the default. Leaving the
password default empty means a misconfigured production deploy **fails at startup** rather
than silently trying a blank password — fail-fast again.

For anything beyond a small deployment, the password itself comes from a secret manager
(AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets) rather than a shell variable, so
it can be rotated and audited.

### Spring profiles

```
application.yaml        # shared defaults
application-local.yaml  # local overrides
application-prod.yaml   # production overrides
```

```bash
java -jar app.jar --spring.profiles.active=prod
```

Profiles also gate beans — `@Profile("!prod")` on the mock payment gateway means it is
*impossible* to accidentally deploy the mock to production, because the bean does not exist
there. Enforce with structure, not with discipline.

### Externalise the magic numbers

```java
private static final int TICKET_HOLD_TIME_IN_MILLISEC = 300000;   // BookingUtils:12
int DISPLAY_SHOW_FOR_MONTHS = 3;                                  // AppConstants:4
```

Both are business policy someone will want to tune. Bind them properly:

```java
@ConfigurationProperties(prefix = "booking")
@Validated
public class BookingProperties {
    @NotNull private Duration holdDuration = Duration.ofMinutes(5);
    @Min(1)   private int catalogueMonths = 3;
}
```

```yaml
booking:
  hold-duration: 5m
  catalogue-months: 3
```

Type-safe, documented, validated at startup, and changeable without a code review.

### ✅ Already done: the schema drop

`BookMyShowApplication.run()` used to drop and recreate all eleven tables on every startup.
It has been replaced with Flyway:

```
src/main/resources/db/
├── migration/V1__initial_schema.sql                 ← CREATE TABLEs, no DROPs
├── migration/V2__status_master_reference_data.sql   ← FK targets, needed everywhere
└── seed/R__sample_data.sql                          ← demo data, 'local' profile only
```

Migrations are **versioned, forward-only, and immutable** — once `V1` has run anywhere you
never edit it; you add `V2`. Flyway records what it applied in a `flyway_schema_history`
table and skips it next time. Chapter 01 covers the reference-data/seed-data split and the
`baseline-on-migrate` setting.

The next schema change you make — Exercise 9's unique-key fix, for instance — becomes
`V3__customer_unique_email.sql`. Never an edit to `V1`.

---

## 2. Error handling

### The current state

Every controller repeats:

```java
try {
    response = new ResponseEntity<>(bookingService.initateBooking(request), HttpStatus.CREATED);
} catch (BookingException e) {
    response = new ResponseEntity<>(new ErrorResponse(e.getErrorMessage()), HttpStatus.OK);
}
```

Three problems: duplicated in six places, wrong status code, and it only catches
`BookingException` — an NPE (of which chapter 04 found several reachable ones) escapes and
produces Spring's default error page, which in a default configuration **includes the
stack trace**.

> **🔴 A stack trace in an HTTP response is an information leak.** It reveals your package
> structure, framework versions, and internal class names — a map for anyone probing for
> known CVEs. Sometimes it reveals SQL fragments or parameter values, which can include
> personal data. **Users get an error code; engineers get the stack trace, in the logs.**

### The fix

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ApiError> onBooking(BookingException e) {
        return ResponseEntity.status(e.getError().getStatus())
                .body(new ApiError(e.getError().name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .collect(joining(", "));
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION_FAILED", detail));
    }

    @ExceptionHandler(Exception.class)                        // the catch-all
    public ResponseEntity<ApiError> onUnexpected(Exception e) {
        String traceId = MDC.get("traceId");
        log.error("Unhandled exception [traceId={}]", traceId, e);     // full detail, internal
        return ResponseEntity.status(500)
                .body(new ApiError("INTERNAL_ERROR",
                      "Something went wrong. Reference: " + traceId));  // opaque, external
    }
}
```

Note the last handler: the client gets an opaque reference; support asks for it; you grep
the logs for that ID and get the full stack trace. The user learns nothing exploitable and
you lose nothing diagnostically.

Give errors a **machine-readable code** (`SEAT_UNAVAILABLE`) alongside the human message,
so clients can branch on the code and you can change the wording — or translate it —
without breaking anyone.

---

## 3. Input validation

Chapter 04 listed four ways to break `POST /booking` with a body. All are closed by Bean
Validation:

```java
@Data
public class BookingRequest {
    @NotNull @Positive          private Integer customerId;
    @NotNull @Positive          private Integer scheduledLiveShowId;
    @NotEmpty @Size(max = 10)   private List<@NotNull @Positive Integer> seatIdList;
}
```

```java
@PostMapping
public ResponseEntity<ApiResponse<BookingDetails>> initiateBooking(
        @Valid @RequestBody BookingRequest request) { ... }
```

`@Valid` makes Spring validate before your method runs, and a violation becomes a
`MethodArgumentNotValidException` that the advice above turns into a 400 with field-level
detail. Add `spring-boot-starter-validation` to the pom.

> **Validate at the boundary, once.** Every layer beneath the controller may then assume
> its inputs are well-formed. The alternative — defensive null checks scattered through
> services and DAOs — is noisy, incomplete, and never quite consistent. Note also that
> `@Size(max = 10)` is not just hygiene: without it, `seatIdList` with 100,000 entries is a
> denial-of-service vector.

Bean Validation does **not** cover business rules ("the seat must be free", "the showtime
must be in the future"). Those belong in the service, where the data lives.

---

## 4. Logging

### What is wrong now

```java
log.info("Customer Login - Email : {}, Password : {}", request.getEmail(), request.getPassword());
```

Plaintext passwords in the log. Also `ToStringBuilder.reflectionToString(addCustomerRequest)`
in `CustomerController:55` and `CustomerService:43` — reflection over *every* field,
including the password.

> **🔴 Never log credentials, tokens, card numbers, or personal data.** Logs are shipped to
> aggregators, retained for months, indexed for search, and read by people with far broader
> access than your database. Under GDPR and similar regimes, personal data in logs is also a
> compliance problem with real financial consequences.
>
> Beware reflection-based `toString()` in particular: it logs whatever fields exist
> *today*, so adding a sensitive field to a DTO silently starts leaking it, with no code
> change at the log line.

### What good logging looks like

**Levels, used consistently:**

| Level | Use for | Example here |
|-------|---------|--------------|
| `ERROR` | something broke; a human should look | payment gateway unreachable |
| `WARN` | unexpected but handled | hold expired during payment |
| `INFO` | significant business events | booking confirmed, ref XYZ |
| `DEBUG` | detail for diagnosis; off in prod | SQL statements |

`SeatDAO:37` logs every seat insert at DEBUG — 300 lines per hall. Correct level; would be
a disaster at INFO.

**Correlation IDs.** The single highest-value logging improvement. With 200 threads
interleaving, a lone log line is nearly useless. Assign an ID per request and attach it to
every line:

```java
@Component
public class TraceIdFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String traceId = Optional.ofNullable(req.getHeader("X-Trace-Id"))
                                 .orElse(UUID.randomUUID().toString());
        MDC.put("traceId", traceId);                 // MDC = per-thread logging context
        res.setHeader("X-Trace-Id", traceId);        // give it back so clients can quote it
        try { chain.doFilter(req, res); } finally { MDC.clear(); }   // ← must clear: threads are pooled
    }
}
```

Add `%X{traceId}` to the log pattern and every line carries it. Now
`grep abc-123 app.log` reconstructs one user's entire journey.

**Structured logging.** In production, emit JSON rather than prose:

```json
{"ts":"2026-08-24T10:15:32Z","level":"INFO","traceId":"abc-123","event":"booking_confirmed",
 "bookingRef":"KjHg...","seats":3,"amountPaisa":75000}
```

Log aggregators can then filter and aggregate on fields (`amountPaisa > 100000`) rather
than regex-matching English sentences. `logstash-logback-encoder` does this with a config
change.

**Where logs go.** Not a file managed by your app. The twelve-factor rule: **write to
stdout** and let the platform handle collection and rotation. If you must write files,
configure rotation and retention — an unrotated log file filling the disk takes down not
just your app but everything else on the machine, and it is a genuinely common outage
cause.

---

## 5. Security

Consolidated from earlier chapters, in priority order.

| # | Issue | Location | Fix |
|---|-------|----------|-----|
| 1 | Passwords in plaintext | `TBL_Customer`, `CustomerDAO` | BCrypt |
| 2 | Passwords logged | `CustomerController:35` | delete the line |
| 3 | No authentication | everywhere | JWT or session tokens |
| 4 | Identity from the request body | `customerId` params | derive from a verified token |
| 5 | IDOR on history/bookings | `/customer/{id}/history` | `/customer/me/history` |
| 6 | Predictable booking refs | `ApplicationUtils:36` | `SecureRandom` / UUIDv4 |
| 7 | No rate limiting | all endpoints | at the gateway/LB |
| 8 | No HTTPS | deployment | TLS at the reverse proxy — chapter 12 |
| 9 | Secrets in git | committed defaults in `application.yaml`, `db.properties`, `generatorConfig.xml` | no default in prod; fail to start without it |
| 10 | Stack traces to clients | default error page | `@ControllerAdvice` |

### Password hashing, concretely

```java
private final PasswordEncoder encoder = new BCryptPasswordEncoder();

// register
customer.setPassword(encoder.encode(rawPassword));

// login — note: look up by email ONLY, then verify
Customer c = customerDAO.getCustomerByEmail(email);
if (c != null && encoder.matches(rawPassword, c.getPassword())) { ... }
```

Note the change in shape: you can no longer query `WHERE email = ? AND password = ?`,
because each hash embeds a random salt and so differs even for identical passwords. That
salt is the point — it defeats rainbow tables and means two users with the same password
have different hashes.

BCrypt is deliberately slow (~100 ms), with a tunable work factor you increase as hardware
gets faster. **Slowness is the security property.** Never use MD5 or SHA-256 for passwords;
their speed is exactly what makes them wrong here.

### Authentication, in shape

```
POST /customer/login  ──▶  verify password  ──▶  issue signed JWT
                                                   { sub: 42, role: USER, exp: ... }

Later requests:  Authorization: Bearer <token>
                      │
                      ▼
                 filter verifies the signature, puts the user in the SecurityContext
                      │
                      ▼
                 @PreAuthorize("hasRole('ADMIN')")   ← declarative, not an if-statement
```

The token is signed with a server-held key, so a client cannot forge or alter it. **The
server never trusts a user ID that did not come out of a verified token.** Spring Security
provides all of this; do not hand-roll authentication.

---

## 6. Observability

### Health checks

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

`/actuator/health` reports UP/DOWN, including database connectivity.

> **Liveness vs readiness — a distinction worth knowing.**
> **Liveness** = "is this process healthy, or should it be killed and restarted?"
> **Readiness** = "can this instance serve traffic right now?"
>
> They differ during startup (alive but not ready — still warming up) and during dependency
> failures (alive, but the database is down, so stop sending it traffic without killing it).
> Spring Boot exposes `/actuator/health/liveness` and `/actuator/health/readiness`.
> Conflating them causes restart loops: a database blip kills every instance, and none can
> start until the database returns.

> **🔴 Do not expose all actuator endpoints publicly.** `/actuator/env` prints your entire
> configuration including resolved secrets; `/actuator/heapdump` returns a heap dump.
> Restrict with `management.endpoints.web.exposure.include=health,info,metrics` and bind
> the management port to an internal interface.

### Metrics

Micrometer ships with Actuator and gives you HTTP request rates, latency percentiles, JVM
memory, GC, and connection pool stats out of the box, exportable to Prometheus. Add the
business metrics from chapter 09 — holds created, conversion rate, payment success — with
a few lines each. Those usually detect an incident before the technical dashboards do.

### Graceful shutdown

By default, `SIGTERM` kills the process immediately, dropping in-flight requests. Someone's
payment is mid-flight when you deploy.

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

Tomcat then stops accepting new connections, finishes what it is serving, and exits.
Combined with the load balancer removing the instance first, deploys become invisible to
users. **Deployment should never be an outage.**

---

## 7. Time and dates

Three time bugs, all from the same root cause.

**1. `java.util.Date` is a deprecated design.** Mutable (so a `Date` field can be changed by
anyone who gets a reference), no timezone information, and months numbered from zero. Use
`java.time`: `Instant` for a moment in time, `LocalDateTime` for a wall-clock time with no
zone, `ZonedDateTime` when the zone matters.

**2. Zone-dependence everywhere.** `FastDateFormat.getInstance(DATE_FORMAT)`
(`ApplicationUtils:19`) uses the **JVM default zone**. `new Date()` is UTC internally but
prints in the default zone. MySQL `datetime` stores no zone at all. So the same code
produces different results on a laptop in IST and a server in UTC — and nothing warns you.

**3. `utcToIST()` subtracts 5:30** when IST is UTC**+**5:30 (`ApplicationUtils:29-33`).
Unused, so dormant. Chapter 06 covers why manual offset arithmetic is always wrong
eventually.

### The rule

> **Store UTC. Compute in UTC. Convert to a local zone only at the display edge.**

Use `TIMESTAMP` (which MySQL stores as UTC) rather than `DATETIME` (which stores a literal
wall-clock value with no zone), set the JVM to UTC explicitly (`-Duser.timezone=UTC`) so it
does not depend on the host, and put `serverTimezone=UTC` in the JDBC URL. Then a timestamp
means exactly one thing everywhere in the system, and the frontend renders it in the user's
zone.

For show times specifically there is a subtlety worth thinking about: a 10:00 AM show is
10:00 AM *in the cinema's local time*, permanently — it does not shift when daylight saving
changes. So the correct model may be a `LocalDateTime` plus the cinema's `ZoneId`, not an
`Instant`. **Modelling time correctly means asking what the value actually means**, not
just picking a type.

---

## 8. A production readiness checklist

Steal this for your own projects.

**Config & secrets**
- [ ] No secrets in the repo; all injected from the environment
- [ ] Config differs per environment without a rebuild
- [ ] The app fails to start on missing required config

**Data**
- [ ] Schema managed by versioned migrations; nothing is ever dropped
- [ ] Backups exist, and a restore has actually been tested
- [ ] Connection pool sized and bounded

**Correctness**
- [ ] Input validated at the boundary
- [ ] Transactions cover multi-write operations
- [ ] Concurrency invariants enforced by database constraints
- [ ] Money is an integer or `BigDecimal`, never a float

**Failure**
- [ ] All exceptions handled; no stack traces to clients
- [ ] Correct HTTP status codes
- [ ] Timeouts on every outbound call
- [ ] Retries with backoff where safe; idempotency where retried

**Operations**
- [ ] Health checks (liveness and readiness)
- [ ] Metrics for the four golden signals + business metrics
- [ ] Structured logs with correlation IDs, to stdout
- [ ] Graceful shutdown
- [ ] Alerts a human actually acts on

**Security**
- [ ] Passwords hashed with bcrypt/Argon2
- [ ] Authentication on every non-public endpoint
- [ ] Authorisation checks resource ownership (no IDOR)
- [ ] HTTPS everywhere
- [ ] Rate limiting
- [ ] Dependencies scanned for known CVEs

---

## Checkpoint

- [ ] List every place a secret is hardcoded, and describe the env-var replacement
- [ ] Explain why the app must fail to start when a required secret is missing
- [ ] Explain what a correlation ID is and why 200 threads make it necessary
- [ ] Explain the difference between liveness and readiness
- [ ] Explain why bcrypt's slowness is a feature
- [ ] Explain the "store UTC, convert at the edge" rule

Next: [12-running-on-a-server.md](12-running-on-a-server.md).
