# 15 — Answer Key

> **Stop.** If you have not genuinely attempted the exercise, close this file. Reading a
> solution produces a feeling of understanding that does not survive contact with the next
> problem.

This key gives the *reasoning* and the shape of the solution, not always complete code.
Where a solution is short, it is written out; where it is long, you get the approach and
the traps.

---

## Exercise 1 — Make the toolchain requirement explicit

```xml
<plugin>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <executions>
        <execution>
            <id>enforce-java</id>
            <goals><goal>enforce</goal></goals>
            <configuration>
                <rules>
                    <requireJavaVersion>
                        <version>[11,21)</version>
                        <message>Build with JDK 11-20. Lombok 1.18.30 does not support newer JDKs.</message>
                    </requireJavaVersion>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The `<message>` is the whole point. Without it a developer on the wrong JDK gets:

```
java.lang.IllegalAccessError: class lombok.javac.apt.LombokProcessor ... cannot access
class com.sun.tools.javac.processing.JavacProcessingEnvironment ...
```

…halfway through compilation, which tells them nothing actionable. With it, they get one
sentence at the start of the build.

> **The general principle: when a constraint exists, encode it in the build rather than in
> a README nobody reads.** The same reasoning drives Maven Toolchains (download and use an
> exact JDK regardless of what is installed), `.tool-versions` / `.sdkmanrc`, and lockfiles.
> A build that only works on machines configured a particular way, with no check that they
> are, will break for every new joiner.

---

## Exercise 2 — Understand the wiring failure

**Why at startup, not at first request?** Spring builds the entire object graph during
context initialisation — before Tomcat binds the port. Every `@Autowired` dependency is
resolved then. A missing bean means the graph cannot be completed, so the context fails and
the application exits.

**Why is that good?** Three reasons, in increasing importance:

1. **The failure is loud and immediate**, with a message naming the exact field and type.
2. **It cannot reach a user.** A deploy that fails to start never receives traffic, so the
   load balancer keeps the old version serving. Contrast a `NullPointerException` at 2 AM
   on a code path that only executes on refunds.
3. **It makes deployment safe by construction.** Your pipeline knows the difference between
   "started" and "did not start", and can roll back automatically.

This is **fail-fast**: when something is definitely wrong, stop immediately rather than
continuing in an undefined state. The same instinct drives validating input at the boundary
and refusing to start without a required secret.

**Why does `PaymentService` not need a stereotype?** Because nothing ever injects it —
`BookingService` calls `PaymentService.processPayment()` **statically**. No bean is
requested, so none is required. That is not a reprieve; it is the invisible-dependency
problem from chapters 06 and 10, and it is precisely why the payment-failure path cannot be
tested.

---

## Exercise 3 — Add the next migration

```sql
-- V3__cinema_name_length.sql
ALTER TABLE TBL_Cinema MODIFY COLUMN cinema_name varchar(255) NOT NULL;
```

**Why you must never edit `V1`.** Flyway stores a **checksum** of every applied migration in
`flyway_schema_history`. On startup it re-checksums the files and compares. Editing an
applied migration produces:

```
Migration checksum mismatch for migration version 1
```

…and the application refuses to start. That is protecting you from a genuine disaster: your
laptop would have the edited V1 applied to a database built from the *old* V1, while
production still has the old one, and the two schemas would silently diverge with nothing
recording the difference. **The history table is only meaningful if the files are immutable.**

**On the second startup after adding V3:** nothing. Flyway sees V1, V2, V3 in the history
table, finds no pending migrations, and moves on. Migrations are applied exactly once, ever.

**On the `ALTER` at scale:** MySQL 5.6+ can do many `ALTER`s online, but changing a column
type generally rewrites the whole table. On 50 million rows that is minutes to hours of
elevated I/O, and on older versions it takes a metadata lock that blocks reads and writes —
an outage. The standard answers are `pt-online-schema-change` or `gh-ost`, which build a
shadow table, backfill it, and swap. **A migration that is instant on your laptop is not
necessarily instant in production**, and that is one of the more common ways engineers cause
their first outage.

---

## Exercise 4 — Handle missing resources

`GET /cinemas/999`: `CinemaDAO.getCinemayId()` returns `null` →
`CinemaService.getCinemaDetails(Cinema, boolean)` calls `cinema.getCinemaId()` →
`NullPointerException` → Spring's default error handler → **HTTP 500** with a stack trace.

Wrong in three ways: the status is a server error when the client asked for something that
does not exist; the message is useless; and the stack trace leaks internals.

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String type, Object id) {
        super(type + " not found: " + id);
    }
}
```

```java
public CinemaDetail getCinemaDetails(int cinemaId, boolean withSeats) {
    Cinema cinema = cinemaDAO.getCinemayId(cinemaId);
    if (cinema == null) throw new ResourceNotFoundException("Cinema", cinemaId);
    return getCinemaDetails(cinema, withSeats);
}
```

**Why unchecked, and why thrown from the service?** Unchecked so intermediate layers do not
have to declare an exception they cannot handle (chapter 06). Thrown from the service
because that is the layer that knows a missing row is a business-meaningful condition — the
DAO only knows it got no rows, and the controller should not be doing lookups.

`Optional<Cinema>` as a DAO return type is a defensible alternative — it makes "may be
absent" part of the signature. Either is fine; a `null` that callers forget to check is not.

---

## Exercise 5 — Fix the seat map

**Bug 1** — use the path variable:

```java
ScheduledLiveShow timing = scheduledLiveShowDAO.getScheduledLiveShowById(timingId);
if (timing == null || timing.getLiveShowId() != /* the hall's live show */) {
    throw new ResourceNotFoundException("Showtime", timingId);
}
```

Note the second half: verify the showtime actually belongs to the requested hall. Same
IDOR-prevention principle as `HallDAO.getHallBy(cinemaId, hallId)` (chapter 02). Accepting a
parent ID and never checking the child belongs to it is the bug class to watch for.

**Bug 2** — return all seats with real statuses:

```java
public List<SeatDetails> getSeatMap(int hallId, int scheduledLiveShowId) {
    List<Seat> allSeats = seatDAO.getAllSeatList(hallId);
    Map<Integer, SeatStatus> statusBySeatId = seatBookingDAO.statusForShow(scheduledLiveShowId);

    return allSeats.stream()
            .map(seat -> SeatDetails.builder()
                    .seatId(seat.getSeatId())
                    .seatCode(seat.getSeatCode())
                    .seatRowLocation(seat.getSeatRowLoc())
                    .seatColLocation(seat.getSeatColLoc())
                    .seatStatus(statusBySeatId.getOrDefault(seat.getSeatId(), SeatStatus.AVAILABLE))
                    .build())
            .collect(toList());
}
```

**Two queries total**, regardless of seat count — all seats, all bookings for the showtime,
joined in memory with a map. If you wrote a per-seat lookup, you built a 300-query endpoint;
that is the N+1 instinct and it is worth catching in yourself.

`statusForShow` should reuse the single availability rule from Exercise 17 — do that
exercise first if you have not.

---

## Exercise 6 — Make it runnable from anywhere

```java
// ConnectionFactory
Reader mybatisConfig = Resources.getResourceAsReader("mybatisConfig.xml");   // org.apache.ibatis.io.Resources
Properties dbProperties = new Properties();
dbProperties.load(Resources.getResourceAsStream("db.properties"));           // load BEFORE reading
String environmentType = dbProperties.getProperty(ENVIRONMENT_TYPE);         // ← now non-null
```

Note this also fixes the initialisation-ordering bug from chapter 07 as a side effect —
load, then read. Add `log.info("MyBatis environment: {}", environmentType)` so the value is
visible at startup; silently-ignored config is what made that bug invisible.

`MovieBooking.sql` is no longer read at all — Flyway replaced it — so only these two remain.

**The real answer, though, is Exercise 15** — with `mybatis-spring-boot-starter` there is no
`mybatisConfig.xml`, no `db.properties`, and no resource loading to get right.

---

## Exercise 7 — Fix the latent crash

**What the merge is supposed to do:** given several `LiveShow`s for one movie, group them by
cinema so each cinema appears once with all its halls.

The existing nested loops are wrong twice over. Rewrite around the grouping intent rather
than patching the loops:

```java
private Collection<CinemaDetail> getCinemaDetailsForShow(List<LiveShow> liveShows, boolean withSeats) {
    Map<Integer, CinemaDetail> byCinemaId = new LinkedHashMap<>();

    for (LiveShow liveShow : liveShows) {
        CinemaDetail incoming = getCinemaDetailsByShow(liveShow, withSeats);
        if (incoming == null) continue;                    // getCinemaDetailsByShow can return null

        byCinemaId.merge(incoming.getCinemaId(), incoming, (existing, added) -> {
            List<HallDetail> halls = new ArrayList<>(existing.getHallDetails());
            Set<Integer> seenHallIds = halls.stream().map(HallDetail::getHallId).collect(toSet());
            added.getHallDetails().stream()
                 .filter(h -> seenHallIds.add(h.getHallId()))       // dedupe by hall id
                 .forEach(halls::add);
            return CinemaDetail.builder()
                    .cinemaId(existing.getCinemaId())
                    .cinemaName(existing.getCinemaName())
                    .hallDetails(halls)
                    .build();
        });
    }
    return byCinemaId.values();
}
```

**Why build a new object instead of mutating?** Because `@Singular` made the list immutable
and you should leave it that way — immutability is a feature, not the obstacle. Deleting
`addHallDetail()` entirely is the right call; a mutator on an immutable object is a trap for
the next reader.

Also note `LinkedHashMap` rather than `HashMap`: it preserves insertion order, so the
response is deterministic. Non-deterministic response ordering makes tests flaky and
confuses clients.

**The test that reproduces it:** two `LiveShow` rows for the same `showId` whose halls
belong to the same cinema. Before the fix, `UnsupportedOperationException`.

---

## Exercise 8 — Validate input

Add `spring-boot-starter-validation`, then annotate:

```java
@Data
public class BookingRequest {
    @NotNull @Positive               private Integer customerId;
    @NotNull @Positive               private Integer scheduledLiveShowId;
    @NotEmpty @Size(max = 10)
    private List<@NotNull @Positive Integer> seatIdList;
}
```

**Note `Integer` rather than `int`.** A primitive `int` cannot be null — Jackson defaults a
missing field to `0`, and `@NotNull` can never fire. Using the wrapper is what makes
"absent" distinguishable from "zero". This catches people constantly.

`@Valid` on the parameter, and a handler for `MethodArgumentNotValidException` (Exercise 10)
to turn violations into a `400` with field-level detail.

**Duplicate seat IDs** are not covered by these annotations — that needs a custom validator
or a check in the service. Worth noticing that Bean Validation handles shape, not semantics.

---

## Exercise 9 — Fix duplicate registration

```sql
-- V4__customer_unique_email.sql   (V1-V3 already exist; never reuse or edit a version)
ALTER TABLE TBL_Customer DROP INDEX TBL_Customer_uk_1;
ALTER TABLE TBL_Customer ADD UNIQUE KEY TBL_Customer_email_uk (email);
```

(On a real database with existing data you must deduplicate first, or the `ALTER` fails —
worth thinking about how you would choose which duplicate account survives.)

```java
public void register(AddCustomerRequest request) {
    if (customerDAO.findByEmail(request.getEmail()).isPresent()) {
        throw new CustomerException(CustomerError.EMAIL_ALREADY_REGISTERED);
    }
    try {
        customerDAO.insert(...);
    } catch (DuplicateKeyException e) {           // lost the race between check and insert
        throw new CustomerException(CustomerError.EMAIL_ALREADY_REGISTERED);
    }
}
```

**Why both?** The application check gives a clean, fast, friendly error in the normal case.
The constraint guarantees correctness in the racing case — because the check-then-insert
here is the same TOCTOU shape as the booking bug in chapter 08, just with lower stakes.

> **The general pattern, worth memorising: check for a good error message, constrain for
> correctness, and catch the constraint violation to convert it back into the good error
> message.** You need all three.

---

## Exercise 10 — Global exception handling

```java
@Getter
public enum BookingError {
    SEAT_UNAVAILABLE(HttpStatus.CONFLICT),
    SESSION_EXPIRED(HttpStatus.CONFLICT),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND);

    private final HttpStatus status;
    BookingError(HttpStatus status) { this.status = status; }
}

public class BookingException extends RuntimeException {      // now unchecked
    private final BookingError error;
    public BookingException(BookingError error, String detail) { super(detail); this.error = error; }
    public BookingError getError() { return error; }
}
```

```java
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ApiError> onBooking(BookingException e) {
        return ResponseEntity.status(e.getError().getStatus())
                             .body(new ApiError(e.getError().name(), e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> onNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(new ApiError("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage()).collect(joining(", "));
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION_FAILED", detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onUnexpected(Exception e) {
        String traceId = MDC.get("traceId");
        log.error("Unhandled exception [traceId={}]", traceId, e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", "Something went wrong. Reference: " + traceId));
    }
}
```

Controllers collapse to:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public BookingDetails initiateBooking(@Valid @RequestBody BookingRequest request) {
    return bookingService.initiateBooking(request);
}
```

Four lines instead of fifteen, and no error-handling code at all. **That contraction is the
measure of whether the refactor worked.**

**Making `BookingException` unchecked was necessary**, not incidental: as a checked
exception, every intermediate method must declare it, and Spring's default rollback rules
would have *committed* on it (chapter 07).

---

## Exercise 11 — Hash passwords

```java
private final PasswordEncoder encoder = new BCryptPasswordEncoder();

// register
customer.setPassword(encoder.encode(rawPassword));

// login
Optional<Customer> found = customerDAO.findByEmail(email);
if (found.isPresent() && encoder.matches(rawPassword, found.get().getPassword())) { ... }
```

**Why you cannot query by password any more:** BCrypt generates a random salt per hash and
embeds it in the output, so the same password hashed twice produces two different strings.
`WHERE password = ?` can never match. You *must* fetch by email and verify in code. That is
the salt doing its job — it means an attacker with the hash table cannot use a precomputed
rainbow table, and cannot tell that two users share a password.

**Migrating existing users** — you cannot hash what you do not have in reversible form, and
forcing a global password reset is hostile. The standard technique is **lazy rehashing**:

1. Add a `password_version` column (1 = plaintext legacy, 2 = bcrypt).
2. On login, if version 1, compare in plaintext; if it matches, immediately rehash with
   bcrypt, store it, set version 2.
3. After a deadline, force a reset for anyone still on version 1.

Users upgrade transparently as they log in, and you keep a shrinking, countable population
of legacy accounts. This "migrate on access" pattern generalises to many data migrations.

---

## Exercise 12 — Unguessable booking references

```java
public static String newBookingReference() {
    byte[] bytes = new byte[16];
    SECURE_RANDOM.nextBytes(bytes);                                    // static SecureRandom
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);   // ~22 chars, fits varchar(32)
}
```

Or simply `UUID.randomUUID().toString()` — 36 characters, also fits, and instantly
recognisable as a random identifier.

**Why `SecureRandom`:** `java.util.Random` is a 48-bit linear congruential generator. Given
a couple of outputs, its entire future sequence is computable. For a value that acts as a
bearer credential — and `GET /booking/{ref}` has no authentication, so it *is* one —
predictable means anyone can enumerate other people's tickets.

**Collisions:** the column is `UNIQUE`, so catch `DuplicateKeyException` and retry a couple
of times before failing. At 128 bits the probability is negligible, but "negligible" handled
by a two-line retry beats "negligible" handled by an HTTP 500.

---

## Exercise 13 — Fix money handling

**The failing test:**

```java
@Test
public void roundTripsPaiseWithoutLoss() {
    // 250.99 → should be 25099 paisa, not 25098
    assertEquals(25099, AmountTypeHandler.toPaise(250.99));
}
```

`(int)(250.99 * 100)` is `25098` because `250.99 * 100` evaluates to `25098.999999999996`
in IEEE-754 and the cast truncates.

**The patch-level fix:**

```java
public class AmountTypeHandler extends BaseTypeHandler<Double> {   // handles nulls for you

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Double value, JdbcType t) throws SQLException {
        ps.setInt(i, (int) Math.round(value * 100));               // round, don't truncate
    }

    @Override
    public Double getNullableResult(ResultSet rs, String column) throws SQLException {
        int paise = rs.getInt(column);                             // not getString + parseInt
        return rs.wasNull() ? null : paise / 100.0;
    }
    // ... the other two getNullableResult overloads
}
```

**The real fix is to delete the type handler.** Keep money as a `long` of paisa from the
database through the service layer, and format to rupees only at the API boundary — or emit
paisa in the JSON and let the client format. Then no floating-point value exists anywhere in
the money path and the whole class of bug is gone by construction.

> **The general principle: fix bugs by removing the possibility, not by handling the case.**
> Rounding correctly fixes today's bug; not using `double` fixes every future one.

---

## Exercise 14 — Remove the boolean traps

`getShowDetailsList(boolean)` → `getLiveShows()` and `getRecentShows()`, as in chapter 10.

**For `shouldShowSeatDetails`, either answer is defensible** — what matters is the
reasoning:

**Split into two methods** if you conclude the seat-map path should not share code with the
listing path at all. This is likely correct: after Exercise 5, the seat-map query has a
different shape (all seats + a status map) from the listing query, so the two paths diverge
anyway. Sharing them was the mistake.

**Introduce a `ShowDetailLevel` enum** if you conclude a third variant is coming — an admin
view showing who holds each seat, for example — and you want adding it to be additive.

**What is definitely wrong** is leaving a boolean threaded through six methods. Anything is
better than that.

---

## Exercise 15 — Unify the database stack

```java
@SpringBootApplication
@MapperScan("com.project.bookmyshow.db.mappers")
public class BookMyShowApplication { ... }
```

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/movie_booking?serverTimezone=UTC}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}
    hikari:
      maximum-pool-size: 20
      connection-timeout: 3000
mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

```java
@Repository
@RequiredArgsConstructor
public class HallDAO {
    private final HallMapper hallMapper;

    public Hall getHallById(int hallId) {
        return hallMapper.selectByPrimaryKey(hallId);
    }
}
```

Every `@Cleanup SqlSession` and `sqlSession.commit()` disappears. So do `ConnectionFactory`,
`mybatisConfig.xml`, and `db.properties`.

**Proving `@Transactional` actually works** — the step most people skip:

```java
@Test
@Transactional
void addShowRollsBackEntirelyOnFailure() {
    long before = countRows("TBL_Show");
    doThrow(new RuntimeException("boom")).when(liveShowDAO).insertOrUpdateLiveShow(anyInt(), anyInt());

    assertThrows(RuntimeException.class, () -> showService.addNewShowToDB(request));

    assertEquals("the Show insert must have rolled back", before, countRows("TBL_Show"));
}
```

If that assertion fails, `@Transactional` is not taking effect. The usual causes, in order:
self-invocation through `this` (the proxy is bypassed), a checked exception (default rules
commit), or a non-public method.

**Pool sizing:** `maximum-pool-size: 20` is a reasonable start. Bigger is not better — a
pool larger than the database can usefully serve just moves the queue from your application
into the database, where it is harder to see and affects everyone. Size it from measurement,
and remember the pool is *per instance*: 10 instances × 20 = 200 connections against MySQL's
default limit of 151.

---

## Exercise 16 — Understand the fix that was made

**Why `NOT IN (INPROGRESS, SUCCESS)` inverted the method.** The clause ran in the *SQL*,
removing confirmed and in-flight rows from the result set. The Java loop then examined only
what survived — which could only be `FAILED` or `INITIATED` rows — and concluded "no live
hold found, therefore available". A paid seat was excluded from the evidence and so was
never counted against availability. The author meant "ignore the ones I already know are
taken"; the code meant "pretend they do not exist".

**Why `occupiesSeat` throws on an unknown status.** Because the alternative default for a
seat availability check is "free", and being wrong in that direction sells a seat twice.
The original `switch` in `getBookedSeats` had no `default` and simply fell through, so a
status added later — `REFUNDED`, `CANCELLED`, anything — would have been silently treated as
available. **Choose your failure direction deliberately: for an availability check, refuse
to answer rather than guess permissively.**

**Why throwing mid-loop is safe.** Follow the session:

```java
// ShowBookingDAO.initiateBooking
@Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
ShowBooking showBooking = insertBookingEntry(bookingRequest, ticketPrice, sqlSession);
int seatEntries = seatBookingDAO.insertSeatsForBooking(..., sqlSession);   // may throw
sqlSession.commit();                                                        // never reached
```

MyBatis' `openSession()` does not auto-commit. If `insertSeatsForBooking` throws, `commit()`
is skipped, `@Cleanup` calls `close()`, and closing an uncommitted session **rolls back** —
taking the `TBL_ShowBooking` row with it. So a partial claim leaves nothing behind.

Note this is correct *by accident of layering* rather than by declaration: there is no
`@Transactional`, just one session threaded through two calls. Exercise 15 makes it
explicit.

---

## Exercise 17 — Prove the rule is not duplicated again

**The audit.** All of `areSeatsAvailable`, `getBookedSeats`, and `insertSeatsForBooking` now
delegate to `BookingUtils.occupiesSeat`. `areSeatsAvailable` is a one-liner over
`getOccupiedSeatIds`.

**The `SeatDAO.getAvailableSeatList` route is the interesting part.** It goes:

```java
List<Integer> showBookingIds = showBookingDAO.getShowBookingForShow(scheduledShowId);
Set<Integer> acquiredSeatIds = seatBookingDAO.getBookedSeats(showBookingIds);
```

Two queries and an intermediate list of booking IDs, to answer the same question that
`getOccupiedSeatIds(seatIds, scheduledLiveShowId)` answers in one. They agree today because
both call `occupiesSeat` — but they are two code paths where one would do, and the extra
path is a place a future divergence can hide. Collapsing `getAvailableSeatList` onto
`getOccupiedSeatIds` is the right call, and it also removes a query.

**A test that catches a third copy** is really an architecture test. The pragmatic version:

```java
@Test
public void seatOccupancyRuleLivesInExactlyOnePlace() throws IOException {
    long implementations = Files.walk(Paths.get("src/main/java"))
            .filter(p -> p.toString().endsWith(".java"))
            .filter(p -> !p.endsWith("BookingUtils.java"))
            .map(this::readSafely)
            .filter(src -> src.contains("StatusConstant.INITIATED")
                        && src.contains("StatusConstant.SUCCESS"))
            .count();
    assertEquals("status logic outside BookingUtils", 0, implementations);
}
```

Crude, and it will need adjusting as the code moves — but it fails loudly the day someone
pastes the rule somewhere new, which is exactly when you want to hear about it. (ArchUnit
does this properly if the idea appeals.)

**On the richer return type:** yes. Exercise 5's seat map needs to distinguish `BOOKED` from
`ON_HOLD` from `AVAILABLE`, and a boolean cannot. The natural evolution is for the shared
method to return the `SeatStatus` rather than a yes/no, with `occupiesSeat` becoming
`status != AVAILABLE`. Do Exercise 5 and 17 together and let the seat map drive the shape.

---

## Exercise 18 — Concurrency

Note this is genuinely unsolved in the code you have — the availability *rule* was fixed,
the *race* was not.

**Recommended: the conditional update (chapter 08, Option 1).**

```java
int claimed = seatsBookingMapper.claimSeat(
        seatId, scheduledLiveShowId, newBookingId, holdCutoff);
if (claimed == 0) {
    throw new BookingException(SEAT_UNAVAILABLE, "Seat " + seatId + " was just taken");
}
```

```sql
-- claimSeat
UPDATE TBL_SeatsBooking
   SET booking_id = #{bookingId}, seat_booking_status = 4, modified_at = NOW()
 WHERE seat_id = #{seatId}
   AND scheduled_live_show_id = #{showId}
   AND (seat_booking_status = 2                                    -- FAILED
        OR (seat_booking_status = 4 AND modified_at < #{holdCutoff}))  -- lapsed hold
```

Plus a plain `INSERT` for the no-row-yet case, catching `DuplicateKeyException` when another
thread inserts first. All of it inside one `@Transactional` method (Exercise 15) so the
booking and its seats commit atomically.

**Why the affected-row count is the whole trick:** MySQL evaluates the `WHERE` clause and
performs the write as one atomic operation, holding a row lock for its duration. Two racing
transactions cannot both see the row in the claimable state. `claimed == 1` means you won;
`claimed == 0` means someone else did. **No application-level lock exists anywhere, and it
is correct across any number of instances** — because the arbitration happens inside the one
component every instance shares.

**Justifying it over the alternatives** — the reasoning your write-up should contain:

- vs `synchronized` — does not work across processes, and serialises unrelated bookings.
- vs `SELECT ... FOR UPDATE` — correct, but holds locks longer, blocks rather than failing
  fast, and needs deadlock handling. More machinery for no additional guarantee.
- vs optimistic locking — degrades badly under exactly the contention this system sees on
  opening day.
- vs Redis — a new dependency, a new failure mode, and no guarantee the database was not
  already providing.

**Measuring:** with `synchronized` removed and per-seat arbitration in the database,
bookings for *different* showtimes now run fully in parallel. On a multi-core machine with a
connection pool, expect a large throughput increase — and note that the improvement comes
from removing a lock, not from adding one.

---

## Exercise 19 — Kill the N+1

**Batched approach** (easier to retrofit, and often enough):

```java
List<Cinema> cinemas = cinemaDAO.getOpenCinemas(page);                       // 1
List<Hall> halls = hallDAO.findByCinemaIds(idsOf(cinemas));                  // 2
List<LiveShow> liveShows = liveShowDAO.findByHallIds(idsOf(halls));          // 3
List<ScheduledLiveShow> timings = scheduledDAO.findByLiveShowIds(idsOf(liveShows));  // 4

Map<Integer, List<Hall>> hallsByCinema = halls.stream().collect(groupingBy(Hall::getCinemaId));
// ... assemble in memory
```

**Four queries, regardless of catalogue size.** From 1+N+N·M+N·M·P to a constant.

**Join approach** (one query, best performance, more mapping work): a single `SELECT` with
`LEFT JOIN`s and a MyBatis nested `<collection>` result map.

**Use `LEFT JOIN`, not `JOIN`.** With an inner join, a cinema that has no showtimes vanishes
from the response entirely — so a newly-added cinema is invisible until someone schedules a
film. That is a real bug, and it is the kind that gets shipped because the test data always
has showtimes.

**Guard the empty `IN` list.** `WHERE cinema_id IN ()` is invalid SQL. If `cinemas` is empty,
return early rather than issuing the query. (This is the same latent issue flagged in
chapter 07 for `getBookedSeats`.)

**Pagination:**

```java
@GetMapping
public PagedResponse<CinemaDetail> getCinemas(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { ... }
```

The `@Max(100)` matters: without an upper bound, a client sends `size=1000000` and you are
back where you started.

---

## Exercise 20 — Make it deployable

Covered in detail in chapters 01, 11, and 12. The checklist:

1. `spring-boot-maven-plugin` replaces `maven-shade-plugin`.
2. `application.yaml` and `db.properties` already read `${DB_*}` env vars, but still carry
   working defaults. Remove the password default so a misconfigured deploy fails loudly,
   and do the same for `generatorConfig.xml`.
3. `generatorConfig.xml`: `targetProject="${project.basedir}/src/main/java"`.
4. Actuator with `management.endpoints.web.exposure.include=health,info`.
5. `server.shutdown=graceful`.
6. systemd unit — non-root user, `EnvironmentFile`, `Restart=on-failure`.
7. Dockerfile — non-root user, `MaxRAMPercentage`, no secrets baked into the image.

**The verification is the exercise.** If you can `docker run` it with configuration supplied
entirely through environment variables, and `/actuator/health` returns `UP`, you have a
genuinely deployable artifact. If any step needed you to be in a particular directory or
edit a file inside the image, you do not.

---

## Group E

No answers — these are open-ended design work, and the value is in your reasoning and your
write-up. Bring them to your mentor and defend your choices.

If you want a hint on the hardest one (**Exercise 21**, the sweeper/payment race): the
sweeper must not blindly `UPDATE ... SET status = FAILED WHERE status = INITIATED AND
modified_at < cutoff`, because a payment may have moved that row to `INPROGRESS` a
microsecond earlier. The conditional-update pattern from Exercise 18 applies here too — and
so does the question of whether the payment path should re-verify the hold *inside* its own
transaction rather than before it.
