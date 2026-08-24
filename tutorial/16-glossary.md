# 16 — Glossary

Terms used in this tutorial, defined plainly, with a pointer to where they matter in this
project.

---

## Java and Spring

**Annotation processor** — a plugin that runs during compilation and can generate or modify
code. Lombok is one; it is why `@Data` produces getters that appear nowhere in the source,
and why an old Lombok breaks on a new JDK (chapter 01).

**Bean** — an object whose lifecycle Spring manages. Created at startup, injected wherever
needed. Made a bean by `@Component`, `@Service`, `@Repository`, `@Controller`, or `@Bean`.

**Bean Validation** — the `@NotNull` / `@Size` / `@Email` annotation standard. With `@Valid`
on a controller parameter, Spring rejects invalid input before your method runs
(chapter 11).

**Checked vs unchecked exception** — checked extends `Exception` and the compiler forces
callers to handle or declare it; unchecked extends `RuntimeException` and does not. Spring
applications usually prefer unchecked, handled centrally (chapter 06).

**Component scan** — Spring's startup search for annotated classes to register as beans.

**DTO (Data Transfer Object)** — an object shaped for moving data across a boundary, as
opposed to an entity, which mirrors a database row. `models/response/` holds DTOs;
`db/mappers/` holds entities (chapter 02).

**Dependency injection (DI)** — dependencies are handed to an object rather than fetched by
it. The point is that the object cannot see which implementation it got, so you can
substitute one (chapters 06, 10).

**Dynamic proxy** — an object implementing an interface, generated at runtime. How MyBatis
turns a mapper interface into working SQL, and how Spring implements `@Transactional`
(chapter 07).

**Fail-fast** — stop immediately when something is definitely wrong, rather than continuing
in an undefined state. Why a missing `@Service` kills startup (chapter 01).

**Field injection vs constructor injection** — `@Autowired` on a field vs a constructor
parameter. Constructor injection allows `final` fields, prevents half-built objects, and
makes tests framework-free (chapter 10).

**Marker interface** — an interface with no methods, used only as a label. `APIResponse` is
one (chapter 04).

**MDC (Mapped Diagnostic Context)** — a per-thread map that logging frameworks read, used to
attach a correlation ID to every log line (chapter 11).

**Servlet / DispatcherServlet** — the servlet API is Java's HTTP abstraction;
`DispatcherServlet` is the single Spring MVC servlet that routes every request to a handler
method (chapter 02).

**Singleton scope** — Spring's default: one instance shared by all threads. Which is why
beans must be stateless (chapter 02).

**Stereotype annotation** — `@Service`, `@Repository`, `@Controller`. All make a bean; the
different names document intent and let Spring apply layer-specific behaviour.

---

## Databases

**ACID** — Atomicity (all or nothing), Consistency (constraints hold), Isolation
(concurrent transactions do not corrupt each other), Durability (committed data survives a
crash).

**Composite key / index** — one built from several columns. Order matters: an index on
`(a, b)` serves `WHERE a = ?` but not `WHERE b = ?` (chapter 03).

**Connection pool** — a set of pre-opened database connections that are borrowed and
returned rather than created per query. HikariCP is Spring Boot's default. This project uses
`UNPOOLED` instead (chapter 07).

**Deadlock** — two transactions each holding a lock the other needs. InnoDB detects it and
kills one; your code must retry (chapter 08).

**Foreign key** — a column referencing another table's primary key, enforced by the
database, making orphaned references impossible.

**Idempotent** — an operation that produces the same result whether applied once or many
times. Essential for payments, since networks retry (chapters 04, 09).

**Index** — a sorted structure that turns a full table scan into a fast lookup, at the cost
of disk and slower writes (chapter 03).

**InnoDB** — MySQL's default storage engine; the one with transactions, row-level locking,
and foreign keys.

**Migration** — a versioned, forward-only, immutable schema change file, applied in order
by a tool like Flyway. The alternative to this project's drop-and-recreate (chapters 01, 11).

**N+1 query problem** — one query for a list, then one more per item. The most common
performance bug in backend code (chapter 07).

**Normalisation** — splitting data so each fact is stored once. Why this project has three
show tables instead of one (chapter 03).

**Optimistic locking** — assume conflicts are rare; detect them with a version column and
retry. Poor under high contention (chapter 08).

**Pessimistic locking** — assume conflicts will happen; take a lock upfront with
`SELECT ... FOR UPDATE` (chapter 08).

**Read replica** — a copy of the primary database that serves reads. Always slightly behind
— see replication lag (chapter 09).

**Replication lag** — the delay between a write on the primary and its appearance on a
replica. Breaks "write then immediately read" (chapter 09).

**Soft delete** — mark a row inactive (`is_open = 0`) instead of deleting it, preserving
foreign keys and history. Costs you a filter on every query (chapter 03).

**Transaction** — a group of statements that all take effect or none do (chapter 07).

**Unique constraint** — a database guarantee that a column combination appears at most once.
Cannot be raced, unlike an application check.
`UNIQUE (seat_id, scheduled_live_show_id)` is the key one here (chapters 03, 08).

---

## Concurrency

**Atomic operation** — indivisible; no other thread can observe a partial state. `UPDATE ...
WHERE <expected state>` is atomic; check-then-write is not (chapter 08).

**Compare-and-set (CAS)** — write only if the current value is what you expect. The
conditional-update pattern is CAS in the database (chapter 08).

**Distributed lock** — a lock held in shared infrastructure (e.g. Redis) so it works across
processes. What `synchronized` is not (chapter 08).

**Race condition** — behaviour that depends on the unpredictable timing of concurrent
operations.

**Stateless** — holds no per-request state between requests, so any instance can serve any
request. The property that makes horizontal scaling work (chapter 09).

**`synchronized`** — Java's built-in lock. Scoped to a single JVM, therefore useless across
multiple instances (chapter 08).

**Thread pool** — a fixed set of worker threads reused across requests. Tomcat's default is
200 (chapter 02).

**TOCTOU (Time Of Check To Time Of Use)** — the gap between checking a condition and acting
on it, during which the condition can change. The central bug of chapter 08.

**`volatile`** — guarantees that writes to a field are visible to other threads and are not
reordered. Required for double-checked locking; missing in `ConnectionFactory`
(chapter 07).

---

## HTTP and APIs

**Bind parameter** — a value sent to the database separately from the SQL text, so it can
never be interpreted as SQL. What makes injection impossible (chapter 02).

**Cursor vs offset pagination** — offset uses `page`/`size` (simple, degrades at high
offsets, can skip items); cursor uses an opaque token (stable and fast, no random access)
(chapter 04).

**Idempotency key** — a client-generated unique value per logical operation, so a retry
returns the stored result instead of repeating the action (chapter 09).

**IDOR (Insecure Direct Object Reference)** — accepting an ID from the client and returning
that object without checking the requester is allowed to see it.
`/customer/{customerId}/history` is one (chapters 02, 04).

**Idempotent HTTP methods** — GET, PUT, and DELETE should be safe to repeat; POST generally
is not.

**JWT (JSON Web Token)** — a signed token carrying claims (user ID, role, expiry). Any
instance can verify it without a shared session store (chapters 09, 11).

**Status codes** — 2xx success, 3xx redirect, 4xx client error, 5xx server error. This
project returns 200 for errors, which breaks clients, monitoring, and caches (chapter 02).

---

## Operations

**Correlation ID / trace ID** — a unique ID per request, attached to every log line, so one
user's journey can be reconstructed from interleaved logs (chapter 11).

**Four golden signals** — latency, traffic, errors, saturation. The default monitoring set
(chapter 09).

**Graceful shutdown** — stop accepting new requests, finish in-flight ones, then exit. Makes
deploys invisible to users (chapter 11).

**Horizontal vs vertical scaling** — more machines vs a bigger machine. Horizontal requires
statelessness (chapter 09).

**Liveness vs readiness** — "should this process be restarted?" vs "can it serve traffic
right now?" Conflating them causes restart loops (chapter 11).

**Least privilege** — grant only the access actually needed. Why the service runs as a
dedicated non-root user (chapter 12).

**p50 / p95 / p99** — latency percentiles. p99 = the slowest 1% of requests, which is where
user pain lives and where averages hide it (chapter 09).

**Reverse proxy** — a server in front of your application handling TLS, load balancing, and
rate limiting. nginx here (chapter 12).

**Rolling deploy** — replace instances one at a time so the service stays up. Means two
versions run simultaneously, which constrains what a single deploy may change (chapter 12).

**systemd** — the Linux service manager. Handles start-on-boot, restart-on-crash, and log
collection (chapter 12).

**Twelve-factor app** — a set of conventions for services: config in the environment, logs
to stdout, stateless processes, disposability (chapters 11, 12).

---

## Build and tooling

**Classpath** — the set of locations (directories and jars) the JVM searches for classes and
resources. A classpath resource works identically in an IDE and inside a jar; a filesystem
path does not (chapter 01).

**Fat jar / uber jar** — a jar containing your code *and* all dependencies, so
`java -jar` works standalone (chapters 01, 12).

**Maven lifecycle** — `compile` → `test` → `package` → `verify` → `install` → `deploy`. Each
phase runs all preceding ones.

**MyBatis Generator** — the tool that produced `db/mappers/` from the live schema. Requires a
database connection at build time (chapter 06).

**ORM vs SQL mapper** — an ORM (Hibernate) generates SQL from annotated entities; a SQL
mapper (MyBatis) maps between SQL you wrote and objects (chapter 07).

**Surefire vs Failsafe** — Maven plugins that run unit tests (`*Test`, during `test`) and
integration tests (`*IT`, during `verify`) respectively (chapter 13).

**Testcontainers** — a library that starts real dependencies in Docker for a test. Lets you
test against real MySQL instead of H2 (chapter 13).

---

## Testing

**Arrange–Act–Assert** — the three visible sections of a well-formed test.

**Mock / stub / spy** — a mock is a fake object; stubbing scripts its return values (`when`);
`verify` asserts an interaction happened. A spy wraps a real object and intercepts some calls
(chapter 13).

**Seam** — a place where a test can substitute an implementation. Code that constructs its
own dependencies has no seams and cannot be unit tested (chapter 13).

**Test pyramid** — many fast unit tests, fewer integration tests, very few end-to-end tests
(chapter 13).

---

## This project's domain

**Booking reference (`booking_ref_no`)** — the customer-facing 20-character random ticket
code. Unguessable by design, because `GET /booking/{ref}` has no authentication
(chapter 03).

**Hold** — a provisional seat reservation lasting five minutes: status `INITIATED` with a
recent `modified_at` (chapter 05).

**Lazy expiry** — holds are never actively cleaned up; expiry is recomputed on every read by
comparing `modified_at` to now. Simple, but every reader must remember the rule
(chapter 05).

**Live show** — a movie placed in a specific hall. The middle level of the show chain
(chapter 03).

**Paisa** — 1/100 of a rupee. Money is stored as an integer count of paisa, which is correct
— then converted to `Double` in Java, which is not (chapters 03, 07).

**Reserve-then-confirm** — the two-phase booking pattern: hold inventory, then finalise
after payment. Used by every ticketing, hotel, and airline system (chapter 05).

**Scheduled live show** — a live show at a specific time and price. The thing a customer
actually buys a ticket to; `scheduledLiveShowId` is the ID that appears in booking requests
(chapter 03).

**Show** — a movie title, independent of venue or time. Top of the show chain (chapter 03).

**Status master** — the four-row lookup table (`SUCCESS`, `FAILED`, `INPROGRESS`,
`INITIATED`) referenced by bookings and seat bookings (chapter 03).
