# 06 — Code Tour

Package by package. For each one: what it is for, what belongs in it, what is actually in
it, and where the boundaries have been crossed.

```
com.project.bookmyshow
├── server/        1 class   — application entry point
├── controller/    4 classes — HTTP layer
├── services/      5 classes — business logic
├── db/
│   ├── dao/       9 classes — data access, hand-written
│   ├── mappers/  44 classes — GENERATED, do not edit
│   ├── typehandlers/ 1      — custom JDBC↔Java conversion
│   └── ConnectionFactory    — hand-rolled MyBatis bootstrap
├── models/
│   ├── request/   5 classes — inbound DTOs
│   ├── response/  7 classes — outbound DTOs
│   ├── APIResponse          — marker interface
│   └── SeatDetails          — shared DTO
├── enums/         3 enums
├── constants/     3 classes
├── exceptions/    2 classes
└── utils/         2 classes
```

---

## The layered architecture, and why it exists

```
   HTTP request
        │
        ▼
   ┌─────────────┐   knows about: HTTP, status codes, JSON
   │ controller  │   must not know: SQL, MyBatis
   └─────────────┘
        │ calls, passing/receiving DTOs
        ▼
   ┌─────────────┐   knows about: business rules, orchestration
   │  services   │   must not know: HTTP or SQL
   └─────────────┘
        │ calls, passing/receiving entities
        ▼
   ┌─────────────┐   knows about: SQL, MyBatis, transactions
   │     dao     │   must not know: HTTP, business rules
   └─────────────┘
        │
        ▼
     database
```

Each layer talks only to the one directly below. **Dependencies point one way, downward.**
No DAO ever imports a controller.

Why bother? Three concrete payoffs, not abstractions:

1. **You can change one layer without touching the others.** Swap MyBatis for JPA and only
   `db/` changes. Add a gRPC interface alongside REST and only `controller/` changes,
   because the services know nothing about HTTP.
2. **You can test the middle in isolation.** `BookMyShowTestApplication` mocks
   `CustomerDAO` and tests `CustomerService` with no database and no HTTP server. That is
   only possible because the layers are separate — and chapter 13 shows where this
   codebase breaks that property.
3. **A reader knows where to look.** "Where is the seat-availability rule?" → services or
   DAO, never a controller. That predictability compounds enormously as a codebase grows.

**How to spot a violation:** look at the `import` statements. A service importing
`org.springframework.http.HttpStatus` is doing HTTP. A controller importing
`org.apache.ibatis.session.SqlSession` is doing SQL. Both are leaks. Check
`CinemaController:139-143` — the commented-out code constructs DAOs directly from a
controller, skipping the service layer entirely. It is commented out, but it shows the
pressure the design is under.

---

## `server/` — the entry point

**`BookMyShowApplication.java`** — now 26 lines. It used to be 44, doing four distinct
jobs, three of which did not belong in an entry point. Two have since moved out (schema
management to Flyway, seed data to a profile-guarded migration); the MyBatis bootstrap
below is the one that remains.

```java
@SpringBootApplication
@ComponentScan("com.project.bookmyshow")
public class BookMyShowApplication {
```

`@SpringBootApplication` is three annotations in one: `@Configuration`,
`@EnableAutoConfiguration`, and `@ComponentScan`. The **explicit `@ComponentScan` is
redundant** — the implicit one already scans the package of the annotated class and
everything beneath it. Since this class sits in `com.project.bookmyshow.server`, the
implicit scan would cover only `...server`, so the explicit annotation *is* doing work
here. The cleaner fix is to move the application class up to `com.project.bookmyshow`,
which is the conventional layout precisely because it makes scanning work with no
configuration.

```java
public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(BookMyShowApplication.class, args);
    try {
        ConnectionFactory.INSTANCE.init();
    } catch (Exception e) {
        log.error(ExceptionUtils.getStackTrace(e));
        applicationContext.close();
    }
}
```

> **🔴 Ordering bug.** `SpringApplication.run()` returns only after the entire context is
> built **and the HTTP port is open**. `ConnectionFactory.INSTANCE.init()` runs *after*
> that. So there is a window — short, but real — in which the service accepts requests
> while `sqlSessionManager` is still `null`, and any DAO call throws
> `NullPointerException`.
>
> This ordering is also why the old seed-data `CommandLineRunner` could never have used a
> DAO: Spring invokes runners *during* `SpringApplication.run()`, before `ConnectionFactory`
> exists at all. Moving seeding into a Flyway migration sidestepped the problem rather than
> solving it — the underlying ordering bug is still here.
>
> **The right way** is to let Spring own the lifecycle: make the MyBatis bootstrap a
> `@Bean` in a `@Configuration` class. Spring then constructs it as part of the context,
> before the port opens, and refuses to start if it fails. Manual initialisation *after*
> the framework has started is always a smell — you are fighting the container instead of
> using it.

The class used to also implement `CommandLineRunner` and drop/recreate every table on
startup, with a hardcoded connection string that ignored every config file in the project.
That is gone; chapter 01 covers what replaced it and why.

> **Design point: what belongs in a `main` method?** Ideally one line —
> `SpringApplication.run(...)`. Everything else is a responsibility that belongs to a
> dedicated class: schema management to a migration tool, connection setup to a
> `@Configuration`, seed data to a profile-guarded initialiser. The entry point should be
> boring. When `main` grows logic, it becomes the one place nobody can test.

---

## `controller/` — the HTTP layer

Four controllers, one per resource: `Cinema`, `Show`, `Customer`, `Booking`. Grouping by
resource rather than by verb is right and matches REST conventions.

**What they do well:** thin, delegate to services, use path variables correctly, and
`CinemaController` verifies the child-belongs-to-parent relationship (chapter 02).

**What they do badly**, all covered in chapters 02 and 04: errors as HTTP 200, duplicated
`try/catch`, raw `ResponseEntity` with `@SuppressWarnings`, mixed annotation styles, no
validation, and business decisions leaking upward.

One more thing to notice — the **repeated authentication preamble**. Every protected
handler opens with:

```java
if (customerService.isCustomerRegistered(bookingRequest.getCustomerId())) {
    ...
} else {
    response = new ResponseEntity<>(new ErrorResponse(ErrorMessages.SIGNED_IN_FOR_BOOKING), HttpStatus.OK);
}
```

Copy-pasted across `BookingController` twice, `CustomerController` once, and in a variant
form (`isAdmin`) in `ShowController`. Each copy costs a database query and is a chance to
forget the check entirely on a new endpoint.

> **The structural answer is a cross-cutting concern mechanism**, because authentication
> applies to *many* endpoints and is not the business logic of any of them. Spring offers
> several, in increasing order of power: a **`HandlerInterceptor`** (runs before the
> handler; can reject), a **servlet `Filter`** (runs even earlier, before Spring MVC), or
> **Spring Security** (a full framework built exactly for this). With any of them, the
> check is declared once and applied by rule, and a new endpoint is protected by default
> rather than by remembering. **Security that depends on every developer remembering is
> not security.**

---

## `services/` — business logic

| Class | Lines | Notes |
|-------|-------|-------|
| `BookingService` | ~185 | The core. Holds, payment orchestration, ticket assembly. |
| `CinemaService` | ~205 | Cinema/hall/showtime/seat DTO assembly. The most complex. |
| `ShowService` | ~95 | Movie listing and creation. |
| `CustomerService` | ~105 | Registration, login, role checks. **Missing `@Service`.** |
| `PaymentService` | 16 | A static mock. |

### `PaymentService` — the odd one out

```java
public class PaymentService {
    public enum Status { SUCCESS, FAILED, INPROGRESS }
    public static Status processPayment() { return Status.SUCCESS; }
}
```

No `@Service`, no interface, a static method, and called statically from
`BookingService:73`.

> **🟡 Static calls are invisible dependencies.** `BookingService` depends on
> `PaymentService`, but that dependency appears nowhere in its constructor or fields —
> only buried in a method body. Consequences:
>
> - **You cannot substitute it in a test.** Standard Mockito cannot mock a static method
>   (you need `mockito-inline` and `mockStatic`, which is a workaround, not a design). So
>   you can never write a test for "what does `finalizeBooking` do when payment fails?" —
>   which is exactly the case you most need to test.
> - **You cannot swap implementations.** Real payments need Razorpay in production and a
>   fake locally. With a static call, the only lever is an `if` inside the method.
>
> **The fix is textbook dependency inversion.** Define what you need as an interface, and
> let the caller be handed an implementation:
>
> ```java
> public interface PaymentGateway {
>     PaymentResult charge(String bookingRef, long amountInPaisa, String idempotencyKey);
> }
>
> @Service @Profile("!prod")  class MockPaymentGateway implements PaymentGateway { ... }
> @Service @Profile("prod")   class RazorpayGateway  implements PaymentGateway { ... }
> ```
>
> `BookingService` now declares `private final PaymentGateway gateway;` — the dependency
> is visible in the type, a test injects a fake, and swapping providers touches no
> business logic. **This is what dependency injection is actually for.** It is not about
> avoiding the `new` keyword; it is about the caller being unable to see, and therefore
> unable to depend on, which concrete implementation it got. Chapter 10 develops this.

### `CinemaService` — where the complexity concentrates

Twelve methods, five of them overloads of `getHallDetail`/`getCinemaDetails`/
`getScheduledLiveShow*`, and a `boolean shouldShowSeatDetails` threaded through nearly all
of them.

The genuinely broken part is `getCinemaDetailsForShow(List<LiveShow>, boolean)`
(lines 124-145), which tries to merge halls when one movie plays in several halls of the
same cinema:

```java
if (cinemaDetailHashMap.containsKey(cinemaDetail.getCinemaId())) {
    CinemaDetail cachedCinemaDetails = cinemaDetailHashMap.get(cinemaDetail.getCinemaId());
    for (HallDetail hallDetail : cinemaDetail.getHallDetails()) {
        for (HallDetail newHallDetail : cachedCinemaDetails.getHallDetails()) {
            if (hallDetail.getHallId() != newHallDetail.getHallId()) {
                cachedCinemaDetails.addHallDetail(newHallDetail);
            }
        }
    }
    ...
```

Read the loops carefully. The outer iterates the *new* cinema's halls; the inner iterates
the *cached* cinema's halls; and on mismatch it adds `newHallDetail` — which came from the
**cached** collection — back into the cached collection. It is adding a hall that is
already there, while mutating the collection it is iterating.

Two failures, both fatal:

1. **`ConcurrentModificationException`** — mutating `cachedCinemaDetails.getHallDetails()`
   while the inner `for-each` is iterating it.
2. **`UnsupportedOperationException`** — which actually fires first. `CinemaDetail`
   declares `@Singular private List<HallDetail> hallDetails;` (line 19). Lombok's
   `@Singular` builds an **immutable** list (`Collections.unmodifiableList`). So
   `addHallDetail()` — which calls `hallDetails.add(...)` — throws the moment it is
   reached.

Look at `addHallDetail` itself:

```java
public void addHallDetail(HallDetail hallDetail) {
    if (hallDetails == null) { hallDetails = new ArrayList<>(); }
    hallDetails.add(hallDetail);
}
```

The null guard suggests the author expected the field to sometimes be null. With
`@Singular`, Lombok initialises it to an empty immutable list, so the guard never fires and
the `add` always throws.

> **Why has nobody hit this?** Because the seeded data has each movie in exactly one hall
> per cinema, so `containsKey` is never true and the branch never executes. **A latent
> bug: correct-looking code on a path that data has never reached.** These are the bugs
> that surface at the worst possible moment — when the business grows into the case you
> never tested. The lesson: when you write a branch, write a test that reaches it, or
> delete the branch.
>
> The deeper lesson is about `@Singular` specifically: it is a *builder* convenience that
> also changes the mutability of your object. Convenient annotations have semantics.
> Read them before adopting them. Exercise 7 fixes this method.

---

## `db/dao/` — data access

Nine hand-written DAOs. Every method follows one shape:

```java
public Hall getHallById(int hallId) {
    @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
    HallMapper hallMapper = sqlSession.getMapper(HallMapper.class);
    return hallMapper.selectByPrimaryKey(hallId);
}
```

Fully analysed in chapter 07. Two structural problems worth flagging here:

**1. `@Repository` beans constructed with `new`.**

```bash
grep -rn "new SeatBookingDAO()\|new ShowBookingDAO()" src/main/java
```

```
db/dao/ShowBookingDAO.java:49:  SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
db/dao/ShowBookingDAO.java:84:  SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
db/dao/SeatDAO.java:66:         ShowBookingDAO showBookingDAO = new ShowBookingDAO();
db/dao/SeatDAO.java:68:         SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
```

Every one of these classes is annotated `@Repository`, so Spring already has a singleton
instance ready to inject. These call sites ignore it and allocate a new object per call.

Today the classes are stateless so it "works". But it means: a new object allocated on
every call for no reason; any future field, cache, or metric on the DAO silently does
nothing; and — most importantly — **these dependencies cannot be mocked in a test**,
because the test has no way to reach inside the method and substitute the instance.
Constructing collaborators with `new` inside a method is the same invisible-dependency
problem as the static `PaymentService`, in a different costume. Fix by injecting:

```java
@Autowired private SeatBookingDAO seatBookingDAO;
```

**2. Business logic in the DAO layer.** `ShowBookingDAO.initiateBooking()` decides whether
seats are available, builds the `ShowBooking`, computes `ticketPrice × seatCount`, sets
the status, and throws `BookingException`. That is business logic, and the layering
diagram says it belongs in `BookingService`. The DAO should offer narrow persistence
operations (`insert`, `findByRef`, `updateStatus`) and let the service compose them. As it
stands, the class doing the SQL is also the class deciding the business rules, so neither
can be tested or changed independently.

### `db/mappers/` — 44 generated classes. Do not edit.

Every file carries:

```java
@Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.709+05:30", ...)
```

They are produced by the MyBatis Generator from `generatorConfig.xml`, by running:

```bash
mvn mybatis-generator:generate
```

**Any edit you make is destroyed the next time anyone regenerates.** If a mapper needs to
behave differently, change `generatorConfig.xml` and regenerate; if you need a query the
generator cannot express, write it in a separate, hand-written mapper interface that
lives outside this package.

Three files per table:

- `Customer.java` — the row as a POJO
- `CustomerMapper.java` — the interface with the CRUD methods
- `CustomerDynamicSqlSupport.java` — typed column references for building queries

> **Generated code is a real trade-off, worth understanding.** You get 44 correct,
> consistent, type-safe classes for free, and they cannot drift from the schema. You pay
> with: a large amount of code in your repo that nobody reads or reviews; a build step
> that requires a **live database connection** (look at `generatorConfig.xml` — it
> connects to `localhost:3306` to read the schema), which means it cannot run in CI on a
> clean machine; and an absolute path baked into the config:
> ```xml
> targetProject="/Users/yatharthlakhera/GitWorkspace/BookMyShow/src/main/java"
> ```
> — pointing at one person's laptop, at a directory that does not even exist on the
> machine this tutorial was written on. Nobody else can regenerate. That path should be
> `${project.basedir}/src/main/java`. **Machine-specific absolute paths in committed
> config are always a bug.**

---

## `models/` — DTOs

`request/` (5) and `response/` (7), plus `APIResponse` and `SeatDetails`. All use Lombok:

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CinemaDetail implements APIResponse {
```

- `@Data` = `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + a required-args
  constructor.
- `@Builder` gives the fluent `CinemaDetail.builder().cinemaId(1).build()` style used
  throughout the services.
- `@JsonInclude(NON_NULL)` omits null fields from the JSON — applied to `CinemaDetail`,
  `HallDetail`, and `ShowDetail`, but **not** to `ScheduledTimingDetails`, `BookingDetails`,
  or `CustomerDetail`. That inconsistency is why some responses show
  `"seatDetailsList": null` and others simply omit the key.

> **🟡 `@Data` on a DTO is heavier than it looks.** It generates setters, making every DTO
> **mutable** — anyone can modify a response object after it is built, and a mutable object
> shared across threads is a hazard. It also generates `equals`/`hashCode` over *all*
> fields, so putting a DTO in a `HashSet` gives you semantics you did not think about.
>
> For an immutable DTO you want `@Value` (Lombok's immutable equivalent: final fields,
> getters, no setters) plus `@Builder`. On modern Java, a `record` does it natively.
> **Prefer immutable DTOs**: they are thread-safe for free, cannot be modified halfway
> through a call stack, and are far easier to reason about.

Notice `AddShowRequest` breaks the DTO rule by putting parsing logic in its getters
(chapter 04). A DTO should be a dumb data carrier; the moment it has behaviour, the
behaviour is hidden from callers who reasonably assume a getter is free.

---

## `constants/` — three classes, three lessons

```java
public interface AppConstants   { int DISPLAY_SHOW_FOR_MONTHS = 3; }
public interface ErrorMessages  { String BOOKING_SESSION_EXPIRED = "..."; ... }
public class StatusConstant     { public static final int SUCCESS = 1; ... }
```

> **🟡 The constant interface antipattern.** Fields in a Java interface are implicitly
> `public static final`, so an interface full of constants "works". *Effective Java*
> Item 22 argues against it, for a concrete reason: an interface is a **type**, a promise
> about behaviour. `class Foo implements ErrorMessages` compiles and silently exports
> every one of those strings as part of `Foo`'s public API. Constants are an
> implementation detail, and an interface makes them a contract.
>
> Use a `final` class with a private constructor, or — better here — put constants where
> they belong. `DISPLAY_SHOW_FOR_MONTHS = 3` is not a constant at all: it is a **business
> policy** that someone will want to change without a deploy, and it belongs in
> `application.yaml`. The same goes for `TICKET_HOLD_TIME_IN_MILLISEC` in `BookingUtils`.
>
> Rule of thumb: **a value is a constant only if changing it would be a bug.** `int
> SECONDS_PER_MINUTE = 60` is a constant. A five-minute hold window is configuration.

`StatusConstant` should be an enum, and its double-brace map initialiser is a trap — both
covered in chapter 03.

---

## `utils/` — two classes worth reading closely

**`BookingUtils`** — small, focused, correct, and pure: it takes values in and returns a
boolean, touching no database and no clock beyond `System.currentTimeMillis()`. Note the
overloads that accept the current time as a parameter:

```java
public static boolean isSeatOnHold(SeatsBooking sb, long currentTimeInMilliSeconds)
```

> **🟢 Good call, and worth stealing.** Passing time in as a parameter makes the logic
> **testable**: a test can assert behaviour at exactly 4:59 and 5:01 without sleeping.
> Code that calls `System.currentTimeMillis()` internally can only be tested by actually
> waiting — so nobody tests it. (The mature version of this idea is injecting a
> `java.time.Clock`, which Java provides precisely so you can substitute a fixed clock in
> tests.) The author got this right; the shame is that no test uses it.

**`ApplicationUtils`** — a grab bag, and mostly a list of things not to do.

```java
public static Date utcToIST(@NonNull Date utcDate) {
    Date istDate = DateUtils.addHours(utcDate, -5);
    istDate = DateUtils.addMinutes(istDate, -30);
    return istDate;
}
```

IST is UTC **+5:30**. This subtracts 5:30, so despite its name it converts IST→UTC — or
produces a time 5.5 hours *behind* UTC, which is not a real timezone. (The method is
unused, so the bug is dormant.)

> **The real lesson is that this method should not exist.** Manual timezone arithmetic is
> always wrong eventually: offsets are not constant (daylight saving), they change by
> government decree, and historical dates use historical offsets. `java.time` exists to
> handle this — `instant.atZone(ZoneId.of("Asia/Kolkata"))` consults the IANA timezone
> database and is right.
>
> The correct architecture: **store and compute in UTC, convert only at the display
> edge.** Then the conversion happens once, in the client or the presentation layer, and
> your data has one unambiguous meaning. This project stores MySQL `datetime` (which has
> no timezone at all), compares it against `new Date()` (the JVM's default zone), and
> parses input with `FastDateFormat.getInstance(format)` (also the JVM's default zone).
> Three places, all implicitly zone-dependent, all silently different if the server's zone
> changes. Chapter 11 has the fix.

`getStatusString(int)` is a 15-line `switch` that maps a status to itself via
`StatusConstant.getName()` — every branch does the same thing. It reduces to
`return StatusConstant.getName(statusId);`, and disappears entirely if the status becomes
an enum.

`getParsedDate` swallows its exception and returns `null`, discussed in chapter 04.

---

## `exceptions/`

```java
public class BookingException extends Exception {
    private String errorMessage;
    public BookingException(String errorMessage) { super(errorMessage); this.errorMessage = errorMessage; }
    public String getErrorMessage() { return errorMessage; }
}
```

Note it stores the message **twice** — once in `Exception`'s own field via `super(...)`,
once in its own. `getMessage()` and `getErrorMessage()` return the same string. The extra
field is pure duplication; delete it and use the inherited `getMessage()`.

> **Checked vs unchecked, and why it matters here.** `BookingException extends Exception`
> makes it a **checked** exception: the compiler forces every caller to catch it or
> declare it. That is why `BookingService.initateBooking` declares `throws BookingException`
> and every controller has a `try/catch`.
>
> The modern convention in Spring applications is to extend `RuntimeException` (unchecked)
> and let a `@ControllerAdvice` catch it centrally. The reason is not laziness: a checked
> exception forces *every intermediate layer* to acknowledge an error it cannot do
> anything about, which is why you see `try/catch` blocks that simply re-wrap and rethrow.
> Reserve checked exceptions for conditions the immediate caller can genuinely recover
> from — which, for "this seat is taken", is not the case; only the outermost layer, which
> talks to the user, can act on it.
>
> Also note `BookingException` carries only a string. It cannot tell the caller *which*
> failure occurred, so mapping to a status code requires string matching. A better design
> carries a machine-readable code:
> ```java
> throw new BookingException(BookingError.SEAT_UNAVAILABLE, "Seats 4, 5 are taken");
> ```
> Then the advice maps `SEAT_UNAVAILABLE → 409` with no string comparison anywhere.

---

## Checkpoint

- [ ] Name the three layers and state what each must never know about
- [ ] Find one place where a layer boundary is crossed and explain the cost
- [ ] Explain why `new SeatBookingDAO()` inside a `@Repository` is a problem, even though
      it currently works
- [ ] Explain why you must never edit anything in `db/mappers/`
- [ ] Explain why passing the current time as a parameter makes `BookingUtils` testable

Next: [07-mybatis-data-layer.md](07-mybatis-data-layer.md) — the most project-specific
chapter in the tutorial.
