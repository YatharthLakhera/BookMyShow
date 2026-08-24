# 05 — User Flows

Chapter 04 listed the endpoints. This chapter strings them together the way a real
frontend would, so you can see the *sequence* and the state changes in the database.

Run every command in this chapter. When you finish, you will have a confirmed ticket in
your local database and will have watched every row change.

---

## Flow 1 — Browse and pick a show

A customer opens the app. Two entry points exist, and the API supports both:

**"What's playing near me?"** → `GET /cinemas` — organised by venue.
**"Where can I watch Avenger?"** → `GET /shows` — organised by title.

Both return the same underlying data assembled in the opposite direction. Then the
customer drills down to a specific showtime and needs a seat map:

```
GET /cinemas                                       → pick Cinema1
GET /cinemas/1/halls                               → pick Hall C1A
GET /cinemas/1/halls/1/timings                     → pick 20 Feb, 10:00
GET /cinemas/1/halls/1/timings/1                   → seat map (the only endpoint with seats)
```

```bash
curl -s http://localhost:8080/cinemas | python3 -m json.tool | head -40
curl -s http://localhost:8080/cinemas/1/halls/1/timings/1 | python3 -m json.tool | head -30
```

Remember from chapter 04: that last call ignores the `timingId` and returns *all* timings
for the hall, and its `seatDetailsList` contains only available seats, all labelled
`AVAILABLE`. A real frontend cannot draw a seat map from this. Note it; you will fix it.

**Database state:** unchanged. Pure reads.

---

## Flow 2 — Register and log in

```bash
curl -s -X POST http://localhost:8080/customer/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Intern","email":"intern@example.com","password":"hunter2"}'
```

```json
{"errorMessage": "User is successfully registered. Please login to proceed"}
```

**Database state:** one new row in `TBL_Customer`, `role = 'USER'`, password stored in
plaintext. Confirm it:

```bash
mysql -u root -p movie_booking -e "SELECT customer_id, name, email, password, role FROM TBL_Customer;"
```

Seeing your own password sitting in a column is a more effective lesson than any amount
of writing about it.

```bash
curl -s -X POST http://localhost:8080/customer/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"intern@example.com","password":"hunter2"}'
```

```json
{"customerId": 1, "customerName": "Intern", "customerEmail": "intern@example.com", "customerRole": "USER"}
```

Note your `customerId` — you need it for booking.

**What "logged in" means here:** nothing on the server. No session was created, no token
issued, nothing recorded. The server answered a question ("do these credentials match?")
and forgot the interaction. The client is expected to remember `customerId` and include it
in later requests, and the server will accept that number from anyone. See chapter 11.

---

## Flow 3 — The booking flow (the important one)

This is a **two-phase** flow, and understanding why is the point of the chapter.

```
Phase 1: POST /booking       → hold the seats, status INITIATED, 5-minute timer starts
                                (customer now goes off to enter card details)
Phase 2: POST /booking/pay   → verify the hold is still valid, take payment, confirm
```

### Why two phases and not one?

Because payment takes time — the customer types a card number, the bank sends an OTP, the
gateway round-trips. That is thirty seconds to two minutes during which **someone else
must not be able to buy the same seat**, but during which the sale is not yet final either.

A single-phase design fails at both ends:

- Confirm the seat only *after* payment succeeds, and two customers can both pay for seat
  C1A5. Now you must refund one of them and explain why. Unacceptable for a real product.
- Confirm the seat *before* payment, and every abandoned checkout permanently removes a
  seat from inventory. A bot could sweep the entire hall by starting bookings and never
  paying.

The hold resolves it: the seat is **provisionally** unavailable, with an expiry. If
payment completes in time the hold becomes a sale; if not, the seat quietly returns to
inventory. Every ticketing, hotel, and airline system works this way. The pattern is worth
recognising by name — **reserve, then confirm** — because you will implement it again.

### Phase 1 — hold the seats

```bash
curl -s -X POST http://localhost:8080/booking \
  -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "scheduledLiveShowId": 1, "seatIdList": [1, 2, 3]}' \
  | python3 -m json.tool
```

```json
{
  "totalAmount": 750.0,
  "bookingRefNo": "aBcDeFgHiJkLmNoPqRsT",
  "bookingStatus": "INITIATED",
  "showDetail": { "showId": 1, "showName": "Avenger", ... }
}
```

Save that `bookingRefNo`.

**What happened in the database.** Look immediately:

```bash
mysql -u root -p movie_booking -e "
  SELECT booking_id, customer_id, total_seats_booked, total_booking_amount,
         booking_ref_no, status_id, modified_at FROM TBL_ShowBooking\G
  SELECT seat_booking_id, seat_id, booking_id, seat_booking_status, modified_at
         FROM TBL_SeatsBooking;"
```

- One `TBL_ShowBooking` row, `status_id = 4` (INITIATED), `total_booking_amount = 75000`
  (paisa — the API showed you 750.0, the type handler converted).
- Three `TBL_SeatsBooking` rows, `seat_booking_status = 4`, one per seat.
- `modified_at` on those seat rows **is the hold clock**. Five minutes from that instant,
  the hold lapses.

Code path, so you can follow it in your IDE:

```
BookingController.initiateBooking            (:37)
  └─ CustomerService.isCustomerRegistered    — one query
  └─ BookingService.initateBooking           (:49)   ← note: `synchronized`, and a typo
       ├─ ScheduledLiveShowDAO.getScheduledLiveShowById   — get the price server-side
       ├─ ShowBookingDAO.initiateBooking     (:47)
       │    ├─ SeatBookingDAO.areSeatsAvailable          ← session #1  (the check)
       │    ├─ insertBookingEntry                        ← session #2  (the write)
       │    ├─ SeatBookingDAO.insertSeatsForBooking      ← same session #2
       │    └─ sqlSession.commit()
       └─ getBookingDetails(...)             — ~6 more queries to build the response
```

Two things to notice now, both developed fully in chapter 08:

1. **The availability check and the insert happen on different database sessions** — so
   between them, another request can slip in. That gap is a **TOCTOU** (time-of-check to
   time-of-use) race.
2. **`synchronized`** on the service method is the author's attempt to close that gap. It
   works on one JVM and stops working the moment you run two instances behind a load
   balancer — which is the first thing you do in production.

### Phase 2 — pay

Within five minutes:

```bash
curl -s -X POST http://localhost:8080/booking/pay \
  -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "bookingRefNo": "aBcDeFgHiJkLmNoPqRsT", "ticketAmount": 750.0}' \
  | python3 -m json.tool
```

```json
{"totalAmount": 750.0, "bookingRefNo": "aBcDeFgHiJkLmNoPqRsT",
 "bookingStatus": "SUCCESS", "showDetail": { ... }}
```

The status transitions `INITIATED (4) → INPROGRESS (3) → SUCCESS (1)`, on the booking row
and all three seat rows together. Verify:

```bash
mysql -u root -p movie_booking -e "
  SELECT booking_ref_no, status_id FROM TBL_ShowBooking;
  SELECT seat_id, seat_booking_status FROM TBL_SeatsBooking;"
```

Note the intermediate `INPROGRESS` write is a real database round-trip that happens
*before* the payment call. That is deliberate and correct: if the process crashes during
payment, the durable record says "we were mid-payment on this booking", which is what a
reconciliation job needs to resolve it. A booking still sitting in `INITIATED` after a
crash means payment was never attempted. **Write your intent before you act, not after.**

### The expiry path

Now do it again and *wait*:

```bash
# Phase 1
curl -s -X POST http://localhost:8080/booking -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "scheduledLiveShowId": 1, "seatIdList": [10, 11]}'

# wait more than 5 minutes, then:
curl -s -X POST http://localhost:8080/booking/pay -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "bookingRefNo": "<the ref>", "ticketAmount": 500.0}'
```

```json
{"errorMessage": "Booking session expired. Please try booking again"}
```

(Returned with HTTP 200. It should be `409 Conflict` or `410 Gone`.)

The check is `BookingUtils.isBookingSessionOnHold()` →
`statusId == INITIATED && (now - modifiedAt) < 300000`.

**Now look at what is left behind:**

```bash
mysql -u root -p movie_booking -e "
  SELECT booking_id, status_id, modified_at FROM TBL_ShowBooking WHERE status_id = 4;
  SELECT seat_booking_id, seat_id, seat_booking_status FROM TBL_SeatsBooking WHERE seat_booking_status = 4;"
```

The rows are still there, still `INITIATED`. Nothing cleaned them up. The seats *are*
bookable again — because every reader recomputes expiry from `modified_at` — but the rows
accumulate forever.

> **This is "lazy expiry", and it is a real design choice with real trade-offs.**
>
> **In its favour:** no background job to write, deploy, monitor, or debug. No possibility
> of the cleaner and a live request disagreeing. Nothing to go wrong at 3 AM. For a
> system this size, that simplicity is worth a lot.
>
> **Against it:** every reader must remember to apply the expiry rule, and if one forgets,
> seats leak — which is exactly the bug chapter 08 dissects, where one of the two
> availability checks forgot the rule. (Both now share a single implementation.) The
> `TBL_SeatsBooking` table also grows without bound with dead holds. And you cannot answer "how many holds expired
> today?" without scanning.
>
> **The alternative** is a scheduled job (`@Scheduled`, or a separate worker) that sweeps
> expired holds to a terminal status. Then reads are simple — `status = INITIATED` really
> means held — at the cost of an extra moving part, plus a new race between the sweeper
> and a payment landing at the same instant (which you close with a conditional update:
> `UPDATE ... SET status = ? WHERE booking_id = ? AND status = ?`).
>
> **There is no universally right answer.** What matters is that you can articulate the
> trade-off, and that whichever you choose is applied *consistently*. The codebase's actual
> bug was never the choice of lazy expiry — it was that the rule existed in two places and
> one of them was wrong.

---

## Flow 4 — View a ticket

```bash
curl -s http://localhost:8080/booking/aBcDeFgHiJkLmNoPqRsT | python3 -m json.tool
curl -s http://localhost:8080/customer/1/history | python3 -m json.tool
```

The first is the QR-code lookup (no auth, by design). The second is the account's history,
filtered to `SUCCESS` and `INPROGRESS`.

Assembling one `BookingDetails` is expensive. Follow `BookingService.getShowDetails()`
(lines 149-183):

```java
LiveShow liveShow = liveShowDAO.getById(...);            // query 1
Show show = showDAO.getById(...);                        // query 2
Hall hall = hallDAO.getHallById(...);                    // query 3
Cinema cinema = cinemaDAO.getCinemayId(...);             // query 4
List<Integer> seatIdList = seatBookingDAO.getSeatIdsForBooking(...);  // query 5
List<Seat> seatList = seatDAO.getSeatList(seatIdList);   // query 6
```

Six queries — plus the `scheduledLiveShow` lookup before them, so seven — each on its own
brand-new database connection, to render **one** ticket. Now note that `/history` calls
this in a loop, once per booking (`BookingService:103-111`). A customer with 20 bookings
triggers ~140 sequential connections and queries in a single request.

That is the **N+1 query problem**, and it is the subject of the next two chapters. All
seven of those tables could be fetched in a single `JOIN`.

---

## Flow 5 — The admin flow

```bash
mysql -u root -p movie_booking -e "UPDATE TBL_Customer SET role='ADMIN' WHERE customer_id=1;"

curl -s -X POST http://localhost:8080/shows/add \
  -H 'Content-Type: application/json' \
  -d '{"customerId":1,"showName":"Dune","showType":"MOVIE","hallId":1,
       "startTime":"2026-09-01 10:00:00","endTime":"2026-09-01 12:30:00","ticketPrice":300.0}'
```

`ShowService.addNewShowToDB()` writes the three-level chain from chapter 03 in order:

```java
Show show = addShowToDB(addShowRequest);                              // TBL_Show
LiveShow liveShow = addLiveShowToDB(show.getShowId(), hallId);        // TBL_LiveShow
ScheduledLiveShow s = addScheduledLiveShowToDB(liveShow.getLiveShowId(), request);  // TBL_ScheduledLiveShow
```

Each DAO is "insert if not already present", so re-running with the same movie name reuses
the existing `Show` and only adds a new showtime. That is sensible.

> **🔴 Three writes, three separate transactions, no atomicity.** Each DAO opens its own
> `SqlSession` and commits independently. If the process dies after the `Show` insert but
> before the `LiveShow` insert, you are left with a movie that exists but plays nowhere —
> a partial write with no rollback.
>
> This is precisely what **transactions** are for. All three inserts should happen inside
> one transaction that commits together or rolls back together — the "A" in ACID,
> atomicity. In a normal Spring application this is one annotation:
> ```java
> @Transactional
> public ShowDetail addNewShowToDB(AddShowRequest request) { ... }
> ```
> Spring then wraps the method in a transaction and rolls it back if an exception escapes.
> **But `@Transactional` cannot work in this codebase**, because Spring can only manage
> transactions on connections it owns — and the DAOs bypass Spring entirely to get their
> own sessions from `ConnectionFactory`. Chapter 07 explains this in full; it is the
> single biggest consequence of the two-database-stacks design.
>
> Note also there is **no validation that the new showtime does not overlap an existing
> one in the same hall.** You can schedule two films in Hall C1A from 10:00 to 12:00. The
> unique key `(live_show_id, show_start_time, show_end_time)` only prevents an exact
> duplicate of the same film. Overlap detection needs a real query:
> `WHERE hall_id = ? AND show_start_time < ? AND show_end_time > ?`.

---

## The complete flow, as one diagram

```
 CUSTOMER                    API                          DATABASE
    │                         │                              │
    │── GET /shows ──────────▶│── many SELECTs ─────────────▶│
    │◀── movie list ──────────│                              │
    │                         │                              │
    │── POST /customer/register ─▶ INSERT TBL_Customer ──────▶│
    │── POST /customer/login ─▶│── SELECT by email+password ─▶│
    │◀── customerId ──────────│   (no session created)       │
    │                         │                              │
    │── GET /cinemas/1/halls/1/timings/1 ─── seat map ───────▶│
    │                         │                              │
    │   ┌─────────────────────────────────────────────────┐  │
    │   │ PHASE 1: HOLD                                   │  │
    │── POST /booking ───────▶│                              │
    │                         │── SELECT: seats available? ─▶│
    │                         │── INSERT ShowBooking (4) ───▶│
    │                         │── INSERT SeatsBooking ×N (4)▶│
    │                         │── COMMIT ───────────────────▶│
    │◀── bookingRefNo ────────│                              │
    │   │  ⏱  5-minute hold running (modified_at + 300s)  │  │
    │   └─────────────────────────────────────────────────┘  │
    │                         │                              │
    │   ┌─────────────────────────────────────────────────┐  │
    │   │ PHASE 2: PAY                                    │  │
    │── POST /booking/pay ───▶│                              │
    │                         │── SELECT by ref+cust+amount ▶│
    │                         │   hold still valid? ─────────│─── no ──▶ 409 "expired"
    │                         │── UPDATE status → 3 ────────▶│
    │                         │   PaymentService (mocked)    │
    │                         │── UPDATE status → 1 ────────▶│
    │◀── SUCCESS + ticket ────│                              │
    │   └─────────────────────────────────────────────────┘  │
    │                         │                              │
    │── GET /booking/{ref} ──▶│── ~7 SELECTs to build ticket▶│
```

---

## Checkpoint

- [ ] Complete a booking end to end and see all three status values in the database
- [ ] Let a hold expire and observe both the error and the orphaned `INITIATED` rows
- [ ] Explain to someone why booking is two-phase, without using the word "hold"
- [ ] Explain what lazy expiry is, and give one argument for and one against it
- [ ] Count the queries behind one `GET /booking/{ref}` with SQL logging on

Next: [06-code-tour.md](06-code-tour.md).
