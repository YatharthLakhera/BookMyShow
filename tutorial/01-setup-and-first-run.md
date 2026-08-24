# 01 — Setup and First Run

Goal: get this service compiling, running, connected to MySQL, and answering an HTTP
request. You will hit at least four errors on the way. Every one of them is listed here
with the real message, because *reading an error message properly* is the skill being
taught in this chapter.

---

## What you need installed

| Tool | Version this project expects | Why |
|------|------------------------------|-----|
| JDK | 8 (`<java.version>1.8</java.version>` in `pom.xml`) | The pom targets Java 8 bytecode |
| Maven | 3.6+ | Build tool; downloads dependencies, compiles, packages |
| MySQL | 5.7 or 8.0 | The SQL dump was taken from 8.0.19 against a 5.7 server |

Check what you have:

```bash
java -version
mvn -v
mysql --version
```

On a Mac with multiple JDKs, list them and pick one:

```bash
/usr/libexec/java_home -V                    # list all installed JDKs
export JAVA_HOME=$(/usr/libexec/java_home -v 11)   # use JDK 11 for this shell only
```

> **Concept: `JAVA_HOME`.** Maven does not use whatever `java` is on your `PATH`; it uses
> the JDK at `JAVA_HOME`. If a build behaves differently from what you expect, print
> `mvn -v` — it tells you exactly which JDK Maven picked. This one variable explains a
> large fraction of "works on my machine" bugs.

---

## Error #1 — the build used to fail on a modern JDK (fixed, but read this)

`pom.xml` now pins `<lombok.version>1.18.30</lombok.version>`, so `mvn compile` works.
It did not always, and the failure is worth understanding because you will meet its shape
again:

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.8.1:compile
(default-compile) on project BookMyShow: Fatal error compiling:
java.lang.IllegalAccessError: class lombok.javac.apt.LombokProcessor (in unnamed module
@0x4d13b552) cannot access class com.sun.tools.javac.processing.JavacProcessingEnvironment
(in module jdk.compiler) because module jdk.compiler does not export
com.sun.tools.javac.processing to unnamed module @0x4d13b552
```

**How to read that.** Ignore the length; find the nouns. `lombok.javac.apt.LombokProcessor`
`cannot access` `com.sun.tools.javac...` `because module jdk.compiler does not export`.
So: Lombok is reaching into the internals of the Java compiler, and the compiler is
refusing.

**Why it happened.** Lombok is not a normal library. It is an *annotation processor* that
rewrites your code during compilation — that is how `@Data` can invent getters that do
not exist in the source file. To do that it hooks into private compiler internals. Java 9
introduced the module system (JPMS), which made those internals off-limits, and every JDK
since has tightened it. Lombok has to be updated for each new JDK, and the version this
project originally pinned — `1.18.10`, from 2019 — predates JDK 17 entirely.

You can watch it happen without editing anything, because the pom reads the version from a
property:

```bash
mvn compile -Dlombok.version=1.18.10     # reproduces the failure
mvn compile                              # uses 1.18.30, succeeds
```

> **🔴 Production trap: unpinned toolchains.** The pom says "Java 8" but nothing *enforces*
> it. A new laptop with a new JDK silently produces a different build or no build at all.
> Real projects pin the toolchain in the repo — `maven-enforcer-plugin` to fail fast with a
> clear message, or Maven Toolchains to use an exact JDK regardless of the machine. "It
> compiled on my machine" is not a build. Adding that enforcement is Exercise 1.

## Setting up the database

The service expects a MySQL database named `movie_booking` at `localhost:3306`. Create it:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS movie_booking;"
```

Everything inside it — tables, reference data, sample data — is created for you at startup
by Flyway. See the next section.

If your MySQL root password is not the committed default, supply it through the
environment rather than editing files:

```bash
export DB_PASSWORD='your-password'
```

`application.yaml` and `db.properties` both read it. Note that this project has **two**
independent database configurations — Spring's `spring.datasource` and MyBatis' own
`db.properties`/`mybatisConfig.xml` — and the DAOs use the second one. That is one of the
stranger things about this codebase and chapter 07 is largely about it.

There is a third copy in `generatorConfig.xml`, used only by the code generator:

```bash
grep -rn "ubona123" src/ pom.xml
```

> **🔴 Production trap: credentials in version control.** Those default passwords are
> committed to git forever. Even if you delete them in a later commit, `git log -p` still
> has them. The real rule: **secrets never enter the repository.** They arrive at runtime
> as environment variables or from a secret manager, and production has *no* working
> default so a misconfigured deploy fails loudly. The defaults that remain here exist only
> so a fresh clone runs without setup; chapter 11 covers removing them properly.

---

## How the schema gets created (this used to be a disaster)

The application no longer touches your schema on startup beyond applying **Flyway**
migrations, but the original behaviour is the single best cautionary tale in the project.

`BookMyShowApplication` used to implement `CommandLineRunner`:

```java
@Override
public void run(String... args) throws Exception {
    String script = System.getProperty("user.dir") + "/src/main/resources/MovieBooking.sql";
    ScriptRunner scriptRunner = new ScriptRunner(
            DriverManager.getConnection("jdbc:mysql://localhost:3306/movie_booking", "root", "ubona123"));
    scriptRunner.runScript(new BufferedReader(new FileReader(script)));
}
```

Spring calls `run()` once, automatically, **every time the application starts**. And
`MovieBooking.sql` is a `mysqldump` — it opens with:

```sql
DROP TABLE IF EXISTS `TBL_Cinema`;
CREATE TABLE `TBL_Cinema` ( ... );
INSERT INTO `TBL_Cinema` VALUES (1,'Cinema1',...);
```

`DROP TABLE` for all eleven tables, then recreate, then re-insert the samples.

**So every restart destroyed all data in the database.** Convenient for a laptop demo;
catastrophic in production, where a routine deploy or a crash-restart would wipe every
customer booking that ever existed, unrecoverably.

### What replaced it

```
src/main/resources/db/
├── migration/
│   ├── V1__initial_schema.sql                  ← the 11 CREATE TABLEs, no DROPs
│   └── V2__status_master_reference_data.sql    ← the 4 status rows
└── seed/
    └── R__sample_data.sql                      ← demo cinemas/halls/seats/shows
```

Flyway applies pending migrations at startup and records what it has applied in a
`flyway_schema_history` table, so each file runs exactly once, ever. Migrations are
**versioned, forward-only and immutable**: once `V1` has run anywhere you never edit it,
you add `V2`.

Three details in that layout are worth copying into your own projects:

**1. Reference data is not sample data.** The four `TBL_StatusMaster` rows are FK targets
for every booking, so the application cannot function without them — they belong in a
*migration*, applied everywhere including production. The demo cinemas and shows belong in
a *seed*, applied nowhere but a developer laptop. Conflating the two is how test fixtures
end up in production databases.

**2. The seed can only run locally, by construction.** `application.yaml` lists only
`classpath:db/migration`. `application-local.yaml` adds `classpath:db/seed`. So the sample
data is unreachable unless you explicitly activate the `local` profile — you cannot leak it
by forgetting a flag, because the default *is* the safe one. Prefer making mistakes
impossible over remembering not to make them.

**3. The seed is idempotent and repeatable.** `R__` (repeatable) migrations run after all
versioned ones and re-run whenever the file changes, so every statement uses
`INSERT IGNORE`. A seeder that blindly inserts creates duplicates on every restart.

### Starting for the first time

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS movie_booking;"
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Creating the *database* is still a manual step — Flyway manages objects inside a schema,
not the schema itself.

> **If you already have an old `movie_booking` from before this change**, it has the tables
> but no `flyway_schema_history`, and Flyway would normally refuse ("found non-empty schema
> without schema history table"). `application.yaml` sets `baseline-on-migrate: true` and
> `baseline-version: 1`, which records V1 as already applied instead of trying to run it.
> This is exactly the situation you hit when introducing Flyway to any existing project, so
> it is worth recognising.

### Database credentials

The four hardcoded copies of the password are down to the defaults in `application.yaml`
and `db.properties`. Configuration now comes from the environment:

```bash
export DB_URL='jdbc:mysql://localhost:3306/movie_booking?serverTimezone=UTC'
export DB_USER='root'
export DB_PASSWORD='...'
```

The defaults exist so a fresh clone runs without setup. **That is still wrong for
production** — a real deployment must have no working default, so a misconfigured deploy
fails loudly instead of quietly trying a committed password. Chapter 11 covers the rest.

## Starting the application

```bash
mvn spring-boot:run
```

There is no `spring-boot-maven-plugin` declared in this pom, but the Spring Boot parent
POM's plugin management makes the goal resolvable. If it does not work for you, run the
class directly from your IDE — right-click `BookMyShowApplication` → Run — which is what
most people do day to day anyway.

### Error #2 — the context used to fail to start (fixed, but understand it)

`CustomerService` is now annotated `@Service`. Before it was, startup aborted with:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Field customerService in com.project.bookmyshow.controller.BookingController required a
bean of type 'com.project.bookmyshow.services.CustomerService' that could not be found.
```

Compare `services/CustomerService.java` with `services/BookingService.java:21-23` and note
what was missing. Three controllers do
`@Autowired private CustomerService customerService;`, so Spring must supply an instance —
but with no stereotype annotation, `@ComponentScan` never registered the class as a bean,
so there was nothing to supply.

> **Why this is worth dwelling on.** Spring's dependency injection is a two-sided
> contract. `@Autowired` is the *request* ("give me one of these"). `@Service` /
> `@Component` / `@Repository` is the *offer* ("here is one you may hand out"). A
> `@Autowired` field with no matching offer is a startup failure, by design. Spring
> deliberately crashes at boot rather than handing you a `null` that would explode on the
> first customer request an hour later. That is called **fail-fast**, and it is a feature.
>
> Notice also *when* it failed: at application startup, not at the first HTTP call. Spring
> builds and wires the entire object graph before it opens the server port. So a wiring
> mistake can never reach a user — it takes the deploy down instead. When you get to
> chapter 12, this is why deployment pipelines run a health check before shifting traffic.
>
> You can reproduce it in thirty seconds: comment out the `@Service`, start the app, read
> the message. Do that — being able to recognise this error instantly is worth more than
> reading about it.

### Error #3 — the deprecated MySQL driver class (fixed)

You may still see this in older logs or other projects:

```
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is
`com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and
manual loading of the driver class is generally unnecessary.
```

A warning, not an error. The project uses MySQL Connector/J 8.x, where the driver class was
renamed, but `application.yaml` and `mybatisConfig.xml` both still named the old
`com.mysql.jdbc.Driver` while `generatorConfig.xml` named the new one — three config files,
two different answers.

`application.yaml` now omits `driver-class-name` entirely and `mybatisConfig.xml` names the
`cj` driver. **Omitting it is the modern default**: since JDBC 4.0, drivers register
themselves from the classpath via the ServiceLoader — that is the "SPI" the warning
mentions. Naming a driver class explicitly is legacy configuration that can only be wrong.

### Error #4 — `Public Key Retrieval is not allowed` / `Unable to load authentication plugin`

If you are on MySQL 8 with the default authentication plugin, connections may be rejected.
Two options — either switch that user back to the legacy plugin:

```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'ubona123';
FLUSH PRIVILEGES;
```

…or append connection parameters to the JDBC URL in `application.yaml` **and**
`mybatisConfig.xml`:

```
jdbc:mysql://localhost:3306/movie_booking?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
```

> **Note `serverTimezone=UTC`.** Without it, the MySQL JDBC driver may refuse to connect,
> or silently shift every `DATETIME` you read and write by your machine's UTC offset.
> This project stores show start/end times as `datetime` and compares them to
> `new Date()` in Java — so a timezone mismatch between the JVM and the database becomes
> "the show is listed at the wrong time" or "my hold expired instantly". Timezone
> handling in this project is genuinely broken; see `utils/ApplicationUtils.java:29-33`
> and the analysis in chapter 11.

---

## Confirming it works

Once startup succeeds, you will see something like:

```
Tomcat started on port(s): 8080 (http) with context path ''
Started BookMyShowApplication in 4.312 seconds (JVM running for 5.02)
```

Two things in that line matter and are explained in chapter 12: an **embedded Tomcat** is
now running inside your JVM process, and it is **listening on TCP port 8080**.

Make your first call:

```bash
curl -s http://localhost:8080/cinemas | python3 -m json.tool
```

You should get four cinemas, each with one hall and that hall's showtimes. If you get
JSON back, your setup is complete.

Try the seat map for the first showtime:

```bash
curl -s "http://localhost:8080/cinemas/1/halls/1/timings" | python3 -m json.tool
```

And register yourself a user:

```bash
curl -s -X POST http://localhost:8080/customer/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Intern","email":"intern@example.com","password":"hunter2"}'
```

Full endpoint list with payloads: [04-api-reference.md](04-api-reference.md).
Full booking walkthrough: [05-user-flows.md](05-user-flows.md).

---

## Error #5 — `mvn package` fails (and this one is interesting)

You will eventually want a deployable artifact rather than `mvn spring-boot:run`. Try it:

```bash
mvn package -DskipTests
```

It fails:

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-shade-plugin:3.1.1:shade
(default) on project BookMyShow: Unable to parse configuration of mojo
org.apache.maven.plugins:maven-shade-plugin:3.1.1:shade for parameter mainClass:
Cannot find 'mainClass' in class
org.springframework.boot.maven.PropertiesMergingResourceTransformer
```

**What is going on.** A Java application needs every class it uses on the classpath at
runtime. A plain jar of *your* classes is not runnable on its own — Spring, Jackson,
MyBatis, and the MySQL driver are not in it. So you need a "fat jar" (a.k.a. uber jar)
that bundles the dependencies too.

There are two ways to build one, and **this pom is accidentally using both**:

- `maven-shade-plugin` — the generic tool. It unpacks every dependency jar and merges
  all the class files into one flat jar.
- `spring-boot-maven-plugin` — Spring Boot's own. It keeps dependency jars *nested*
  intact and adds a custom classloader that reads them in place.

The pom declares `maven-shade-plugin` with a `ManifestResourceTransformer` carrying
`<mainClass>`. But `spring-boot-starter-parent` *also* pre-configures shade in its
`pluginManagement` with Boot's own transformer list. Maven merges the two configurations
by position, so the project's `<mainClass>` element lands on Boot's
`PropertiesMergingResourceTransformer`, which has no such field. Hence the error.

**The fix is not to fight the merge — it is to use the right plugin.** Delete the
`maven-shade-plugin` block and use Boot's plugin, which needs no configuration at all
because the parent already configured it:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

> **Why shade is wrong for Spring Boot specifically.** Spring relies heavily on files at
> fixed classpath locations — `META-INF/spring.factories`,
> `META-INF/spring.schemas`, `META-INF/additional-spring-configuration-metadata.json`.
> Dozens of jars each ship their own copy of the *same* path. Flattening them into one
> jar means later files overwrite earlier ones, so most of the registry disappears and
> autoconfiguration mysteriously stops working. That is what the `ServicesResourceTransformer`
> in the pom is trying to patch up, and why Boot has its own transformers. Boot's plugin
> sidesteps the whole problem by never flattening. Use the tool the framework ships.

### And even then, the jar will not run

There is a second, deeper problem. Note the jar that *did* get built before shade failed:

```bash
ls -la target/BookMyShow-1.0-SNAPSHOT.jar     # ~188 KB — far too small to contain Spring
cd /tmp && java -jar /path/to/target/BookMyShow-1.0-SNAPSHOT.jar
```

```
Error: Could not find or load main class com.project.bookmyshow.server.BookMyShowApplication
Caused by: java.lang.NoClassDefFoundError: org/springframework/boot/CommandLineRunner
```

Exactly as predicted: your classes, none of your dependencies.

Now the deeper issue. Even with a correct fat jar, this application still cannot run from
an arbitrary directory, because of two lines — both in `db/ConnectionFactory.java:25-28`:

- reads `System.getProperty("user.dir") + "/src/main/resources/db.properties"`
- reads `System.getProperty("user.dir") + "/src/main/resources/mybatisConfig.xml"`

(A third such read, of `MovieBooking.sql`, disappeared with the `CommandLineRunner`.)

`user.dir` is **the directory you launched the process from**, not where the jar lives.
So the app only works if your current working directory happens to be the project source
root. Deploy the jar to `/opt/bookmyshow/` on a server and start it from `/`, and it dies
looking for `/src/main/resources/db.properties`.

Prove it to yourself:

```bash
unzip -l target/BookMyShow-1.0-SNAPSHOT.jar | grep -E "mybatisConfig|db.properties"
```

```
      902  mybatisConfig.xml
      104  db.properties
```

The files **are** in the jar, at the classpath root. The code just does not read them
from there.

> **The distinction that matters: filesystem path vs classpath resource.**
>
> `new FileReader("/some/path/x.xml")` asks the operating system for a file at a location
> on disk. Fine in development, wrong for anything packaged, because after packaging your
> resources live *inside* a zip archive and have no filesystem path at all.
>
> The classpath is different: it is the set of locations (directories and jars) the JVM
> searches for classes and resources. Reading a resource *through the classpath* works
> identically whether it is a loose file in `target/classes` during development or an
> entry inside a jar in production:
>
> ```java
> // Plain Java
> InputStream in = getClass().getResourceAsStream("/mybatisConfig.xml");
>
> // MyBatis' own helper, which is what this project should use
> Reader reader = Resources.getResourceAsReader("mybatisConfig.xml");
>
> // Spring's abstraction, which handles classpath:, file:, and http: uniformly
> Resource r = new ClassPathResource("mybatisConfig.xml");
> ```
>
> **Rule of thumb: anything shipped with your application is a classpath resource. Never
> a filesystem path, and never relative to `user.dir`.** Filesystem paths are for things
> genuinely outside the app — a log directory, an upload folder, a mounted config volume.
>
> This one issue is a large part of why "it runs in my IDE but not as a jar" happens, and
> fixing it here is Exercise 6.

---

## Checkpoint

Before moving on, you should be able to:

- [ ] state which JDK Maven is using on your machine, and why that matters for Lombok
- [ ] explain what the old `CommandLineRunner` did to the database on every start, and why
      that is unacceptable in production
- [ ] explain the difference between a *migration* and a *seed*, and why the status rows
      are a migration while the demo cinemas are a seed
- [ ] explain why a missing `@Service` fails at startup rather than at request time
      (reproduce it by commenting the annotation out)
- [ ] `curl http://localhost:8080/cinemas` and get JSON
- [ ] explain the difference between a filesystem path and a classpath resource, and name
      the two places this project still gets it wrong

Next: [02-how-a-request-flows.md](02-how-a-request-flows.md) — follow one request all the
way down and back.
