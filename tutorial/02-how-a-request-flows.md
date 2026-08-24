# 02 — How a Request Flows

This is the mental model everything else in the tutorial hangs off. We are going to
follow one single HTTP request from the moment bytes arrive on a socket to the moment
JSON goes back out, naming every component it touches *in this codebase*.

The request we will trace:

```bash
curl http://localhost:8080/cinemas/1/halls/1/timings
```

---

## The 10,000-foot view

```
  curl                                     your JVM process (one OS process)
   │                                        ┌─────────────────────────────────────┐
   │  TCP connect to 127.0.0.1:8080         │                                     │
   ├───────────────────────────────────────▶│ 1. Embedded Tomcat                  │
   │                                        │      accepts socket                 │
   │  GET /cinemas/1/halls/1/timings        │      picks a worker thread          │
   │                                        │             │                       │
   │                                        │             ▼                       │
   │                                        │ 2. DispatcherServlet                │
   │                                        │      routes URL → handler method    │
   │                                        │             │                       │
   │                                        │             ▼                       │
   │                                        │ 3. CinemaController                 │
   │                                        │      binds @PathVariable            │
   │                                        │             │                       │
   │                                        │             ▼                       │
   │                                        │ 4. CinemaService                    │
   │                                        │      business logic, assembles DTOs │
   │                                        │             │                       │
   │                                        │             ▼                       │
   │                                        │ 5. HallDAO / LiveShowDAO / ...      │
   │                                        │      opens a MyBatis SqlSession     │
   │                                        │             │                       │
   │                                        │             ▼                       │
   │                                        │ 6. MyBatis mapper                   │
   │                                        │      builds SQL, runs it via JDBC   │
   │                                        └─────────────┼───────────────────────┘
   │                                                      │  TCP to 127.0.0.1:3306
   │                                                      ▼
   │                                                   MySQL (a separate OS process)
   │                                                      │
   │  ◀─── JSON ─── Jackson serialises the DTO tree ◀─────┘
```

Two separate processes, talking over two separate TCP connections. That is the whole
picture. Everything below is detail.

---

## Step 1 — Tomcat, threads, and what "the server" actually is

When you saw `Tomcat started on port(s): 8080` at startup, this happened:

1. Your JVM process asked the operating system for TCP port 8080 (a `bind` + `listen`
   syscall).
2. The OS now routes any connection arriving at that port to your process.
3. Tomcat created a **thread pool** — by default 200 worker threads in Spring Boot 2.x.

This is worth internalising because it explains most production performance behaviour:

> **One HTTP request is handled start-to-finish by exactly one thread**, in the classic
> servlet model this app uses. That thread is *blocked* — doing nothing, consuming a
> thread slot — for the entire time your code waits on the database.

Concretely: if a request needs 40 database round-trips and each takes 2 ms, that thread
is parked for 80 ms. With 200 threads, your theoretical ceiling is 200 concurrent
in-flight requests; request 201 waits in the accept queue. This is exactly why the N+1
query problem in [07-mybatis-data-layer.md](07-mybatis-data-layer.md) is not a
theoretical concern — it directly determines how many users this service can serve.

It also explains why **your code must be thread-safe**. 200 threads run your controller
and service methods *simultaneously*, on the same singleton bean instances. Which brings
us to a rule that governs this entire codebase:

> **Spring beans are singletons by default.** There is exactly one `CinemaService`
> object in the whole application, shared by all 200 threads. Therefore a Spring bean
> must be **stateless** — no mutable instance fields. Look at any service in this repo:
> the only fields are `@Autowired` DAO references, which are set once at startup and
> never written again. That is not an accident, it is the rule.
>
> The moment someone adds `private int lastBookingId;` as a field on a service, two
> concurrent requests will corrupt each other, and the bug will be intermittent,
> unreproducible locally, and appear only under production load. Local variables inside a
> method are fine — each thread gets its own stack. Instance fields are shared.

---

## Step 2 — DispatcherServlet: URL → method

Spring MVC's `DispatcherServlet` is the single servlet that receives every request. At
startup it scanned all `@RestController` classes and built a routing table from the
`@RequestMapping` annotations.

For our URL it finds `controller/CinemaController.java:102`:

```java
@RequestMapping(value = "/cinemas")          // class level, line 23
public class CinemaController {

    @RequestMapping(value = "/{cinemaId}/halls/{hallId}/timings", method = RequestMethod.GET)
    public ResponseEntity getScheduledTimeDetails(@PathVariable(name = "cinemaId") int cinemaId,
                                                  @PathVariable(name = "hallId") int hallId) {
```

The class-level `/cinemas` and the method-level path concatenate. `{cinemaId}` and
`{hallId}` are **path variables** — named holes in the pattern. Spring extracts `"1"` and
`"1"` as strings and converts them to `int` before calling your method.

> **What happens on bad input?** Try `curl http://localhost:8080/cinemas/abc/halls/1/timings`.
> Spring cannot convert `"abc"` to `int`, throws `MethodArgumentTypeMismatchException`,
> and returns a 400. That default is reasonable. Now try
> `curl http://localhost:8080/cinemas/999/halls/1/timings` — a *valid* int for a cinema
> that does not exist. That one reaches your code, and what happens next is this project's
> responsibility, not Spring's. Follow it through and see whether you get a sensible error
> or a `NullPointerException`. (Exercise 4.)

### 🟡 Smell: two annotation styles, mixed arbitrarily

This codebase writes handlers two ways:

```java
@RequestMapping(value = "/{cinemaId}", method = RequestMethod.GET)   // CinemaController
@PostMapping(value = "", consumes = ..., produces = ...)             // BookingController
```

`@GetMapping(...)` is just shorthand for `@RequestMapping(method = GET)` — identical
behaviour. Mixing both styles in one codebase costs nothing at runtime and costs real
time in code review, because a reader has to check which one they are looking at. Pick
one (modern convention: the shorthand `@GetMapping`/`@PostMapping`) and apply it
everywhere. This is the cheapest possible kind of code-quality win: purely mechanical,
zero risk.

---

## Step 3 — The controller

```java
public ResponseEntity getScheduledTimeDetails(@PathVariable int cinemaId, @PathVariable int hallId) {
    ResponseEntity responseEntity;
    HallDetail hallDetail = cinemaService.getHallDetail(cinemaId, hallId, false);
    if (hallDetail != null) {
        responseEntity = new ResponseEntity(cinemaService.getScheduledLiveShowByHallId(hallId, false), HttpStatus.OK);
    } else {
        responseEntity = new ResponseEntity(new ErrorResponse(ErrorMessages.NO_SHOW_FOR_TIMING), HttpStatus.OK);
    }
    return responseEntity;
}
```

**What a controller should do** — and this one mostly does:

1. Accept and validate input.
2. Call exactly one service method.
3. Translate the result into an HTTP response (status + body).

**What a controller must never do:** business logic, SQL, or calling DAOs directly. The
reason is testability and reuse — the moment logic lives in a controller, the only way to
exercise it is over HTTP. Note this controller is *already* leaking: it calls the service
twice and makes a decision between them. That "does the hall exist? then fetch timings"
sequence is business logic and belongs in `CinemaService`.

### 🔴 Production trap: errors returned as HTTP 200

Look closely at the `else` branch. The hall does not exist, so we return an
`ErrorResponse` — with status `HttpStatus.OK`. **200.**

This is wrong, and it is wrong throughout this codebase (see
[04-api-reference.md](04-api-reference.md) for the full tally). HTTP status codes are not
decoration; they are a contract that a great deal of infrastructure reads automatically:

- **Client libraries** branch on them. `response.ok` is true for a 200, so a JavaScript
  frontend takes the success path and then crashes on `data.map is not a function`.
- **Monitoring and alerting** compute your error rate from status codes. If failures
  return 200, your dashboard shows 0% errors during a total outage. You find out from
  customers.
- **Load balancers and service meshes** retry 5xx and pass 4xx straight through. Errors
  disguised as 200 are never retried.
- **Caches** happily cache a 200. Your error message can get cached and served to
  everyone.

The correct mapping for this codebase:

| Situation | Correct status | Currently returns |
|-----------|---------------|-------------------|
| Resource fetched fine | `200 OK` | 200 ✓ |
| Resource created (register, booking) | `201 Created` | 201 ✓ |
| Malformed/missing fields in body | `400 Bad Request` | 200 or a 500 |
| Not logged in / bad credentials | `401 Unauthorized` | 200 |
| Logged in but not allowed (non-admin adding a show) | `403 Forbidden` | 403 ✓ |
| Cinema/hall/booking does not exist | `404 Not Found` | 200 |
| Seat already taken, hold expired | `409 Conflict` | 200 |
| Unexpected server-side failure | `500 Internal Server Error` | often 200 |

> **The structural fix: `@ControllerAdvice`.** Notice that *every* controller method in
> this project wraps its call in `try/catch` and hand-builds an `ErrorResponse`. That is
> the same six lines copy-pasted across four controllers — and each copy is a chance to
> get the status code wrong, which is exactly what happened.
>
> Spring's answer is a **global exception handler**: one class annotated
> `@RestControllerAdvice` containing `@ExceptionHandler` methods. Your controllers and
> services simply *throw* `BookingException`; the advice catches it centrally and maps it
> to one consistent response shape and the right status. Controllers get to contain only
> the happy path, error formatting is defined once, and adding a new exception type is a
> five-line change in one file rather than an edit to every controller.
>
> This is a textbook application of the open–closed principle to real code, and it is
> Exercise 10. Chapter 10 walks through the design reasoning.

---

## Step 4 — The service layer

`CinemaService` is where the actual work happens. Its job is to orchestrate DAOs and
assemble **DTOs** (Data Transfer Objects) for the response.

```java
public HallDetail getHallDetail(int cinemaId, int hallId, boolean shouldShowSeatDetails) {
    HallDetail hallDetail = null;
    Hall hall = hallDAO.getHallBy(cinemaId, hallId);      // does this hall belong to this cinema?
    if (hall != null) {
        hallDetail = getHallDetail(hallId, shouldShowSeatDetails);
    }
    return hallDetail;
}
```

> **🟢 Good call.** `getHallBy(cinemaId, hallId)` checks both IDs together rather than
> just loading hall 1 and trusting it. If it only looked up `hallId`, then
> `/cinemas/4/halls/1/timings` would happily return hall 1's data even though hall 1
> belongs to cinema 1. That class of bug — accepting a parent ID and never verifying the
> child belongs to it — is one of the most common real-world API vulnerabilities
> (**IDOR**, Insecure Direct Object Reference). Here it is done right. Remember the
> pattern.

Two things about the service layer you should notice and carry into your own work:

**The DTO / entity split.** `Hall` (in `db/mappers/`) is a database row: generated code,
mirrors columns exactly, includes `createdAt`/`modifiedAt`. `HallDetail` (in
`models/response/`) is what the API returns: hand-written, shaped for the client, omits
internal fields. The service converts between them.

Why bother, when you could just return `Hall` and save the work?

- **Your database schema stops being your public API.** Rename a column and you would
  break every client. With a DTO you rename the column and adjust one mapping line.
- **You control exposure.** `Customer` has a `password` field. If controllers returned
  entities, every login response would ship the password hash to the browser. The
  `CustomerDetail` DTO simply does not have that field — the leak is impossible by
  construction, not by remembering to filter.
- **The response shape can differ from the storage shape.** `ShowDetail` nests cinemas →
  halls → timings → seats, a tree assembled from five flat tables. No single entity looks
  like that.

**The `boolean` parameter.** `getHallDetail(cinemaId, hallId, false)` — what does `false`
mean? You cannot tell without opening the method. This is called a **boolean trap**, this
codebase is full of them (`shouldShowSeatDetails` is threaded through six methods,
`isLive` through two), and chapter 10 shows how to remove them.

---

## Step 5 — The DAO layer, where this project gets unusual

Now the part that is genuinely specific to this repo. A normal Spring Boot app injects a
`DataSource`, and Spring manages connections and transactions for you. **This project
does not do that.** Look at `db/dao/HallDAO.java:18-22`:

```java
public Hall getHallById(int hallId) {
    @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
    HallMapper hallMapper = sqlSession.getMapper(HallMapper.class);
    return hallMapper.selectByPrimaryKey(hallId);
}
```

Three things to unpack.

**`ConnectionFactory.INSTANCE`** is a hand-rolled singleton (a Java enum used as a
singleton — a well-known idiom) that builds its own MyBatis `SqlSessionFactory` from
`mybatisConfig.xml`. It is completely independent of the Spring `DataSource` configured
in `application.yaml`. This project has **two parallel database stacks** and the DAOs use
the non-Spring one. Chapter 07 is largely about the consequences.

**`@Cleanup`** is Lombok. It compiles to a `try/finally` that calls `sqlSession.close()`
when the variable goes out of scope — equivalent to try-with-resources. Fine as far as it
goes; the problem is what is being closed.

**A new `SqlSession` per DAO method call.** Combined with `<dataSource type="UNPOOLED">`
in `mybatisConfig.xml`, that means **a brand-new TCP connection to MySQL, with a fresh
authentication handshake, for every single method call** — then thrown away. A TCP
connect plus MySQL auth is on the order of a millisecond locally and much worse across a
network. Chapter 07 measures what this costs on the endpoint we are tracing.

---

## Step 6 — MyBatis builds and runs the SQL

`sqlSession.getMapper(HallMapper.class)` returns a **dynamic proxy**: MyBatis generates an
object implementing your interface at runtime, where each method builds SQL and executes
it. There is no hand-written implementation class anywhere — that is the point of MyBatis.

`selectByPrimaryKey` is generated code in `db/mappers/HallMapper.java`, and produces:

```sql
select hall_id, hall_code, cinema_id, hall_row_count, hall_col_count, is_available,
       created_at, modified_at
from TBL_Hall
where hall_id = ?
```

The `?` is a **bind parameter**, not string concatenation. This matters enormously:

> **🟢 Good call: this codebase cannot be SQL-injected.** Because every query goes
> through MyBatis' generated builders with bind parameters, user input is sent to MySQL
> *separately* from the SQL text. The database parses the query first and treats the
> parameter purely as a value — so a user named `'; DROP TABLE TBL_Customer; --` is
> stored as that literal string, not executed. Had anyone written
> `"... where name = '" + name + "'"`, the whole database would be one input field away
> from destruction. Never build SQL by concatenating strings. Ever.

The result rows are mapped back onto the `Hall` object via the `@Results` annotation in
the generated mapper — `hall_id` → `hallId`, and so on.

---

## Step 6b — Back up the stack, and out through Jackson

The `Hall` returns to `CinemaService`, which builds a `HallDetail`, which the controller
wraps in a `ResponseEntity`. Now Spring must turn that Java object into JSON.

It uses **Jackson**, which Spring Boot auto-configured because it is on the classpath. By
reflection it walks the object's public getters — and this is where Lombok reappears:
`@Data` on `HallDetail` generated `getHallId()`, `getHallCode()`, etc., so Jackson finds
`hallId`, `hallCode`, `scheduledTimings`.

Two project-specific details in the serialization:

**`@JsonInclude(JsonInclude.Include.NON_NULL)`** on `HallDetail`, `CinemaDetail`, and
`ShowDetail` tells Jackson to omit null fields entirely rather than emit
`"hallRowCount": null`. That is why the same endpoint can return objects with different
key sets depending on the `shouldShowSeatDetails` flag.

**`Date` serialization.** `ScheduledTimingDetails.startTime` is a `java.util.Date`.
Jackson's default is to render it as a Unix epoch *number*, not a string — so clients get
`"startTime": 1582172400000`. Confirm this in your own response body. It is a legitimate
choice but a surprising one; most APIs prefer ISO-8601 (`"2020-02-20T10:00:00Z"`), which
you get by configuring `spring.jackson.date-format` or, far better, by migrating off
`java.util.Date` to `java.time.Instant`. See chapter 11 on why `Date` is a trap.

---

## The same trace, as a sequence diagram

```
curl        Tomcat        Dispatcher    CinemaController   CinemaService      HallDAO        MySQL
 │            │               │                │                 │               │             │
 ├─ GET ─────▶│               │                │                 │               │             │
 │            ├─ thread #47 ─▶│                │                 │               │             │
 │            │               ├─ route ───────▶│                 │               │             │
 │            │               │  bind 1, 1     │                 │               │             │
 │            │               │                ├─ getHallDetail ▶│               │             │
 │            │               │                │                 ├─ getHallBy ──▶│             │
 │            │               │                │                 │               ├─ CONNECT ──▶│
 │            │               │                │                 │               ├─ SELECT ───▶│
 │            │               │                │                 │               │◀── row ─────┤
 │            │               │                │                 │               ├─ CLOSE ────▶│
 │            │               │                │                 │◀─ Hall ───────┤             │
 │            │               │                │                 │  … repeats for liveShow,    │
 │            │               │                │                 │    scheduledLiveShow, …     │
 │            │               │                │◀─ HallDetail ───┤               │             │
 │            │               │◀─ ResponseEntity                 │               │             │
 │            │◀─ JSON (Jackson)                │                 │               │             │
 │◀─ 200 ─────┤               │                │                 │               │             │
```

Count the `CONNECT` / `CLOSE` pairs when you trace a real request with SQL logging on.
That count is the subject of chapter 07.

---

## Seeing it for yourself

Turn on SQL logging so you can watch every statement. In `application.yaml`:

```yaml
logging:
  level:
    com.project.bookmyshow: DEBUG
    org.apache.ibatis: DEBUG
    java.sql.Connection: DEBUG
    java.sql.PreparedStatement: DEBUG
```

Then hit `/cinemas` and count the statements in the log. Do it now — the number will
surprise you, and it is the single best motivator for chapter 07.

> **A habit worth forming early.** When you join a new codebase, do not start by reading
> files alphabetically. Pick one realistic request and trace it end to end, exactly as we
> just did. You will pass through every architectural layer, meet every convention the
> team uses, and finish with a map you can hang all future detail on. One trace teaches
> more than a week of browsing.

---

## Checkpoint

- [ ] Explain why a Spring singleton bean must not have mutable instance fields
- [ ] Explain what a Tomcat worker thread is doing while MySQL is answering a query
- [ ] Explain why returning HTTP 200 with an error body breaks monitoring and clients
- [ ] Explain why DTOs exist rather than returning `db/mappers/` entities directly
- [ ] Turn on SQL logging and count the queries behind `GET /cinemas`

Next: [03-domain-model-and-db-schema.md](03-domain-model-and-db-schema.md).
