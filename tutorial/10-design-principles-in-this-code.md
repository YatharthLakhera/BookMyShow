# 10 — Design Principles, Applied to This Code

You asked to learn about the open–closed principle and "when to use them". This chapter
answers that using only code from this repository. No shapes, no animals, no
`AbstractVehicleFactory`.

---

## Before the principles: the only rule that matters

> **Principles are tools for managing change. They are not virtues.**

Every design principle costs something — usually an extra interface, an extra layer of
indirection, more files to open before you understand a flow. That cost is worth paying
**where the code changes**, and pure waste where it does not.

The two failure modes are symmetrical, and the second is more common in people who have
just learned SOLID:

- **Under-engineering:** a 200-line method with six boolean flags that three teams are
  afraid to touch.
- **Over-engineering:** four interfaces, a factory, and a strategy pattern, for a rule
  that has never changed and never will.

The practical heuristic used by most experienced engineers:

> **Write the simple thing. When you need to change it a second time in the same place,
> refactor toward the principle that would have made the change easy.** Two occurrences is
> a coincidence; three is a pattern. Do not pay for flexibility you cannot yet name.

Now the principles, each grounded in a specific file.

---

## Open–Closed Principle

> Software entities should be **open for extension, closed for modification** — you should
> be able to add new behaviour without editing existing, working, tested code.

**Why anyone cares:** every time you edit working code, you can break it. Code you never
touch cannot regress. So the goal is to arrange things such that new requirements arrive
as *additions*.

### Where this code violates it: `ApplicationUtils.getStatusString()`

```java
public static String getStatusString(int statusId) {
    String status = null;
    switch (statusId) {
        case StatusConstant.SUCCESS:    status = StatusConstant.getName(StatusConstant.SUCCESS); break;
        case StatusConstant.FAILED:     status = StatusConstant.getName(StatusConstant.FAILED); break;
        case StatusConstant.INPROGRESS: status = StatusConstant.getName(StatusConstant.INPROGRESS); break;
        case StatusConstant.INITIATED:  status = StatusConstant.getName(StatusConstant.INITIATED); break;
    }
    return status;
}
```

Add a status — `REFUNDED`, say, which a real ticketing system needs within a year — and
you must **edit** this method. And this one, in `BookingUtils.occupiesSeat`:

```java
switch (seatsBooking.getSeatBookingStatus()) {
    case StatusConstant.SUCCESS:
    case StatusConstant.INPROGRESS: return true;
    case StatusConstant.INITIATED:  return isSeatOnHold(...);
    case StatusConstant.FAILED:     return false;
    default: throw new IllegalStateException(...);
}
```

And the `switch` in `BookingService.finalizeBooking()`. Three places, all of which must
change together, and **nothing tells you if you miss one** — an unmatched `int` simply
falls through. Note `occupiesSeat` at least *throws* on an unknown status (chapter 08); the
other two still fail silently, `getStatusString` by returning `null`.

### The fix: let the type carry the behaviour

```java
public enum BookingStatus {
    SUCCESS(1, "SUCCESS")       { public boolean occupiesSeat() { return true; } },
    FAILED(2, "FAILED")         { public boolean occupiesSeat() { return false; } },
    INPROGRESS(3, "INPROGRESS") { public boolean occupiesSeat() { return true; } },
    INITIATED(4, "INITIATED")   { public boolean occupiesSeat(Duration age) { return age.toMillis() < HOLD_MS; } };

    private final int id;
    private final String label;

    public abstract boolean occupiesSeat();
}
```

Now adding `REFUNDED` means adding **one enum constant**, and the compiler *forces* you to
implement `occupiesSeat()` for it. You cannot forget. And `getStatusString` collapses to
`status.getLabel()`.

> **This is the everyday form of OCP, and it is the one worth internalising:** *when you
> find yourself switching on a type code, the behaviour probably belongs on the type.* Not
> "introduce an abstract factory". Just: move the logic to where the data lives.

### The other example: `@ControllerAdvice`

Today, supporting a new error means editing every controller's `try/catch`. With a global
exception handler, it means adding one `@ExceptionHandler` method to one class. Existing
controllers are never touched.

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ApiError> handleBooking(BookingException e) {
        return ResponseEntity.status(e.getError().getHttpStatus())
                             .body(new ApiError(e.getError().name(), e.getMessage()));
    }

    @ExceptionHandler(CustomerException.class)      // ← adding a case = adding a method
    public ResponseEntity<ApiError> handleCustomer(CustomerException e) { ... }
}
```

**Open for extension** (new handler methods), **closed for modification** (controllers
never change). Exercise 10.

### And where OCP would be a mistake here

`PaymentService` is a mock with one method. Do **not** build a `PaymentStrategyFactory`
with a registry of providers for it. When a second payment provider actually arrives,
extract a `PaymentGateway` interface then. Chapter 06 describes that refactor — note it is
justified by a *concrete* need (testability today, a real provider tomorrow), not by
principle alone.

---

## Single Responsibility Principle

> A class should have one reason to change.

"One reason to change" is the useful phrasing. Not "one method", not "few lines" — one
*source of change*.

### `BookMyShowApplication` has four reasons to change

```java
public class BookMyShowApplication implements CommandLineRunner {
    public static void main(...)  { start Spring; initialise MyBatis }
    public void run(...)          { drop and recreate the database schema }
}
```

It changed if: Spring startup changed, MyBatis config changed, the schema changed, or the
seed data changed. Four unrelated teams' worth of concerns in 44 lines.

Two of the four have since been split out — Flyway owns the schema, a profile-guarded
migration owns the seed data — and the class is down to 26 lines. **The remaining one is
still a violation**: `main()` both starts Spring and hand-initialises MyBatis afterwards,
which is why chapter 06 calls the ordering a bug. Moving that into a `@Configuration` bean
finishes the job.

### `ShowBookingDAO` has two

It does persistence (`selectByExample`, `insertSelective`) *and* business rules (deciding
availability, computing `ticketPrice × seatCount`, throwing `BookingException`). It changes
when the schema changes **and** when the booking rules change. Chapter 06 covers the fix:
push the rules up to `BookingService`.

> **Do not take SRP too far.** "One reason to change" is a judgement call, and applied
> literally it produces a thousand single-method classes that are individually trivial and
> collectively incomprehensible. The signal to act is real: *when two kinds of change keep
> touching the same file and interfering with each other.* If nobody is stepping on anyone,
> leave it alone.

---

## Dependency Inversion / Dependency Injection

> Depend on abstractions, not concretions. High-level policy should not depend on low-level
> detail.

The point is **not** "avoid `new`". The point is: *the caller should not be able to see
which implementation it got*, because what it cannot see, it cannot depend on — and what it
cannot depend on, you can replace.

### The three violations in this codebase

**1. Static call:**
```java
PaymentService.Status status = PaymentService.processPayment();   // BookingService:73
```

**2. `new` inside a method:**
```java
SeatBookingDAO seatBookingDAO = new SeatBookingDAO();             // ShowBookingDAO:49
```

**3. Static singleton:**
```java
@Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();   // every DAO
```

All three have the same consequence: **there is no seam**. A test cannot substitute a
fake, because the dependency is hardcoded inside the method body. This is precisely why
chapter 13 concludes that nothing below `CustomerService` in this project is unit-testable.

### Field injection vs constructor injection

This codebase uses field injection everywhere:

```java
@Autowired
private BookingService bookingService;
```

The modern recommendation is constructor injection:

```java
private final BookingService bookingService;

public CustomerService(BookingService bookingService) {
    this.bookingService = bookingService;
}
// With Lombok: @RequiredArgsConstructor on the class, and `private final` fields.
```

Four concrete reasons, in order of importance:

1. **The field can be `final`** — genuinely immutable after construction, and therefore
   thread-safe with no reasoning required.
2. **You cannot construct an incompletely-wired object.** With field injection,
   `new CustomerService()` compiles and gives you an object whose fields are all `null`.
3. **Tests need no framework.** `new CustomerService(mockDao)` — no Spring context, no
   reflection, no `@InjectMocks`. (Compare with `BookMyShowTestApplication`, which needs
   Mockito's reflection machinery purely because the fields are private and unsettable.)
4. **It makes bad design visible.** A constructor with nine parameters is uncomfortable to
   look at, and that discomfort is *information*: the class has too many
   responsibilities. Field injection hides it — `BookingService` has **eight** `@Autowired`
   DAOs and looks perfectly tidy. It is not tidy; it is a class that reaches into eight
   tables.

---

## Interface Segregation

> No client should be forced to depend on methods it does not use.

`models/APIResponse.java` is an empty marker interface implemented by six DTOs. It is not
*over*-broad — it is empty, which is the opposite problem: it segregates so thoroughly that
it conveys no information. A method returning `APIResponse` tells the caller nothing about
what it can do with the result, which is why the controllers are full of casts, raw types,
and `@SuppressWarnings("unchecked")`.

> **`@SuppressWarnings` is a design signal.** The compiler noticed something genuinely
> unsound and you told it to be quiet. Occasionally that is correct. Usually it means the
> types are wrong. Four occurrences in four controllers is not four unlucky coincidences —
> it is the marker-interface design leaking.

Chapter 04 has the alternative: `ApiResponse<T>`.

---

## The Boolean Trap, and the worked refactor you asked for

This deserves the most space because this codebase is saturated with it and because it is
the clearest example of "when to apply a principle".

### The symptom

```java
cinemaService.getCinemaList(false);
cinemaService.getHallDetail(cinemaId, hallId, false);
cinemaService.getScheduledLiveShowByHallId(hallId, true);
showService.getShowDetailsList(true);
```

**What does `true` mean?** You cannot tell from the call site. You must open the method.
`shouldShowSeatDetails` is threaded through six methods in `CinemaService`; `isLive`
through two in `ShowService`.

### Why it is bad, concretely

1. **Unreadable at the call site**, which is where code is actually read.
2. **It hides two different behaviours in one method**, so the method has two reasons to
   change (SRP) and every caller pays for a branch it does not use.
3. **Booleans multiply.** Add a second flag and you have four paths through one method,
   most of which no test covers.
4. **`getShowDetailsList(boolean)`'s two branches share almost nothing:**

```java
if (isLive && liveShowDAO.isShowLive(show.getShowId())) {
    showDetails.add(showDetail);
} else if (!isLive && currentDate.before(show.getCreatedAt())) {
    showDetails.add(showDetail);
}
```

One branch queries a table; the other compares dates. These are two different features
sharing a method body because they both happen to return a list.

### Refactor A — just split it (do this first)

```java
public List<ShowDetail> getLiveShows() {
    return showDAO.getAllShow().stream()
            .filter(show -> liveShowDAO.isShowLive(show.getShowId()))
            .map(this::getShowDetails)
            .collect(toList());
}

public List<ShowDetail> getRecentShows() {
    Date cutoff = DateUtils.addMonths(new Date(), -AppConstants.DISPLAY_SHOW_FOR_MONTHS);
    return showDAO.getAllShow().stream()
            .filter(show -> cutoff.before(show.getCreatedAt()))
            .map(this::getShowDetails)
            .collect(toList());
}
```

Call sites become `showService.getLiveShows()` and `showService.getRecentShows()`. Each
method does one thing, each is independently testable, and neither has a branch.

> **This is the right answer 90% of the time, and it is worth stating plainly: the fix for
> a boolean parameter is usually two methods, not a design pattern.** Notice we applied
> *no* principle by name. We just removed a flag. Most good refactoring looks like this.

### Refactor B — when splitting is not enough

`shouldShowSeatDetails` is harder, because the flag is threaded through six nested calls
and splitting each into two would give you twelve methods.

The signal that you need something more is: **the variation is not one branch, it is a
whole strategy that varies together.** Then introduce a type for it:

```java
public enum ShowDetailLevel {
    SUMMARY {
        List<SeatDetails> seats(SeatDAO dao, ScheduledLiveShow s, int hallId) { return null; }
    },
    WITH_SEATS {
        List<SeatDetails> seats(SeatDAO dao, ScheduledLiveShow s, int hallId) {
            return toSeatDetails(dao.getAvailableSeatList(hallId, s.getScheduledLiveShowId()));
        }
    };
    abstract List<SeatDetails> seats(SeatDAO dao, ScheduledLiveShow s, int hallId);
}
```

Call sites now read `getHallDetail(cinemaId, hallId, SUMMARY)` — self-documenting. And when
someone inevitably asks for a third level ("seat map *including* booked seats, for the
admin view"), you add an enum constant rather than a second boolean. That is OCP earning
its keep.

> **When to reach for B instead of A:** when the flag appears in more than two or three
> methods, or when you can already name a likely third variant. Otherwise A. Do not build
> the enum on the first boolean.

---

## DRY, and its opposite failure

Chapter 08's headline bug was the same rule — "is this seat available?" — implemented
twice, differently, in `areSeatsAvailable()` and `getBookedSeats()`, so the system gave
different answers depending on which path a request took. Both now delegate to a single
`BookingUtils.occupiesSeat`. That is what DRY is actually about: **not typing less, but
having one place where a rule lives, so it cannot contradict itself.**

Other duplications here worth consolidating: the `isCustomerRegistered` preamble in four
controllers, and the `@Cleanup SqlSession` + `getMapper` boilerplate in ~40 DAO methods.

> **But do not over-apply DRY.** Two pieces of code that *look* similar today but exist for
> different reasons will need to change independently tomorrow, and merging them creates a
> method with a flag to distinguish them — which is exactly the boolean trap you just
> learned to remove.
>
> The test is not "do these look alike?" It is **"would a change to the business rule
> require changing both?"** If yes, unify. If no, leave them alone. Coincidental
> similarity is not duplication. Premature DRY produces worse code than a little
> repetition, and it is much harder to undo.

---

## A practical checklist for your own code

Before opening a pull request:

- [ ] Can I name what this class is responsible for, in one sentence with no "and"?
- [ ] Does any method take a boolean that a reader would have to look up?
- [ ] Is any business rule implemented in more than one place?
- [ ] Can I unit-test this without a database, an HTTP server, or a sleep?
- [ ] If a new status / provider / error type arrives, do I edit existing code or add new?
- [ ] Have I suppressed a compiler warning, and if so, is the design wrong?
- [ ] Have I added an abstraction for a variation that does not exist yet?

That last one matters as much as the rest. **Speculative flexibility is a cost you pay
today for a benefit you may never receive**, and it is the most common way that people who
have just read a design-patterns book make a codebase worse.

---

## Checkpoint

- [ ] Explain OCP using `getStatusString` and say what adding `REFUNDED` costs today
- [ ] Give the four reasons to prefer constructor injection
- [ ] Explain why `@SuppressWarnings("unchecked")` in the controllers is a design signal
- [ ] Refactor `getShowDetailsList(boolean)` into two methods
- [ ] Explain when you would *not* apply a principle, with an example from this repo

Next: [11-production-readiness.md](11-production-readiness.md).
