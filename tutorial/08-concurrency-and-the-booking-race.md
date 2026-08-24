# 08 — Concurrency and the Booking Race

This is the chapter that separates people who can build a booking system from people who
cannot. Read it slowly and do the exercises.

The question it answers: **two customers click "Book" on seat C1A5 at the same
millisecond. What happens?**

---

## Why this is hard at all

In a single-threaded program, this code is obviously correct:

```java
if (seatIsAvailable(seat)) {
    bookSeat(seat);
}
```

In a server, it is obviously wrong, and the reason is that **you are not the only one
running**. Between the `if` and the `bookSeat`, an unbounded amount of time can pass —
your thread can be descheduled by the OS, or the JVM can pause for garbage collection —
and during that gap another thread can run the same `if`, get the same answer, and book
the same seat.

This shape has a name: **TOCTOU** — Time Of Check To Time Of Use. The check was true when
you looked. By the time you acted, it was not. Whenever you see "check a condition, then
act on it" against shared state, look for the gap.

```
Time ──────────────────────────────────────────────────────▶

Thread A:   check(seat 5) ──▶ "available" ─────────────▶ INSERT booking A
                                              ╲
Thread B:              check(seat 5) ──▶ "available" ──────────▶ INSERT booking B
                                          ▲
                                          │
                              A has not written yet, so B sees
                              exactly what A saw: available.

Result: two bookings, one seat. Two customers arrive at the cinema with
        valid tickets for the same chair.
```

---

## Where the gap is in this codebase

`db/dao/ShowBookingDAO.java:48-61`:

```java
public ShowBooking initiateBooking(BookingRequest bookingRequest, double ticketPrice) throws BookingException {
    SeatBookingDAO seatBookingDAO = new SeatBookingDAO();

    // ── THE CHECK ─────────────────────────────────────────────
    if (seatBookingDAO.areSeatsAvailable(bookingRequest.getSeatIdList(),
                                         bookingRequest.getScheduledLiveShowId())) {
        //                        ▲ opens its own SqlSession, queries, closes it.
        //                          The transaction is over before we continue.

        // ── THE GAP ───────────────────────────────────────────
        //    No lock is held here. Any other thread may act.

        // ── THE USE ───────────────────────────────────────────
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();  // a NEW session
        ShowBooking showBooking = insertBookingEntry(bookingRequest, ticketPrice, sqlSession);
        int seatEntries = seatBookingDAO.insertSeatsForBooking(
                bookingRequest.getSeatIdList(), showBooking, sqlSession);
        sqlSession.commit();
        return showBooking;
    } else {
        throw new BookingException(ErrorMessages.SEAT_NOT_AVAILABLE);
    }
}
```

The check and the write are **on different database connections, in different
transactions**. Even at the database level there is nothing tying them together. This is
the textbook shape of the bug.

---

## The author's fix, and why it does not survive production

`services/BookingService.java:49`:

```java
public synchronized BookingDetails initateBooking(BookingRequest bookingRequest) throws BookingException {
```

`synchronized` on an instance method acquires the monitor of `this` — and since
`BookingService` is a Spring singleton, `this` is the same object for every request. So
only one thread in the whole JVM can be inside `initateBooking` at a time. The gap is
closed.

**On one server.** Now deploy the way every real service is deployed:

```
                  ┌─────────────┐
   users ────────▶│Load Balancer│
                  └──────┬──────┘
                    ┌────┴────┐
                    ▼         ▼
              ┌─────────┐ ┌─────────┐
              │Instance1│ │Instance2│     ← two JVMs, two separate BookingService
              │  JVM A  │ │  JVM B  │       objects, two independent monitors
              └────┬────┘ └────┬────┘
                   └─────┬─────┘
                         ▼
                  ┌─────────────┐
                  │    MySQL    │          ← the only thing they actually share
                  └─────────────┘
```

> **`synchronized` is a lock inside one JVM's memory. It cannot be seen by any other
> process.** Instance 1's `BookingService` monitor and instance 2's are unrelated objects.
> Both threads enter their own `synchronized` block simultaneously, both check, both see
> "available", both insert.
>
> **The moment you scale from one server to two, this protection silently evaporates.**
> Nothing errors. Nothing logs. You simply start double-booking, and you find out from
> customers standing in a cinema arguing over a seat.

And you *will* run more than one instance. Not because of load — because of availability.
One instance means every deploy is an outage and every crash is an outage. Two is the
minimum for a service anyone depends on. **Any correctness argument that assumes a single
process is not a correctness argument.**

There is a second cost, immediate even on one server:

> **`synchronized` here serialises the entire booking endpoint.** Not per-seat, not
> per-showtime — *globally*. A customer booking in Cinema 4 waits behind a customer
> booking in Cinema 1. And what they wait through is not fast: `initateBooking` performs
> roughly a dozen database round-trips, each opening a fresh UNPOOLED connection
> (chapter 07). If that is 50 ms, your maximum booking throughput is **20 per second for
> the entire company**, no matter how many servers you buy. On ticket-opening day for a
> major release, that is the whole business capped at 20 tps.
>
> **Lock granularity is a design decision.** A lock should cover the smallest scope that
> preserves correctness. The contended resource here is *one seat at one showtime*, so
> that is what should be protected — letting bookings for different showtimes proceed in
> parallel, which is almost all of them.

---

## The bug that was here — and why it did not need two threads

Everything above is about scaling. This next one was broken *without any concurrency at
all*, and it has been fixed. Study it: it is the most instructive defect in the project.

`SeatBookingDAO.areSeatsAvailable` used to read:

```java
public boolean areSeatsAvailable(List<Integer> seatIds, int scheduledLiveShowId) {
    List<Integer> seatStatus = Arrays.asList(StatusConstant.INPROGRESS, StatusConstant.SUCCESS);
    ...
    List<SeatsBooking> seatsBookings = seatsBookingMapper.selectByExample()
            .where(seatsBookingSqlSupport.seatId, SqlBuilder.isIn(seatIds))
            .and(seatsBookingSqlSupport.scheduledLiveShowId, SqlBuilder.isEqualTo(scheduledLiveShowId))
            .and(seatsBookingSqlSupport.seatBookingStatus, SqlBuilder.isNotIn(seatStatus))   // ← here
            .build().execute();

    boolean areSeatsAvailable = true;
    if (!CollectionUtils.isEmpty(seatsBookings)) {
        for (SeatsBooking seatsBooking : seatsBookings) {
            if (BookingUtils.isSeatOnHold(seatsBooking)) {
                areSeatsAvailable = false;
                break;
            }
        }
    }
    return areSeatsAvailable;
}
```

Work out the SQL that produces:

```sql
SELECT * FROM TBL_SeatsBooking
WHERE seat_id IN (1, 2, 3)
  AND scheduled_live_show_id = 1
  AND seat_booking_status NOT IN (3, 1)     -- NOT INPROGRESS, NOT SUCCESS
```

**The query deliberately excluded every seat that was confirmed or being paid for.**

So the returned rows could only have status `FAILED` (2) or `INITIATED` (4). The loop then
checked `isSeatOnHold`, true only for `INITIATED` within five minutes.

Trace a seat that customer A had **paid for** — status `SUCCESS`:

1. The `NOT IN (3, 1)` clause excluded A's row from the result set.
2. The result set was therefore empty for that seat.
3. The loop never saw it.
4. `areSeatsAvailable` returned **`true`**.

**A confirmed, paid-for seat was reported as available.**

> **The lesson is about the shape of the mistake, not the typo.** The author was trying to
> express "ignore the rows I already know are taken, then check the rest for live holds" —
> and the negation inverted the meaning of the whole method. **Conditions expressed as
> exclusions are much harder to reason about than conditions expressed positively.** The
> fixed version asks the question the business actually asks: *which of these seats is
> taken?*

### It got worse: the insert then overwrote the paid booking

You might hope the database would save you. Chapter 03 pointed out the constraint:

```sql
UNIQUE KEY `TBL_SeatsBooking_uk_1` (`seat_id`, `scheduled_live_show_id`)
```

A second `INSERT` for the same seat and showtime *would* be rejected. But
`SeatBookingDAO.insertSeatsForBooking()` sidestepped it:

```java
SeatsBooking seatsBooking = getSeatIdsForBooking(showBooking.getScheduledLiveShowId(), seatId);
if (seatsBooking == null) {
    ...insertSelective(seatsBooking);
} else {
    // a row already exists → UPDATE IT to point at the new booking
    seatsBooking.setBookingId(showBooking.getBookingId());          // ← reassigns the seat
    seatsBooking.setSeatBookingStatus(StatusConstant.INITIATED);    // ← resets the status
    seatsBooking.setModifiedAt(new Date());
    ...updateByPrimaryKeySelective(seatsBooking);
}
```

By turning the insert into an update, the code converted a safe duplicate-key rejection
into a silent overwrite. Customer B's booking **took over customer A's seat row**.

The end state:

- Customer A's `TBL_ShowBooking` row still said `SUCCESS`. They had a valid-looking ticket
  and a receipt.
- The `TBL_SeatsBooking` row for their seat now pointed at customer B's booking.
- No exception, no unusual log line, no notification.

**A data-loss bug that silently sold the same seat twice and destroyed the record of who
bought it first.** No amount of `synchronized` would have helped: the logic was wrong
before concurrency was involved.

### How it was fixed

Both halves came down to one idea: **there must be exactly one implementation of "is this
seat taken?"**. It now lives in `utils/BookingUtils.java`:

```java
public static boolean occupiesSeat(@NonNull SeatsBooking seatsBooking, long currentTimeInMilliSeconds) {
    switch (seatsBooking.getSeatBookingStatus()) {
        case StatusConstant.SUCCESS:
        case StatusConstant.INPROGRESS:
            return true;
        case StatusConstant.INITIATED:
            return isSeatOnHold(seatsBooking, currentTimeInMilliSeconds);
        case StatusConstant.FAILED:
            return false;
        default:
            throw new IllegalStateException(
                    "Unknown seat booking status : " + seatsBooking.getSeatBookingStatus());
    }
}
```

Three things worth noticing about that method:

**1. It is positive.** It answers "does this row take the seat?" rather than "which rows
can I ignore?".

**2. It is total.** Every status has an explicit branch. The old `switch` in
`getBookedSeats` simply fell through on anything unrecognised, treating it as *free* —
the dangerous direction for an availability check. The `default` now throws. **When you
cannot classify something, fail loudly rather than guessing permissively.**

**3. It takes the current time as a parameter.** So the boundary can be tested exactly,
without sleeping — see `BookingUtilsTest`, which pins down 4:59, 5:00 and 5:01.

`areSeatsAvailable`, `getBookedSeats` and `insertSeatsForBooking` now all route through it.
`insertSeatsForBooking` reuses an existing row **only** when that row does not occupy the
seat, and otherwise refuses:

```java
} else if (!BookingUtils.occupiesSeat(seatsBooking, curTimeInMillisec)) {
    // failed booking or lapsed hold - safe to reclaim
    ...updateByPrimaryKeySelective(seatsBooking);
} else {
    throw new BookingException(ErrorMessages.SEAT_NOT_AVAILABLE);
}
```

Throwing part way through the loop is safe because the caller commits only after every
seat is claimed — the `@Cleanup` on the session rolls back everything, including the
`TBL_ShowBooking` row inserted moments earlier. Trace that in
`ShowBookingDAO.initiateBooking` and satisfy yourself it holds; it is a good exercise in
reading transaction boundaries.

### Confirm the fix yourself

```bash
# 1. Book and pay for seat 1
REF=$(curl -s -X POST localhost:8080/booking -H 'Content-Type: application/json' \
  -d '{"customerId":1,"scheduledLiveShowId":1,"seatIdList":[1]}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["bookingRefNo"])')

curl -s -X POST localhost:8080/booking/pay -H 'Content-Type: application/json' \
  -d "{\"customerId\":1,\"bookingRefNo\":\"$REF\",\"ticketAmount\":250.0}"
# → "bookingStatus": "SUCCESS"

# 2. Try to book the SAME seat again
curl -s -X POST localhost:8080/booking -H 'Content-Type: application/json' \
  -d '{"customerId":1,"scheduledLiveShowId":1,"seatIdList":[1]}'
# → {"errorMessage":"Some or all seats are occupied, please refresh and try again"}

mysql -u root -p movie_booking -e \
  "SELECT seat_booking_id, seat_id, booking_id, seat_booking_status FROM TBL_SeatsBooking WHERE seat_id=1;"
# → unchanged: still customer A's booking_id, still status 1 (SUCCESS)
```

Before the fix, step 2 succeeded and the row in step 3 showed a new `booking_id` with the
status reset to 4.

> **Note the error is still returned with HTTP 200**, not `409 Conflict`. That is a
> separate problem and still open — see chapter 04 and Exercise 10.

## Why the same rule existed twice — the DRY lesson

The most interesting thing about that bug is that the codebase *already contained a
correct implementation of the same rule*. `SeatBookingDAO.getBookedSeats()` read:

```java
for (SeatsBooking seatsBooking : getBookedSeats(showBookingIds, seatsBookingMapper)) {
    switch (seatsBooking.getSeatBookingStatus()) {
        case StatusConstant.INITIATED:
            if (BookingUtils.isSeatOnHold(seatsBooking, curTimeInMillisec)) {
                bookedSeatIds.add(seatsBooking.getSeatId());     // held and not yet expired → taken
            }
            break;
        case StatusConstant.SUCCESS:
        case StatusConstant.INPROGRESS:
            bookedSeatIds.add(seatsBooking.getSeatId());          // definitely taken
            break;
        case StatusConstant.FAILED:
            break;                                                // free
    }
}
```

Correct, complete, readable — and note the nice detail that a single `curTimeInMillisec` is
captured before the loop, so every row is evaluated against the same instant rather than a
clock that moves as you iterate.

So the system contained **two answers to one question**: this one, which was right, and
`areSeatsAvailable`, which was wrong. Which answer a customer got depended on which code
path their request happened to take — the seat map used one, the booking check used the
other.

> **This is the real argument for DRY, and it is not "repetition is ugly".**
>
> The cost of duplicated logic is not the extra lines. It is that **copies drift**, and a
> system with drifted copies *contradicts itself* depending on how you enter it. Those
> contradictions are extraordinarily hard to debug, because the code you are reading is
> correct — it is the other copy, which you have not found yet, that is wrong.
>
> Whoever wrote the second implementation either did not know the first existed or did not
> check it. Both are ordinary, and both are why the rule now lives in exactly one method
> that every caller shares.

**The test to apply in your own work** is not "do these two blocks look similar?" — it is
**"would a change to the business rule require changing both?"** If yes, unify them. If
no, coincidental similarity is not duplication and merging them will make things worse
(chapter 10 has more on that failure mode).

Verify the consolidation held:

```bash
grep -rn "INITIATED" src/main/java --include="*.java" | grep -v "constants/"
```

Every remaining hit should be either the status definition or the one authoritative
method — not a third copy of the rule.

---

## The race is still open — four ways to close it

Fixing the availability rule fixed a *logic* bug. **It did not fix the race.** Two requests
can still both call `areSeatsAvailable`, both get `true`, and both proceed — because the
check and the write still happen on different connections in different transactions, with
`synchronized` as the only thing in between.

Reproducing it needs real concurrency (see *Testing concurrency* below), and closing it is
Exercise 18. You have to pick one of these. Each is legitimate; they differ in cost and in
what they buy.

### Option 1 — Let the database enforce it (best fit here)

The `UNIQUE (seat_id, scheduled_live_show_id)` constraint already exists. The current code
no longer *steals* rows, but it still decides in Java whether a row may be reused — and
that decision is exactly the check that can be raced. Push the decision into the database
instead: always `INSERT`, never `UPDATE`, and catch the constraint violation:

```java
try {
    seatsBookingMapper.insertSelective(seatsBooking);
} catch (DuplicateKeyException e) {          // Spring translates the SQL exception
    throw new BookingException(BookingError.SEAT_UNAVAILABLE, "Seat " + seatId + " was just taken");
}
```

Now two racing requests both attempt the insert; InnoDB accepts exactly one and rejects
the other with a duplicate-key error. **No application-level lock is involved at all.**

The one wrinkle: expired holds leave rows behind (lazy expiry, chapter 05), so a genuinely
free seat may have a stale `INITIATED` row blocking the insert. Handle that with a
**conditional update** that only succeeds if the row is genuinely stale:

```sql
UPDATE TBL_SeatsBooking
   SET booking_id = ?, seat_booking_status = 4, modified_at = NOW()
 WHERE seat_id = ? AND scheduled_live_show_id = ?
   AND seat_booking_status = 2                                            -- FAILED
    OR (seat_booking_status = 4 AND modified_at < NOW() - INTERVAL 5 MINUTE)  -- lapsed hold
```

Then check the **affected row count**. If it is 1, you won. If it is 0, someone else got
there first — the `WHERE` clause did the checking, atomically, inside the database.

> **This is the single most important technique in the chapter.** `UPDATE ... WHERE
> <expected state>` combined with checking the affected row count is an atomic
> compare-and-set. The database evaluates the condition and performs the write as one
> indivisible operation, so there is no gap to race through. It works across any number of
> application servers because the database is the shared point. Learn this pattern; you
> will use it constantly.

**Verdict for this project: do this.** It is the least code, needs no new infrastructure,
and is correct across any number of instances.

### Option 2 — Pessimistic locking (`SELECT ... FOR UPDATE`)

Take a database row lock, do your work, commit:

```sql
BEGIN;
SELECT * FROM TBL_SeatsBooking
 WHERE seat_id IN (1,2,3) AND scheduled_live_show_id = 1
   FOR UPDATE;                    -- other transactions touching these rows now block
-- check availability, insert
COMMIT;                           -- locks released
```

"Pessimistic" means you assume a conflict will happen and lock upfront. Correct, and
locking is scoped to the seats involved rather than the whole endpoint.

The cost is real: other transactions **block** rather than failing fast, so a slow
transaction backs up everything behind it. And with multiple rows locked in varying order
you can create **deadlocks** — A locks seat 1 and waits for seat 2 while B holds seat 2 and
waits for seat 1. InnoDB detects this and kills one transaction with error 1213, which your
code must be prepared to retry. (Mitigation: always lock rows in a consistent order, e.g.
sorted by `seat_id`.)

Also: `FOR UPDATE` cannot lock rows that do not exist yet, which is the common case here.
That gets into gap locks and `SELECT ... FOR UPDATE` on the parent showtime row — workable,
but more subtle than Option 1.

### Option 3 — Optimistic locking (a version column)

Add `version int` to the row. Read it, and on write require it to be unchanged:

```sql
UPDATE TBL_SeatsBooking SET booking_id = ?, version = version + 1
 WHERE seat_booking_id = ? AND version = ?
```

Zero rows affected means someone else modified the row since you read it; you reload and
retry. "Optimistic" means you assume conflicts are rare and detect rather than prevent
them. Excellent for low-contention data, and it takes no locks at all.

Poor fit for a hot showtime: on opening day, hundreds of people contend for the same
seats, so nearly every attempt fails and retries, and you spend all your capacity on retry
loops. **Optimistic locking degrades badly precisely when you need it most.**

### Option 4 — A distributed lock (Redis)

Acquire a named lock in a shared store before booking:

```java
String key = "seat-lock:" + scheduledLiveShowId + ":" + seatId;
boolean acquired = redis.set(key, requestId, SetParams.setParams().nx().px(5000));
```

This is `synchronized` done correctly: the lock lives in Redis, which every instance
shares, so it works across the fleet.

It also adds a new failure mode. What if the holder crashes before releasing? (Hence the
TTL.) What if the work outlives the TTL and two holders believe they own the lock? (Hence
fencing tokens.) What if Redis itself fails over? Distributed locks are genuinely hard —
the correctness debates around Redis's Redlock algorithm are worth reading once you have
the basics.

**Use a distributed lock when the resource you are protecting is not in a database.** If
it *is* in a database, the database's own atomicity is simpler and safer. Here, it is.

### Summary

| Approach | Correct across instances | Complexity | Fit here |
|----------|-------------------------|------------|----------|
| `synchronized` | ❌ no | trivial | what is there now; wrong |
| Unique constraint + conditional update | ✅ yes | low | **best** |
| `SELECT ... FOR UPDATE` | ✅ yes | medium | good |
| Optimistic version column | ✅ yes | medium | poor under contention |
| Redis distributed lock | ✅ yes | high | overkill |

---

## Two more concurrency problems in this code

### Two clocks

`BookingUtils.isOnHold()` compares `System.currentTimeMillis()` — the **application
server's** clock — against `modified_at`, which was written by **MySQL's** clock.

If the app server's clock runs two minutes ahead, every hold expires two minutes early and
customers get "session expired" mid-payment. Two minutes behind, and expired holds stay
blocked.

> **Rule: derive time from one authority.** Since the timestamp is written by the
> database, the comparison should be done by the database too:
> `WHERE modified_at > NOW() - INTERVAL 5 MINUTE`. Then there is exactly one clock and
> drift is impossible. (Production machines run NTP so drift is usually small — but "usually
> small" is not a correctness guarantee, and clocks do jump.)

### Reads outside the transaction

`BookingService.getShowDetails()` issues seven separate queries on seven separate
connections (chapter 07). Each sees the database as of a different instant. A booking that
is confirmed between query 3 and query 5 produces a response assembled from two different
versions of reality.

For a read-only ticket display this is tolerable. The general point is not: **a set of
reads that must be mutually consistent has to happen in one transaction**, so they see a
single consistent snapshot. In InnoDB's default `REPEATABLE READ`, all reads in one
transaction see the database as of the transaction's start.

---

## Testing concurrency

You cannot find these bugs by clicking around. Write a test that makes the race happen:

```java
@Test
public void concurrentBookingsForSameSeatMustNotBothSucceed() throws Exception {
    int threads = 20;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch startGun = new CountDownLatch(1);      // release all threads at once
    AtomicInteger successes = new AtomicInteger();

    for (int i = 0; i < threads; i++) {
        pool.submit(() -> {
            startGun.await();                              // maximise the collision
            try {
                bookingService.initateBooking(requestForSeat(5));
                successes.incrementAndGet();
            } catch (BookingException expected) { }
            return null;
        });
    }
    startGun.countDown();
    pool.shutdown();
    pool.awaitTermination(30, SECONDS);

    assertEquals("exactly one booking must win", 1, successes.get());
}
```

The `CountDownLatch` is the key trick: all threads block on it, so releasing it fires them
simultaneously and maximises the chance of hitting the gap. Without it they trickle in and
the race rarely triggers.

> **Concurrency tests are probabilistic, and that has two consequences.** A passing run
> does not prove correctness — it proves the race did not happen *this time*. So run many
> iterations, and treat an occasional failure as a real bug, never as "flaky, re-run it".
> A test that fails one time in fifty is telling you about a bug that hits one customer in
> fifty. The instinct to dismiss intermittent failures is one of the most expensive habits
> in this profession.
>
> Also note: this test cannot be written against the current code, because DAOs reach a
> static singleton for their connections and cannot be pointed at a test database.
> Untestable design and unsafe concurrency are the same problem wearing different clothes.

---

## Checkpoint

- [ ] Explain TOCTOU using the exact lines in `ShowBookingDAO.initiateBooking`
- [ ] Explain why `synchronized` protects one JVM and not a fleet — and why you will
      always have a fleet
- [ ] Explain why `NOT IN (INPROGRESS, SUCCESS)` inverted the intended meaning, and why
      expressing the condition positively made the bug impossible to write
- [ ] Explain why the fixed `occupiesSeat` throws on an unknown status instead of
      returning `false`
- [ ] Confirm a paid seat can no longer be rebooked, using the curl sequence above
- [ ] Explain what is *still* broken about booking under concurrency
- [ ] Explain `UPDATE ... WHERE <expected state>` + affected-row-count, and why it is
      atomic
- [ ] Name each of the four fixes and say when you would choose it

Next: [09-hld-system-design.md](09-hld-system-design.md).
