# 04 — API Reference

All 16 endpoints, with real payloads. Base URL for local development:
`http://localhost:8080`.

Each entry lists the signature, the source location, an example, and — importantly —
what is wrong with it. Learning to critique an API signature is a large part of backend
work, so do not skim the critique sections.

---

## Endpoint index

| Method | Path | Purpose | Source |
|--------|------|---------|--------|
| `GET` | `/cinemas` | All cinemas with halls and showtimes | `CinemaController:36` |
| `GET` | `/cinemas/{cinemaId}` | One cinema | `CinemaController:48` |
| `GET` | `/cinemas/{cinemaId}/halls` | Halls in a cinema | `CinemaController:60` |
| `GET` | `/cinemas/{cinemaId}/halls/{hallId}` | One hall | `CinemaController:80` |
| `GET` | `/cinemas/{cinemaId}/halls/{hallId}/timings` | Showtimes in a hall | `CinemaController:102` |
| `GET` | `/cinemas/{cinemaId}/halls/{hallId}/timings/{timingId}` | One showtime + seat map | `CinemaController:125` |
| `GET` | `/shows` | Currently-showing movies | `ShowController:36` |
| `GET` | `/shows/all` | All movies from the last 3 months | `ShowController:54` |
| `GET` | `/shows/{showId}` | One movie, with where it plays | `ShowController:42` |
| `POST` | `/shows/add` | Add a movie + schedule (admin only) | `ShowController:59` |
| `POST` | `/customer/register` | Create an account | `CustomerController:54` |
| `POST` | `/customer/login` | Log in | `CustomerController:34` |
| `GET` | `/customer/{customerId}/history` | Booking history | `CustomerController:73` |
| `POST` | `/booking` | Hold seats (start checkout) | `BookingController:38` |
| `POST` | `/booking/pay` | Pay and confirm | `BookingController:64` |
| `GET` | `/booking/{bookingRefNo}` | Look up a ticket | `BookingController:88` |

---

## Browsing: cinemas

### `GET /cinemas`

Returns every open cinema, each with its available halls, each with all showtimes.

```bash
curl -s http://localhost:8080/cinemas | python3 -m json.tool
```

```json
[
  {
    "cinemaId": 1,
    "cinemaName": "Cinema1",
    "hallDetails": [
      {
        "hallId": 1,
        "hallCode": "C1A",
        "hallRowCount": 10,
        "hallColCount": 30,
        "scheduledTimings": [
          {
            "scheduledTimingId": 1,
            "startTime": 1582172400000,
            "endTime": 1582179600000,
            "ticketAmount": 250.0,
            "seatDetailsList": null
          }
        ]
      }
    ]
  }
]
```

Notes on that payload:

- **`startTime` is a number**, not a date string. Jackson's default rendering of
  `java.util.Date` is epoch milliseconds. Most APIs prefer ISO-8601. See chapter 02.
- **`ticketAmount: 250.0`** — a floating-point number for money, converted from `25000`
  paisa by `AmountTypeHandler`. See chapter 03 on why this is the wrong type.
- **`seatDetailsList: null`** because the controller passes `false` for
  `shouldShowSeatDetails`. `ScheduledTimingDetails` lacks the `@JsonInclude(NON_NULL)`
  annotation that its siblings have, so its nulls are rendered rather than omitted — an
  inconsistency worth noticing.

> **🔴 Production trap: no pagination.** This endpoint returns *every* cinema, *every*
> hall, and *every* showtime that has ever existed, in one response, with no limit. With
> four cinemas that is a few kilobytes. With 500 cinemas × 8 halls × 40 showtimes it is a
> multi-megabyte response built by tens of thousands of database round-trips (chapter 07),
> holding a Tomcat thread the whole time.
>
> **Every list endpoint needs a bound.** The two standard approaches: offset pagination
> (`?page=0&size=20` — simple, but gets slow at high offsets and can skip or repeat items
> when data changes underneath) and cursor pagination (`?after=<opaque_token>&limit=20` —
> stable and fast, but no random access to page 57). For a customer-facing catalogue you
> would also want filters that let the client ask for less: `?city=`, `?date=`,
> `?movieId=`. Design list endpoints so that the *default* response is bounded. If a
> client has to opt in to safety, someone will forget.

### `GET /cinemas/{cinemaId}`

```bash
curl -s http://localhost:8080/cinemas/1
```

Same shape, one element.

> **Try `curl -s http://localhost:8080/cinemas/999`.** `CinemaDAO.getCinemayId()` returns
> `null` for a missing row, and `CinemaService.getCinemaDetails(Cinema, boolean)`
> immediately calls `cinema.getCinemaId()` on it. Trace what the client receives. It
> should be a `404`; predict what it actually is, then check. (Exercise 4.)
>
> Note also the method name: `getCinemayId`. A typo, propagated to every call site. Trivial
> to fix with an IDE rename, and worth doing — sloppy names make a codebase feel
> untrustworthy, and that perception affects how carefully people treat it.

### `GET /cinemas/{cinemaId}/halls` and `/halls/{hallId}`

Note the return types in the source:

```java
public ResponseEntity getHallDetails(@PathVariable int cinemaId)                    // line 59
public APIResponse   getHallDetails(int cinemaId, int hallId)                       // line 79
```

The first returns a **raw** `ResponseEntity` (no type parameter), which is why the method
carries `@SuppressWarnings("unchecked")`. The second returns `APIResponse` directly,
giving up control of the status code.

> **🟡 Smell: raw types and the marker interface.** `models/APIResponse.java` is an empty
> interface:
> ```java
> public interface APIResponse { }
> ```
> `CinemaDetail`, `HallDetail`, `ShowDetail`, `BookingDetails`, `CustomerDetail`, and
> `ErrorResponse` all implement it. It exists so a method can return "either a success DTO
> or an error DTO" with a single declared type. That is a reasonable instinct — Java has
> no union types — but the execution has costs: `APIResponse` promises nothing (no
> methods), so callers learn nothing from the type, and the `ResponseEntity` raw types
> force `@SuppressWarnings` sprinkled through the controllers. Suppressing a warning is
> almost always a sign that the design, not the compiler, is wrong.
>
> The conventional alternative is a **generic envelope**:
> ```java
> public class ApiResponse<T> {
>     private T data;
>     private ApiError error;
> }
> ```
> plus a `@RestControllerAdvice` that turns thrown exceptions into the error branch. Then
> controllers return `ResponseEntity<ApiResponse<HallDetail>>`, the type tells the reader
> exactly what comes back, and no warnings need suppressing.

### `GET /cinemas/{cinemaId}/halls/{hallId}/timings/{timingId}`

Look at the implementation (`CinemaController:125-136`):

```java
public ResponseEntity getScheduledTimeDetailsById(@PathVariable int cinemaId,
                                                  @PathVariable int hallId,
                                                  @PathVariable int timingId) {
    HallDetail hallDetail = cinemaService.getHallDetail(cinemaId, hallId, false);
    if (hallDetail != null) {
        responseEntity = new ResponseEntity(cinemaService.getScheduledLiveShowByHallId(hallId, true), HttpStatus.OK);
    }
    ...
```

**`timingId` is accepted and never used.** The method returns every showtime for the
hall, not the one requested. The URL promises one resource and delivers a list.

The `true` argument is `shouldShowSeatDetails`, so this *is* the endpoint that returns the
seat map — the only one that does. It is just filtered wrong. This is the endpoint a
frontend would call to draw the seat-selection screen, so the bug matters. (Exercise 5.)

```bash
curl -s http://localhost:8080/cinemas/1/halls/1/timings/1 | python3 -m json.tool
```

```json
[
  {
    "scheduledTimingId": 1,
    "startTime": 1582172400000,
    "endTime": 1582179600000,
    "ticketAmount": 250.0,
    "seatDetailsList": [
      { "seatId": 1, "seatStatus": "AVAILABLE", "seatCode": "C1A0",
        "seatRowLocation": 0, "seatColLocation": 0 }
    ]
  }
]
```

> **Note what is missing: unavailable seats.** `CinemaService.getSeatDetailsList()` only
> ever builds `SeatDetails` for seats returned by `getAvailableSeatList()`, always with
> `seatStatus(SeatStatus.AVAILABLE)`. So booked seats are simply absent from the array.
> The `SeatStatus` enum defines `BOOKED`, `AVAILABLE`, and `ON_HOLD`, and only one of the
> three is ever used.
>
> That makes it impossible for a frontend to draw a correct seat map: it cannot tell "seat
> C1A5 is taken" (draw it grey) from "seat C1A5 does not exist" (draw a gap for the
> aisle). The endpoint should return *all* seats in the hall, each with its true status.
> (Exercise 5.)

---

## Browsing: shows

### `GET /shows` and `GET /shows/all`

`/shows` returns movies that are currently live; `/shows/all` returns everything created
in the last three months (`AppConstants.DISPLAY_SHOW_FOR_MONTHS = 3`).

```bash
curl -s http://localhost:8080/shows | python3 -m json.tool
```

```json
[
  {
    "showId": 1,
    "showType": "MOVIE",
    "showName": "Avenger",
    "cinemaDetails": [
      { "cinemaId": 1, "cinemaName": "Cinema1", "hallDetails": [ ... ] }
    ]
  }
]
```

This is the inverse view of `/cinemas` — organised by movie rather than by venue, which
is what the README means by "implemented in multiple formats". Both are legitimate
product screens ("what's playing near me" vs "where can I watch this film").

> **🟡 Routing subtlety worth knowing.** Both `/shows/{showId}` and `/shows/all` are
> registered. `all` is not an integer, so how does Spring choose? Spring MVC's pattern
> matcher ranks **literal** path segments above **variable** segments, so `/shows/all`
> wins and `/shows/{showId}` never sees it. This works, but relies on a subtlety a reader
> has to know. Prefer distinct paths (`/shows?live=false`) or at least a comment.
>
> Verify it yourself: `curl -s http://localhost:8080/shows/all` should return the list,
> not a type-mismatch 400.

> **🟡 The `isLive` boolean.** `ShowService.getShowDetailsList(boolean isLive)` branches
> on a flag, and the two branches share almost no logic (`liveShowDAO.isShowLive(...)` vs
> a date comparison). At the call site, `showService.getShowDetailsList(true)` communicates
> nothing. Chapter 10 uses this exact method as its worked example of removing a boolean
> trap.

Also note `LiveShowDAO.isShowLive()` (lines 41-56): it returns true if *any* `TBL_LiveShow`
row exists for the show — it never checks whether any *showtime* is in the future. A film
that finished its run last year is still "live". The name promises more than the query
delivers.

### `POST /shows/add` — the only admin endpoint

```bash
curl -s -X POST http://localhost:8080/shows/add \
  -H 'Content-Type: application/json' \
  -d '{
        "customerId": 1,
        "showName": "Dune",
        "showType": "MOVIE",
        "hallId": 1,
        "startTime": "2026-09-01 10:00:00",
        "endTime": "2026-09-01 12:30:00",
        "ticketPrice": 300.0
      }'
```

Returns `201` with a `ShowDetail`, or `403` with an error if the customer is not an admin.

> **🟢 Good call:** this is the one endpoint that returns a genuinely correct status code
> on the failure path (`403 FORBIDDEN`, `ShowController:64`). Compare with every other
> controller, which returns `200 OK` for errors.

> **🔴 Production trap: authorisation based on a client-supplied ID.** The check is
> `customerService.isAdmin(addShowRequest.getCustomerId())` — where `customerId` comes
> **from the request body**. Anyone can send `{"customerId": 1, ...}`. If customer 1 is an
> admin, any anonymous person on the internet can add shows.
>
> This is the single most important security principle in web backends: **identity must
> be established by the server, never asserted by the client.** The client presents
> *evidence* (a session cookie, a JWT, an API key) that the server verifies
> cryptographically or against its own store; the server then derives the user ID from
> that verified evidence. A user ID in a request body is not evidence of anything.
>
> Every endpoint in this project that takes `customerId` as an input has this flaw.
> Chapter 11 shows the shape of the fix.

> **🟡 Dates as unvalidated strings.** `AddShowRequest` declares `startTime` as a `String`
> and parses it in the getter:
> ```java
> public Date getStartTime() { return ApplicationUtils.getParsedDate(this.startTime, DATE_FORMAT); }
> ```
> And `ApplicationUtils.getParsedDate()` **swallows the parse failure**, logging an error
> and returning `null`. So `"startTime": "next tuesday"` yields `null`, which flows into
> `scheduledLiveShow.setShowStartTime(null)`, and MySQL rejects it with a `NOT NULL`
> violation → HTTP 500. The user gets no useful message and the log line
> (`"Unable to parse date : {} using format {}"`) passes `parsedDate` where it means
> `parsingFormat`, so it prints `null` for the format.
>
> Three separate lessons: **do not swallow exceptions** (a caught exception that returns
> null just moves the crash somewhere less informative); **validate at the boundary**
> (`@Valid` + a `@DateTimeFormat`-annotated `LocalDateTime` field lets Spring reject bad
> input with a clear `400` before your code runs); and **putting logic in a getter is
> surprising** — `getStartTime()` looks free, but re-parses a string on every call.

Also note: no timezone. `"2026-09-01 10:00:00"` in whose zone? `FastDateFormat.getInstance(format)`
uses the **JVM default timezone**, so the answer depends on the server's configuration and
changes if the server moves. See chapter 11.

---

## Customer

### `POST /customer/register`

```bash
curl -s -X POST http://localhost:8080/customer/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Intern","email":"intern@example.com","password":"hunter2"}'
```

```json
{"errorMessage": "User is successfully registered. Please login to proceed"}
```

> **🟡 A success response inside a class called `ErrorResponse`, in a field called
> `errorMessage`.** Look at `CustomerService.addCustomerToDB()` — its return type is
> literally `ErrorResponse`, and the success path returns
> `new ErrorResponse(ErrorMessages.USER_REGISTER_SUCCESSFUL)`. The constant lives in a
> class called `ErrorMessages`.
>
> A client cannot distinguish success from failure by shape — both return
> `{"errorMessage": "..."}` with HTTP 200. The only way to tell them apart is **string
> matching on an English sentence**, which breaks the moment anyone fixes a typo or adds
> a second language.
>
> This is what happens when error handling is not designed up front: the error type
> becomes the only available "some message" type and gets reused for everything. The fix
> is the generic envelope described earlier, plus correct status codes — `201` on success
> with the created resource, `409 Conflict` if the email is taken.

Registration is also where the duplicate-account bug from chapter 03 lives: register the
same email twice with *different* passwords and see what you get.

### `POST /customer/login`

```bash
curl -s -X POST http://localhost:8080/customer/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"intern@example.com","password":"hunter2"}'
```

```json
{"customerId": 1, "customerName": "Intern",
 "customerEmail": "intern@example.com", "customerRole": "USER"}
```

> **🔴 This is not a login.** A real login issues a **credential for subsequent requests**
> — a signed session token or JWT — which the client sends on every later call and the
> server verifies. This endpoint just confirms the password matches and hands back the
> numeric ID, which the client is then expected to include in booking requests. Since the
> server never verifies that ID again, the "login" is decorative: you can call
> `POST /booking` with any `customerId` and book tickets as that person, without ever
> logging in.
>
> Also note `@RequestMapping(method = POST)` here rather than the `@PostMapping` used two
> methods below, and remember `CustomerController:35` logs the password.

### `GET /customer/{customerId}/history`

```bash
curl -s http://localhost:8080/customer/1/history | python3 -m json.tool
```

Returns an array of `BookingDetails`. Filtered to statuses `SUCCESS` and `INPROGRESS`
(`ShowBookingDAO:125-133`), so failed and abandoned bookings do not appear — sensible.

> **🔴 Anyone can read anyone's history.** `/customer/2/history` returns customer 2's
> bookings regardless of who is asking. This is the IDOR vulnerability described in
> chapter 02 — and note the contrast with `HallDAO.getHallBy(cinemaId, hallId)`, where the
> same class of check *was* done correctly. With real authentication, the customer ID
> would come from the verified token and this path variable would not exist at all:
> `GET /customer/me/history`.

---

## Booking — the important flow

### `POST /booking` — hold the seats

```bash
curl -s -X POST http://localhost:8080/booking \
  -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "scheduledLiveShowId": 1, "seatIdList": [1, 2, 3]}'
```

```json
{
  "totalAmount": 750.0,
  "bookingRefNo": "KjHgFdSaQwErTyUiOpAs",
  "bookingStatus": "INITIATED",
  "showDetail": { "showId": 1, "showName": "Avenger", "showType": "MOVIE",
                  "cinemaDetails": [ ... ] }
}
```

What happens server-side (`BookingService:49-56` → `ShowBookingDAO:48-61`):

1. Load the showtime to get the ticket price. **The client does not send the price** —
   🟢 correct, and important. If the client sent the price, a customer could pay ₹1 for a
   ₹250 ticket. Never trust the client for anything that determines money.
2. Check the seats are available.
3. Insert one `TBL_ShowBooking` row, status `INITIATED`.
4. Insert one `TBL_SeatsBooking` row per seat, status `INITIATED`.
5. Commit, and return the reference number.

The booking is now **held for five minutes** (`BookingUtils.TICKET_HOLD_TIME_IN_MILLISEC`).

Failure responses (all with HTTP `200`, all of which should be `4xx`):

```json
{"errorMessage": "Some or all seats are occupied, please refresh and try again"}
{"errorMessage": "Please sign in for booking tickets"}
```

> **🔴 The hold duration is a hardcoded `private static final int` at
> `utils/BookingUtils.java:12`.** The README advertises it as configurable; it is not.
> Changing it requires a code change, a rebuild, and a redeploy. A timeout like this is
> exactly the kind of value the business will want to tune ("make it 8 minutes during
> opening weekend"), and it belongs in `application.yaml` bound with
> `@ConfigurationProperties`. See chapter 11.

> **No input validation at all.** Try these and see what you get:
> ```bash
> curl -X POST localhost:8080/booking -H 'Content-Type: application/json' -d '{"customerId":1,"scheduledLiveShowId":1}'
> curl -X POST localhost:8080/booking -H 'Content-Type: application/json' -d '{"customerId":1,"scheduledLiveShowId":1,"seatIdList":[]}'
> curl -X POST localhost:8080/booking -H 'Content-Type: application/json' -d '{"customerId":1,"scheduledLiveShowId":9999,"seatIdList":[1]}'
> curl -X POST localhost:8080/booking -H 'Content-Type: application/json' -d '{"customerId":1,"scheduledLiveShowId":1,"seatIdList":[1,1,1]}'
> ```
> Missing `seatIdList` → `null` → NPE at `bookingRequest.getSeatIdList().size()`. Empty
> list → a booking for zero seats at ₹0, and an empty SQL `IN ()`. Nonexistent showtime →
> NPE at `scheduledLiveShow.getTicketsPrice()`. Duplicate seat IDs → charged three times
> for one seat.
>
> All four are prevented by Bean Validation: `@NotNull`, `@Size(min=1, max=10)`, `@Valid`
> on the parameter. Roughly six annotations, and Spring rejects bad input with a `400`
> before a line of your code executes. Exercise 8.

> **There is no "release my hold" endpoint.** A customer who changes their mind must wait
> out the full five minutes. Real systems expose a cancel so inventory returns immediately.

### `POST /booking/pay` — confirm

```bash
curl -s -X POST http://localhost:8080/booking/pay \
  -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "bookingRefNo": "KjHgFdSaQwErTyUiOpAs", "ticketAmount": 750.0}'
```

```json
{"totalAmount": 750.0, "bookingRefNo": "KjHgFdSaQwErTyUiOpAs",
 "bookingStatus": "SUCCESS", "showDetail": { ... }}
```

Server-side (`BookingService:65-83`):

1. Find the booking by `(bookingRefNo, customerId, totalBookingAmount)` and verify the
   hold has not lapsed — else throw `BOOKING_SESSION_EXPIRED`.
2. Set status `INPROGRESS` (booking and all its seats).
3. Call `PaymentService.processPayment()` — **a mock that always returns `SUCCESS`**
   (`services/PaymentService.java:13-15`).
4. Set status `SUCCESS` or `FAILED` accordingly.

> **🟡 `ticketAmount` is part of the lookup key.** `ShowBookingDAO:94-98` puts the
> client-supplied amount into the `WHERE` clause. So sending the wrong amount yields "no
> booking found" rather than a clear "amount mismatch". Worse, it is a floating-point
> equality comparison routed through `AmountTypeHandler` — chapter 03 explains why that is
> fragile. The amount should be *verified* against the stored value and reported clearly,
> not used to find the row. Look it up by `bookingRefNo` (which is unique) plus the
> authenticated customer, then compare.

> **🔴 The real payment integration is where this design would break.** `PaymentService`
> returning `SUCCESS` unconditionally hides every hard problem:
>
> - **Payments are asynchronous.** Real gateways take seconds, may return "pending", and
>   confirm later via a **webhook**. A synchronous method returning an enum cannot model
>   that.
> - **The `INPROGRESS` state has no way out.** Look at the `switch` in
>   `BookingService:71-80`: the `INPROGRESS` case is an empty `break`. If a real gateway
>   ever returned pending, that booking's seats stay `INPROGRESS` — treated as taken by
>   `getBookedSeats()`, with no expiry — **forever**. Those seats become permanently
>   unsellable. A real system needs a reconciliation job that polls the gateway for
>   stuck payments and resolves them.
> - **You must be idempotent.** Networks retry. If the customer's browser resends
>   `/booking/pay`, or the gateway delivers its webhook twice, you must not charge twice.
>   The standard mechanism is an **idempotency key**: the client generates a unique key per
>   logical attempt, the server stores the result against it, and a repeat with the same
>   key returns the stored result instead of re-executing.
> - **You must handle "money taken, ticket not issued".** If the gateway succeeds and your
>   database write then fails, the customer has paid for nothing. This is the classic
>   distributed-transaction problem; the practical answers are the transactional outbox
>   pattern and a reconciliation job. Chapter 09 discusses this.

### `GET /booking/{bookingRefNo}` — look up a ticket

```bash
curl -s http://localhost:8080/booking/KjHgFdSaQwErTyUiOpAs | python3 -m json.tool
```

Deliberately unauthenticated, per the comment at `BookingController:78-86` — the QR code
on the ticket encodes this URL so gate staff can scan without logging in. That is a
reasonable design given an unguessable reference (see chapter 03 on `SecureRandom`).

> **What happens for an unknown reference?** `ShowBookingDAO.getShowBookingBy()` returns
> `null`, and `BookingService.getBookingDetails(String)` passes it straight into
> `getBookingDetails(ShowBooking)`, which dereferences it. Predict the response, then
> check. It should be `404`.

---

## Cross-cutting API problems

Collected here so you can see the pattern rather than the instances.

| # | Problem | Where | Fix |
|---|---------|-------|-----|
| 1 | Errors returned as HTTP 200 | every controller | `@RestControllerAdvice` + correct status codes |
| 2 | Identity asserted by the client (`customerId` in body/path) | every authenticated endpoint | real auth; derive the ID from a verified token |
| 3 | No input validation | every `@RequestBody` | Bean Validation (`@Valid`, `@NotNull`, `@Size`) |
| 4 | No pagination on list endpoints | `/cinemas`, `/shows`, `/history` | `page`/`size` or cursor, with a bounded default |
| 5 | `try/catch` duplicated in every handler | every controller | global exception handler |
| 6 | Success and error share one response shape | `/customer/register` | typed envelope `ApiResponse<T>` |
| 7 | No API versioning | all paths | `/api/v1/...` so you can evolve without breaking clients |
| 8 | No OpenAPI/Swagger docs | project-wide | springdoc-openapi generates them from the annotations |
| 9 | Inconsistent annotation style | `@RequestMapping` vs `@PostMapping` | pick one |
| 10 | Money as `double` in payloads | `ticketAmount`, `totalAmount` | integer minor units, or a string decimal |

> **On #7, versioning.** Once a mobile app is in customers' hands you cannot change a
> response shape — old app versions will keep calling you for years. Putting `/v1/` in the
> path from day one costs nothing and buys you the ability to ship `/v2/` later while
> `/v1/` keeps working. Retrofitting a version prefix after launch means breaking
> everyone.

---

## Checkpoint

- [ ] Call every GET endpoint successfully
- [ ] Complete a full booking: register → login → hold → pay → look up the ticket
- [ ] Deliberately break three endpoints with bad input and note what the client receives
- [ ] Explain why the client must not send the ticket price on `POST /booking`, but the
      server must not trust `customerId` either
- [ ] Pick any endpoint and write down the status code it *should* return in each case

Next: [05-user-flows.md](05-user-flows.md).
