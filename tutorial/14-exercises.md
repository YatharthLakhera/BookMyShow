# 14 — Exercises

This is the actual curriculum. The previous chapters were context; this is where you learn.

Exercises are ordered by difficulty and dependency. Do them in order — later ones assume
earlier fixes are in place.

**Rules of engagement:**

1. **Work on a branch.** `git checkout -b tutorial/ex-01`. One branch per exercise, or one
   per group — never straight on `master`.
2. **Write the test first where a test is possible.** Watch it fail, then make it pass.
   That is the only way to know your test actually tests something.
3. **Do not open [15-answer-key.md](15-answer-key.md) until you have genuinely tried.** The
   struggle is the learning; reading a solution feels productive and teaches almost nothing.
4. **Write down what you changed and why.** Treat it as a pull request description. Being
   able to explain a change is a separate skill from making it, and it is the one you are
   assessed on.

**Difficulty:** 🟢 straightforward · 🟡 requires thought · 🔴 hard, design judgement needed

---

## Group A — Warm-up

Three of the original Group A exercises have been done for you (see *What has already been
fixed* in the tutorial README). What remains is to understand them well enough to explain
them, plus the guardrails nobody added.

### Exercise 1 🟢 — Make the toolchain requirement explicit
The Lombok version is fixed, but nothing stops someone building on a JDK that Lombok
1.18.30 does not support. Add `maven-enforcer-plugin` with a `requireJavaVersion` rule and
a `<message>` that tells the developer exactly what to do.

Verify it: build with an unsupported JDK and confirm you get your sentence at the start of
the build rather than a compiler crash in the middle.

*Think about:* why is a clear failure at the start of the build worth a plugin?

### Exercise 2 🟢 — Understand the wiring failure
Comment out `@Service` on `CustomerService`, start the app, and read what Spring says.
Then restore it and answer in writing:

- Why did this fail at startup rather than at the first HTTP request?
- Why is that a *good* design decision by Spring rather than an inconvenience?
- `PaymentService` has no stereotype annotation either, and the app starts fine. Why?

### Exercise 3 🟡 — Add the next migration
The schema is now managed by Flyway. Prove you understand the rules by adding a migration
rather than editing one: add `V3__cinema_name_length.sql` widening
`TBL_Cinema.cinema_name` from `varchar(11)` (chapter 03) to something realistic.

Then answer:

- Why must you never edit `V1__initial_schema.sql`, even though it would be "cleaner"?
- What does Flyway do on the *second* startup after you add V3?
- Deliberately corrupt a byte in `V1` and restart. Read the error. What is Flyway
  protecting you from?

*Think about:* this migration rewrites a table. On a table with 50 million rows, what would
that do to a running production system? (Search "MySQL online DDL".)

## Group B — Correctness bugs

### Exercise 4 🟢 — Handle missing resources
`GET /cinemas/999` currently fails badly (chapter 04). Trace exactly what happens, then make
every "resource does not exist" path return a proper `404` with a useful message.

Endpoints to cover: cinema, hall, showtime, show, booking-by-reference.

*Do not fix this with null checks scattered through the controllers.* Throw a
`ResourceNotFoundException` from the service and handle it in one place — you will build
that handler properly in Exercise 10, so a simple version now is fine.

### Exercise 5 🟡 — Fix the seat map
Two bugs in `GET /cinemas/{c}/halls/{h}/timings/{t}` (chapter 04):

1. `timingId` is ignored; the endpoint returns every showtime for the hall.
2. Only available seats are returned, all labelled `AVAILABLE`, so a frontend cannot
   distinguish "taken" from "does not exist".

Fix both. The response should contain **every** seat in the hall, each with its real status
— `AVAILABLE`, `ON_HOLD`, or `BOOKED`. All three `SeatStatus` values should finally be used.

*Think about:* a hall has 300 seats and this endpoint is called on every page view. How
many queries does your implementation make? Can it be one?

### Exercise 6 🟡 — Make it runnable from anywhere
Remove all three `System.getProperty("user.dir")` filesystem reads (chapter 01/07). Load
resources from the classpath instead.

Verify properly:
```bash
mvn clean package
cd /tmp && java -jar /full/path/to/target/BookMyShow-1.0-SNAPSHOT.jar
```
It must start from a directory that has no `src/` in it. (You will need Exercise 20's build
fix first, or run the packaged classes directly.)

### Exercise 7 🟡 — Fix the latent crash in `CinemaService`
`getCinemaDetailsForShow` throws `UnsupportedOperationException` when a movie plays in two
halls of the same cinema (chapter 06).

1. **First, write a test that reproduces it.** Construct the data so the merge branch runs.
   Watch it fail.
2. Fix it. The nested loops are wrong quite apart from the mutability problem — work out
   what the merge is actually supposed to do before writing code.
3. Then seed a movie into two halls of one cinema and confirm the API works.

*Think about:* what does `@Singular` do to the generated list, and was that annotation the
right choice for a class with an `addHallDetail` method?

### Exercise 8 🟢 — Validate input
Add Bean Validation to all five request DTOs (chapter 11). Then confirm each of these
returns `400` with a helpful message rather than a 500 or a nonsense success:

```bash
curl -X POST localhost:8080/booking -H 'Content-Type: application/json' -d '{"customerId":1,"scheduledLiveShowId":1}'
curl -X POST localhost:8080/booking -H 'Content-Type: application/json' -d '{"customerId":1,"scheduledLiveShowId":1,"seatIdList":[]}'
curl -X POST localhost:8080/customer/register -H 'Content-Type: application/json' -d '{"name":"","email":"not-an-email","password":"x"}'
```

### Exercise 9 🟡 — Fix duplicate registration
Chapter 03 described the bug: `UNIQUE (name, email)` plus an email-**and**-password check
lets two accounts share an email.

1. Write a migration changing the unique key to `email` alone.
2. Fix `addCustomerToDB` to check by email only.
3. Handle the constraint violation properly: return `409 Conflict`, not a 500.

*Think about:* why fix **both** the application check and the database constraint? What does
each one give you that the other does not?

---

## Group C — Structure and design

### Exercise 10 🟡 — Global exception handling
Build the `@RestControllerAdvice` from chapters 10 and 11.

1. Give `BookingException` and `CustomerException` a machine-readable error code carrying
   the appropriate HTTP status.
2. Write handlers for those, for validation failures, and a catch-all that logs the stack
   trace and returns an opaque reference.
3. **Delete every `try/catch` from every controller.** They should be pure happy path.
4. Fix the status codes: seat unavailable → `409`, expired hold → `409`, not registered →
   `401`, not found → `404`.

Measure your success by how much code the controllers lost.

### Exercise 11 🔴 — Hash passwords
1. Add `spring-security-crypto`, use `BCryptPasswordEncoder`.
2. Hash on registration; use `matches()` on login.
3. Change `CustomerDAO` to look up by **email only** (a salted hash cannot be queried by
   equality — make sure you understand why before you start).
4. Delete the password from the log line at `CustomerController:35` and remove the
   reflection-based `toString` calls that also leak it.
5. Write a migration for existing plaintext passwords.

*Think about:* in a real system with live users, you cannot hash existing passwords — you do
not have them in a reversible form, and you should not ask everyone to reset at once. What
migration strategy would let you upgrade users gradually as they log in?

### Exercise 12 🟢 — Unguessable booking references
Replace `RandomStringUtils.randomAlphabetic(20)` with something cryptographically secure
(chapter 03). Justify your choice, and handle the (astronomically unlikely) collision case
rather than letting it become a 500.

### Exercise 13 🟡 — Fix money handling
`AmountTypeHandler` has four bugs (chapter 07).

1. Write a test that round-trips ₹250.99 and asserts nothing is lost. Watch it fail.
2. Rewrite the handler. Consider whether `Double` is the right Java type at all — the better
   fix may be to change the type everywhere rather than to patch the conversion.
3. Handle nulls. Extending `BaseTypeHandler<T>` will help.
4. Check the API payloads still make sense to a client.

### Exercise 14 🟡 — Remove the boolean traps
Refactor `ShowService.getShowDetailsList(boolean)` into two named methods (chapter 10).

Then tackle `shouldShowSeatDetails` in `CinemaService`. Decide for yourself whether the
simple split or the enum approach fits, and **write down why**. There is no single right
answer; the reasoning is the exercise.

### Exercise 15 🔴 — Unify the database stack
The highest-value refactor in the project (chapter 07).

1. Delete `ConnectionFactory`, `mybatisConfig.xml`, and `db.properties`.
2. Use `mybatis-spring-boot-starter` with `@MapperScan`; inject mappers straight into DAOs.
3. Remove every `@Cleanup SqlSession` and `sqlSession.commit()`.
4. Add `@Transactional` where multiple writes must be atomic — at minimum
   `ShowService.addNewShowToDB` and the booking creation path.
5. Remove the unused `spring-boot-starter-data-jpa` and fix the duplicate
   `spring-boot-starter-test` declaration.

**Prove the transaction works:** make the second of three inserts throw, and assert the
first was rolled back. If you cannot demonstrate that, `@Transactional` may be silently
doing nothing — re-read the proxy/self-invocation note in chapter 07.

*Also confirm:* connection pooling is now active. Compare `SHOW GLOBAL STATUS LIKE
'Connections'` before and after a request, as in chapter 07.

---

## Group D — The hard problems

### Exercise 16 🟡 — Understand the fix that was made
The double-booking bug from chapter 08 is fixed. Your job is to be able to defend the fix.

1. Read `BookingUtils.occupiesSeat` and `BookingUtilsTest`.
2. Reproduce the *fixed* behaviour with the curl sequence in chapter 08: pay for a seat,
   try to book it again, confirm rejection and confirm the database row is untouched.
3. Now revert the fix locally (`git stash` a hand-edit, or just change `occupiesSeat` to
   return `false` for `SUCCESS`) and watch `BookingUtilsTest` go red and the curl sequence
   steal the seat. **Seeing the failure is the exercise.** Restore afterwards.
4. Answer in writing:
   - Why did `NOT IN (INPROGRESS, SUCCESS)` invert the meaning of the whole method?
   - Why does `occupiesSeat` throw on an unknown status instead of returning `false`?
   - Why is it safe for `insertSeatsForBooking` to throw half way through its loop?

### Exercise 17 🟡 — Prove the rule is not duplicated again
The availability rule now lives in one method. Verify that, and keep it that way.

1. Audit every caller: `areSeatsAvailable`, `getOccupiedSeatIds`, `getBookedSeats`,
   `insertSeatsForBooking`, and the seat map from Exercise 5. Confirm none re-implements
   the status logic.
2. `SeatDAO.getAvailableSeatList` reaches the rule by a different route — via
   `getShowBookingForShow` then `getBookedSeats`. Trace it. Is it asking the same question
   as `getOccupiedSeatIds`? Should these two paths be one?
3. Write a test that would fail if someone added a third copy.

*Think about:* Exercise 5's seat map needs *statuses*, not just a yes/no. Does the current
shared method give you what you need, or does it need to return a richer type?

### Exercise 18 🔴 — Make booking correct under concurrency
**The main event, and still completely open.** Fixing the availability rule fixed a logic
bug; the race is untouched.

1. **Write the concurrency test first** (chapter 08): N threads, one seat, a
   `CountDownLatch` to fire them together, assert exactly one succeeds. It should fail
   against the current code — if it passes, your threads are not actually colliding; make
   the test harder before you trust it.
2. Remove `synchronized` from both `BookingService` methods.
3. Implement a proper fix. Chapter 08 gives four options; pick one and **justify it in
   writing** against the alternatives.
4. Verify the test passes reliably — run it 50 times, not once.
5. Measure throughput before and after. Removing a global lock should show up.

*Think about:* your fix must work with two application instances. If your reasoning
anywhere depends on there being one JVM, it is wrong.

### Exercise 19 🔴 — Kill the N+1
`GET /cinemas` costs 13 queries with 4 cinemas, and grows multiplicatively (chapter 07).

1. Count the queries with SQL logging on. Write the number down.
2. Rewrite the fetch — a join with a MyBatis nested result map, or batched `IN` queries per
   level.
3. Count again.
4. Add pagination so the response is bounded regardless of catalogue size.

*Think about:* which approach did you choose and why? What happens to your join when a
cinema has halls but no showtimes — do those cinemas disappear from the response? (Consider
`LEFT JOIN`.)

### Exercise 20 🟡 — Make it deployable
1. Fix the build: replace `maven-shade-plugin` with `spring-boot-maven-plugin` (chapter 01).
2. Move every secret to environment variables with `${VAR:default}` (chapter 11).
3. Fix `generatorConfig.xml`'s absolute path so someone else can regenerate.
4. Add `spring-boot-starter-actuator`, and expose only `health` and `info`.
5. Enable graceful shutdown.
6. Write a `systemd` unit file (chapter 12).
7. Write a `Dockerfile` that runs as a non-root user.

Verify: `docker run` it with config supplied entirely through environment variables, and hit
`/actuator/health`.

---

## Group E — Extension work

Open-ended. Pick whichever interests you; there is no answer key for these.

### Exercise 21 🔴 — Expire holds properly
Currently expiry is lazy (chapter 05). Implement a `@Scheduled` sweeper that transitions
lapsed holds to a terminal status.

The hard part is not the job — it is the race between the sweeper and a payment landing at
the same instant. Solve it with a conditional update, and write a test that proves a payment
arriving at the exact moment of expiry cannot produce a booking whose seats were released.

*Also:* with two instances, both run the scheduler. Is that a problem here? What would you
do if it were? (Search "ShedLock".)

### Exercise 22 🔴 — Real authentication
Add JWT authentication (chapter 11). `POST /customer/login` issues a token; a filter
verifies it and populates the security context. Every `customerId` parameter comes from the
token, not the request.

Endpoints that must stay public: the catalogue, and `GET /booking/{ref}` (the QR-code
lookup — re-read chapter 03 on why that one is deliberately open, and decide whether you
still agree).

### Exercise 23 🟡 — Add a cancellation endpoint
`DELETE /booking/{ref}` — release a hold immediately, or cancel a confirmed booking.

Design questions you must answer before writing code: who may cancel? What happens to a
confirmed booking's money? Can you cancel after the show has started? Is the operation
idempotent if called twice?

### Exercise 24 🟡 — Prevent overlapping shows
Chapter 05 noted you can schedule two films in one hall at the same time. Add overlap
detection.

Get the boundary conditions right: a show ending at 12:00 and one starting at 12:00 do not
overlap. Should there be a cleaning gap between showings? Enforce it in the application, and
consider whether the database can help.

### Exercise 25 🔴 — Load test it
Use `k6`, `wrk`, or JMeter to find the actual limits.

1. Measure requests/sec and p50/p95/p99 latency for `/cinemas` and `/booking`.
2. Do it before and after Exercises 15, 18, and 19.
3. Find the breaking point — where does latency spike, and what is the bottleneck? Database
   connections? Tomcat threads? The lock?
4. Write it up with numbers.

*This is the most valuable exercise in the list.* Backend engineering is ultimately about
behaviour under load, and most engineers never actually measure it. Being the person on the
team who has real numbers is a genuine advantage.

---

## Suggested order

- **Week 1:** 1, 2, 3, 16, 17 — get oriented, and study the two fixes already made
- **Week 2:** 4, 5, 8, 9, 12 — correctness bugs that are still open
- **Week 3:** 6, 10, 13, 14 — structure
- **Week 4:** 15 — the big refactor (unify the database stack)
- **Week 5:** 18, 19, 20 — concurrency, performance, deployment
- **Beyond:** pick from Group E

Exercises 16 and 17 come early on purpose: they are now *reading* exercises, and the bug
they cover is the best worked example in the project of how a small logic error becomes
data loss.

---

## How to know you have understood the project

You are done when you can, without notes:

- Draw the schema and explain the three-level show chain
- Trace an HTTP request from socket to SQL and back
- Explain the booking hold, and why booking is two-phase
- Explain the double-booking bug and its fix
- Explain what is *still* unsafe about booking under concurrency
- Explain why `synchronized` is not a solution
- Explain what changes when you go from one server to two
- Deploy the service to a Linux box and reach it over HTTPS
- Name five things you would fix before letting a real customer near it, in priority order

That last one matters most. **Anyone can list what is wrong with a codebase; engineering is
knowing what to fix first.**
