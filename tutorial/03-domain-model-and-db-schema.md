# 03 — Domain Model and Database Schema

The schema is the most important artifact in this project. Code gets rewritten; schemas
outlive the code that created them, because migrating live data is far harder than
refactoring a class. Read this chapter slowly.

Source of truth: `src/main/resources/db/migration/V1__initial_schema.sql`. (The original
`MovieBooking.sql` dump is still in the repo for reference, but nothing loads it any more —
see chapter 01.)

---

## The core idea: three levels of "show"

This is the design decision that confuses everyone on first read, and it is the good part
of this schema. There are three tables that all sound like the same thing:

| Table | Means | Example |
|-------|-------|---------|
| `TBL_Show` | A *title*. The film itself, independent of where or when. | "Avenger" |
| `TBL_LiveShow` | That title **placed in a specific hall**. | "Avenger, in Hall C1A of Cinema1" |
| `TBL_ScheduledLiveShow` | That placement **at a specific time, at a specific price**. | "Avenger, Hall C1A, 20 Feb 10:00–12:00, ₹250" |

Read as a chain:

```
TBL_Show                "Avenger"                        (the movie)
   │ 1:N
TBL_LiveShow            "Avenger" × Hall C1A             (where it plays)
   │ 1:N
TBL_ScheduledLiveShow   Feb 20, 10:00–12:00, ₹250        (when, and how much)
```

**Why three tables instead of one?** Ask what would go wrong with a single flat
`shows(movie_name, hall_id, start_time, price)` table:

- The movie name "Avenger" would be repeated in every one of its showtimes. Nine
  showtimes = nine copies of the title, the poster URL, the runtime, the cast list. Fix a
  typo in the title and you must update every row, and if you miss one you now have two
  movies. This is what **normalisation** prevents.
- You could not ask "what movies are showing anywhere?" without a `SELECT DISTINCT` over
  showtimes.
- Anything genuinely per-title (`show_details` JSON, `show_type`) would have no natural
  home.

And why is `LiveShow` separate from `ScheduledLiveShow`, rather than putting `hall_id`
directly on the showtime? Because "this movie is running in this hall" is a real,
durable business fact with its own lifetime — a cinema books a film into a screen for a
two-week run, and the individual showtimes hang off that run. The middle table gives that
fact a place to live, and a place to attach future attributes (which screen format, which
distributor contract, run start and end dates).

> **🟢 Good call.** This three-level split is genuinely good modelling and mirrors how
> real ticketing systems work. When you design a schema, this is the question to keep
> asking: *what is the actual thing here, independent of the other things?* Each distinct
> answer usually deserves its own table.

> **🟡 The cost, which is real.** Every lookup now traverses the chain. To answer "what
> movie is this ticket for?" the code does
> `ScheduledLiveShow → LiveShow → Show → Hall → Cinema` — four hops, and in this codebase
> each hop is a separate query on a separate database connection. See
> `services/BookingService.java:142-146`. Normalisation is a trade: less duplication, more
> joins. The right response is to *join in one query*, not to denormalise. This codebase
> does neither, which is the subject of chapter 07.

---

## Entity relationship diagram

```
   TBL_Cinema                      TBL_Customer
   ──────────────                  ────────────────
   cinema_id  PK ◀──┐              customer_id  PK ◀──────────────┐
   cinema_name  UQ  │              name  ┐                        │
   extraData JSON   │              email ┴ UQ together            │
   is_open          │              password                       │
                    │              role  ENUM(USER,ADMIN)         │
                    │                                             │
   TBL_Hall         │                                             │
   ────────────     │                                             │
   hall_id  PK ◀──┐ │                                             │
   hall_code  ┐    │ │                                            │
   cinema_id  ┴ UQ─┘ │                                            │
   hall_row_count    │                                            │
   hall_col_count    │                                            │
   is_available      │                                            │
                     │                                            │
   TBL_Seat          │           TBL_Show                         │
   ──────────────    │           ──────────────                   │
   seat_id  PK ◀───┐ │           show_id  PK ◀──┐                 │
   seat_code  ┐    │ │           show_type ENUM │                 │
   hall_id    ┴ UQ─┘ │           show_name      │                 │
   seat_row_loc      │           show_details JSON                │
   seat_col_loc      │                          │                 │
                     │           TBL_LiveShow   │                 │
                     │           ──────────────────                │
                     └────────── hall_id                          │
                                 show_id  ────────┘               │
                                 live_show_id PK ◀──┐             │
                                 UQ(show_id, hall_id)│            │
                                                     │            │
                                 TBL_ScheduledLiveShow            │
                                 ─────────────────────────        │
                                 scheduled_live_show_id PK ◀──┐   │
                                 live_show_id ───────────────┘│   │
                                 show_start_time  ┐            │   │
                                 show_end_time    ┼ UQ together│   │
                                 tickets_price (paisa)         │   │
                                                               │   │
   TBL_StatusMaster              TBL_ShowBooking               │   │
   ────────────────              ────────────────────          │   │
   status_id PK ◀──┐             booking_id  PK ◀──┐           │   │
   status VARCHAR  │             customer_id ──────┼───────────┼───┘
   (1 SUCCESS      ├──────────── status_id         │           │
    2 FAILED       │             scheduled_live_show_id ───────┘
    3 INPROGRESS   │             total_seats_booked │
    4 INITIATED)   │             convenience_fee    │
                   │             total_booking_amount (paisa)
                   │             booking_ref_no  UQ │
                   │                                │
                   │             TBL_SeatsBooking   │
                   │             ────────────────────────
                   ├──────────── seat_booking_status│
                   │             seat_booking_id PK │
                   │             seat_id ───────────┼──▶ TBL_Seat
                   │             booking_id ────────┘
                   │             scheduled_live_show_id ──▶ TBL_ScheduledLiveShow
                   │             UQ(seat_id, scheduled_live_show_id)   ◀── the important one
                   │
                   │             TBL_ShowBookingStatus
                   │             ──────────────────────
                   └──────────── status_id
                                 show_booking_status_id PK
                                 booking_id ──▶ TBL_ShowBooking
                                 UQ(booking_id, status_id)
```

---

## Table by table

### `TBL_Cinema` — a physical venue

```sql
cinema_id    int PK AUTO_INCREMENT
cinema_name  varchar(11) NOT NULL UNIQUE
extraData    json DEFAULT NULL
created_at   timestamp DEFAULT CURRENT_TIMESTAMP
modified_at  timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
is_open      tinyint(1) NOT NULL DEFAULT 1
```

**`varchar(11)` for a cinema name.** Eleven characters. "PVR Phoenix Mall" is sixteen and
would be rejected (or silently truncated, depending on SQL mode). This is a real bug that
only surfaces when someone adds a realistically-named cinema. The sample data
("Cinema1".."Cinema4") is exactly 7 characters, so nobody noticed. **Lesson: size columns
for the real world, not for your test fixtures.** `varchar(255)` costs nothing extra in
InnoDB — storage is based on actual content length, and the declared maximum is only a
constraint.

**`is_open tinyint(1)`** is MySQL's boolean — there is no separate `BOOLEAN` type;
`BOOLEAN` is an alias for `tinyint(1)`. It is used as a **soft delete / soft disable**
flag: `CinemaDAO.getAllCinema()` filters `where is_open = true` rather than deleting rows.

> **Why soft deletes?** A cinema that closes still has historical bookings pointing at it
> through foreign keys. Hard-deleting the row would either fail on the FK constraint or
> orphan the history. Soft deletion keeps referential integrity and preserves the audit
> trail. The cost: **every query must remember the filter.** Forget `where is_open = true`
> once and closed cinemas reappear in the UI. `HallDAO` remembers (`is_available = true`,
> lines 36, 47); check every DAO and see whether they all do.

**`extraData json`.** MySQL 5.7+ has a native JSON column type. This is an escape hatch
for attributes you have not modelled yet — address, phone, amenities. Pragmatic, and
worth understanding the trade: JSON columns are not validated against a schema, are
awkward to index and query, and tend to become a dumping ground that nobody dares clean
up. Use them for genuinely variable or rarely-queried data. Anything you filter or join
on deserves a real column. Here the column is unused (always `NULL`) — a placeholder.

**`created_at` / `modified_at`.** Present on every table. `ON UPDATE CURRENT_TIMESTAMP`
means MySQL updates `modified_at` automatically on any `UPDATE` that changes a value.

> **🔴 This is not a cosmetic detail — it is load-bearing.** `TBL_SeatsBooking.modified_at`
> **is the seat-hold expiry clock**. `BookingUtils.isOnHold()` computes
> `System.currentTimeMillis() - modifiedAt.getTime() < 300000`. So a hold expires five
> minutes after the row was last modified. Which means the hold timer is driven by a
> column that MySQL updates automatically — and the application *also* sets it by hand in
> places (`SeatBookingDAO.java:87`, `:141`). Two writers, one clock. Chapter 08 explains
> why that combination is dangerous. Also note the clock used for the comparison is the
> **application server's** clock, while the value was written by the **database server's**
> clock. If they drift, holds expire early or late. Chapter 08 again.

---

### `TBL_Hall` — a screen inside a cinema

```sql
hall_id         int PK
hall_code       varchar(8) NOT NULL      -- 'C1A'
cinema_id       int NOT NULL FK → TBL_Cinema
hall_row_count  int NOT NULL             -- 10
hall_col_count  int NOT NULL             -- 30
is_available    tinyint(1) DEFAULT 1
UNIQUE (hall_code, cinema_id)
```

The unique key is a **composite** on `(hall_code, cinema_id)`: hall code "A" must be
unique *within* a cinema, but two different cinemas may both have a hall "A". That is
correct modelling of the real constraint. If the unique key were on `hall_code` alone, the
second cinema in your database could never have a hall "A".

`hall_row_count` × `hall_col_count` defines the seat grid — 10 × 30 = 300 seats, which is
why `TBL_Seat` has `AUTO_INCREMENT=601` after seeding four halls (only two halls' worth
were actually inserted in the dump; check for yourself).

---

### `TBL_Seat` — one physical seat

```sql
seat_id       int PK
seat_code     varchar(8) NOT NULL      -- 'C1A0', 'C1A1', ...
hall_id       int NOT NULL FK → TBL_Hall
seat_row_loc  int NOT NULL
seat_col_loc  int NOT NULL
UNIQUE (hall_id, seat_code)
```

Seats are created once per hall by `SeatDAO.createSeatsForHall()` (lines 21-43), which
loops rows × columns and inserts one row each, naming them `hallCode + index` — so
`C1A0` through `C1A299`.

> **🟡 Smell: seat naming.** Real cinemas label seats `A1`, `A2`, … `J30` — a row letter
> and a seat number, which is what is printed on the ticket and painted on the seat. This
> scheme produces `C1A147`, which tells a customer nothing and cannot be matched to the
> physical seat. `seat_row_loc`/`seat_col_loc` hold the real coordinates, so the display
> name should be derived from them: `(char)('A' + row) + String.valueOf(col + 1)`.
>
> Note also that this loop is the *only* way seats ever get created, and the endpoint
> that would call it is commented out at `controller/CinemaController.java:139-143`. So in
> practice seats only exist because the SQL dump inserts them. Adding a new hall through
> the API is not possible today.

**Important: seats are physical, not per-showtime.** There is exactly one row for seat
`C1A0`, forever. Its *availability* is not stored on this table — it is derived from
`TBL_SeatsBooking`, which is per (seat, showtime). This is the right call: 300 seats × 9
showtimes × every day forever would be an enormous table of mostly-empty rows.

---

### `TBL_ShowBooking` — one customer's purchase

```sql
booking_id             int PK
customer_id            int NOT NULL FK → TBL_Customer
scheduled_live_show_id int NOT NULL FK → TBL_ScheduledLiveShow
total_seats_booked     int NOT NULL
convenience_fee        int NOT NULL DEFAULT 0
total_booking_amount   int NOT NULL      -- in paisa
booking_ref_no         varchar(32) NOT NULL UNIQUE
status_id              int NOT NULL FK → TBL_StatusMaster
```

**`booking_ref_no`** is the customer-facing identifier — the code on the ticket. It is a
20-character random alphabetic string from
`ApplicationUtils.getRandomString()` (`utils/ApplicationUtils.java:36-42`).

> **Why not just expose `booking_id`?** Because sequential integers leak information and
> invite abuse. `GET /booking/57` tells an attacker there are ~57 bookings and that
> `/booking/56` probably exists. This project's `GET /booking/{bookingRefNo}` endpoint has
> **no authentication at all** — the comment at `BookingController.java:78-86` explains
> this is intentional so a QR code at the cinema gate can be scanned without a login. That
> is a defensible design *only* because the reference is unguessable. With sequential IDs
> it would be a trivial enumeration attack exposing every customer's booking.
>
> **But:** `RandomStringUtils.randomAlphabetic()` uses `java.util.Random` internally, not
> `SecureRandom`. `java.util.Random` is a linear congruential generator whose entire
> future output can be predicted from ~2 observed values. For anything acting as a
> security token — and this is one — you must use `SecureRandom`, or better, a UUIDv4.
> This is Exercise 12.
>
> Also: 20 alphabetic characters with no uniqueness retry. The column is `UNIQUE`, so a
> collision throws a `SQLIntegrityConstraintViolationException` that nothing catches →
> HTTP 500. Astronomically unlikely at 52²⁰, but the *pattern* (generate random ID, hope
> it is unique, no retry) is worth noticing.

**Money is stored as `int` paisa.** The SQL comment says so: `-- Amount is in paisa`. So
₹250.00 is stored as `25000`.

> **🟢 Good call, executed badly.** Storing money as an integer in the smallest currency
> unit is exactly right. Floating-point types cannot represent most decimal fractions:
> `0.1 + 0.2 == 0.30000000000000004` in Java, in Python, in JavaScript — it is an IEEE-754
> property, not a language bug. Money in `float`/`double` produces cents that appear and
> vanish, and reconciliation reports that never balance. **Never store or compute money in
> a floating-point type.**
>
> But then look at `db/typehandlers/AmountTypeHandler.java` — it converts the integer
> paisa to a **`Double`** on the way into Java, and back on the way out. So the code takes
> a correctly-stored integer and immediately moves it into the one type it must avoid.
> Worse, `BookingPaymentRequest.ticketAmount` is a `double` that is used in a SQL
> **equality comparison** at `ShowBookingDAO.java:97` — comparing floating-point values
> with `=` is unreliable by nature.
>
> The fix: keep it a `long` of paisa end to end, or use `BigDecimal`. Exercise 13.

---

### `TBL_SeatsBooking` — the heart of the system

```sql
seat_booking_id        int PK
seat_id                int NOT NULL FK → TBL_Seat
booking_id             int NOT NULL FK → TBL_ShowBooking
scheduled_live_show_id int NOT NULL FK → TBL_ScheduledLiveShow
seat_booking_status    int NOT NULL FK → TBL_StatusMaster
created_at             timestamp
modified_at            timestamp
UNIQUE (seat_id, scheduled_live_show_id)      -- ★
```

This is the table that decides whether the whole product works. It answers: *for this
showtime, who holds this seat, and in what state?*

**The starred unique key is the single most important line in the schema.**

```sql
UNIQUE KEY `TBL_SeatsBooking_uk_1` (`seat_id`,`scheduled_live_show_id`)
```

It makes it **physically impossible** for two rows to claim the same seat at the same
showtime. Not "unlikely". Impossible — InnoDB will reject the second insert with a
duplicate-key error no matter how many application servers, threads, or racing requests
are involved.

> **This is the most important production lesson in the entire tutorial.**
>
> Application-level checks ("SELECT to see if it is taken, then INSERT") are always
> racy — between your check and your write, another thread can act. You will meet this
> exact bug in chapter 08.
>
> A database constraint is different in kind. It is enforced inside the single component
> that all your application servers share, at the moment of the write, atomically. It
> cannot be raced.
>
> **Rule: every invariant that must never be violated belongs in the schema, as a
> constraint — not only in application code.** Application checks give good error
> messages; constraints give correctness. You want both, and if you can only have one,
> take the constraint.
>
> The application used to go out of its way to defeat this constraint:
> `SeatBookingDAO.insertSeatsForBooking()` turned every would-be duplicate `INSERT` into an
> `UPDATE`, silently overwriting somebody else's seat instead of being rejected. That has
> been fixed — a row is now only reused when it genuinely holds no seat — but the deeper
> point stands: **the code still decides in Java whether a row may be claimed, so the
> decision can still be raced.** Chapter 08 dissects both halves.

**`seat_booking_status`** is the seat's state, referencing `TBL_StatusMaster`:

```
4 INITIATED   → held, checkout in progress. Counts as taken only if modified_at is
                within the last 5 minutes; after that the hold has lapsed.
3 INPROGRESS  → payment being processed. Taken.
1 SUCCESS     → paid and confirmed. Taken.
2 FAILED      → payment failed. Seat is free again.
```

Note there is no `EXPIRED` status and **no background job that expires anything**. A
lapsed hold keeps `INITIATED` in the database forever; expiry is recomputed on every read
by comparing `modified_at` to the current time. This is called **lazy expiry**, and its
consequences are chapter 08's main subject.

---

### `TBL_StatusMaster` — a lookup table

```sql
status_id  int PK
status     varchar(32)
-- (1,'SUCCESS'), (2,'FAILED'), (3,'INPROGRESS'), (4,'INITIATED')
```

Four rows, referenced by foreign key from `TBL_ShowBooking.status_id`,
`TBL_SeatsBooking.seat_booking_status`, and `TBL_ShowBookingStatus.status_id`.

The FK is doing real work: it makes `status_id = 99` impossible. Compare with
`TBL_Show.show_type`, which uses a MySQL `ENUM('MOVIE','IPL')` instead — same guarantee,
different mechanism. The trade-off is worth knowing: a lookup table lets you add a status
with an `INSERT`, while a MySQL `ENUM` needs an `ALTER TABLE` (which rewrites the table
and can lock it for a long time on large tables). Conversely, the lookup table costs a
join or an in-memory copy to display a human-readable name.

This project keeps a hardcoded mirror of the table in Java, at
`constants/StatusConstant.java`:

```java
public static final int SUCCESS = 1;
public static final int FAILED = 2;
private static final Map<Integer, String> StatusMap = new HashMap<Integer, String>(){{
    put(SUCCESS, "SUCCESS");
    ...
}};
```

> **🟡 Two smells in five lines.**
>
> **1. This should be an `enum`.** `int` constants give you no type safety whatsoever —
> `updateBookingStatus(showBooking, 7)` compiles cleanly and corrupts your data.
> `updateBookingStatus(showBooking, customerId)` also compiles. A Java `enum` makes both
> impossible at compile time, gives you exhaustive `switch` checking, and carries the
> display name as a field. Note this project already *has* enums elsewhere
> (`enums/ShowType.java`, `enums/SeatStatus.java`) — the inconsistency is the smell.
>
> **2. `new HashMap<>(){{ put(...); }}` is the double-brace initialisation idiom, and you
> should never use it.** Those inner braces are an *instance initialiser block* inside an
> *anonymous subclass* of `HashMap`. So every use creates an extra class file, and — the
> real problem — the anonymous inner class holds an implicit reference to its enclosing
> instance. In a non-static context that keeps the outer object alive as long as the map
> lives, which is a genuine memory leak, and it breaks serialization. Use
> `Map.of(...)` (Java 9+) or a static block. It looks clever; it is a trap.

---

### `TBL_ShowBookingStatus` — an audit trail that is never written

```sql
show_booking_status_id  int PK
booking_id              int FK
status_id               int FK
UNIQUE (booking_id, status_id)
```

The intent is clear: a history row per status transition, so you can reconstruct
"initiated at 10:00:03, in progress at 10:01:15, succeeded at 10:01:17".

**Nothing in the codebase ever inserts into it.** Search:

```bash
grep -rn "ShowBookingStatus" src/main/java --include="*.java" | grep -v "db/mappers"
```

You will find only generated mapper classes. The table and its generated Java are dead
code.

Two lessons. First, **audit trails are worth building** — when a customer says "I was
charged but have no ticket", the status history is how you find out what happened, and
without it you are guessing. Second, note the unique key `(booking_id, status_id)`:
because it is unique, a booking could never pass through the same status twice. A retry
that goes `INITIATED → INPROGRESS → FAILED → INPROGRESS` would violate it. The key should
be on `(booking_id, created_at)` or nothing at all. **A half-designed table nobody uses is
worse than no table** — it looks like a solved problem to the next reader.

---

## `TBL_Customer` and the security problems

```sql
customer_id  int PK
name         varchar(128) NOT NULL
email        varchar(128) NOT NULL
password     varchar(128) NOT NULL
role         enum('USER','ADMIN') NOT NULL DEFAULT 'USER'
UNIQUE (name, email)          -- ★ note the columns
```

> **🔴 Production trap 1: passwords stored in plaintext.** `CustomerDAO.insert()` writes
> `customer.setPassword(password)` with the raw string, and `getCustomerBy(email, password)`
> logs in with `where email = ? and password = ?` — comparing plaintext. Anyone with read
> access to the database, any backup file, any accidental log dump, gets every customer's
> password. And because people reuse passwords, you have also compromised their email and
> banking accounts.
>
> **The correct approach, briefly:** store a *hash*, never the password. Not MD5 or SHA-256
> — those are designed to be *fast*, which is exactly wrong here, because fast means an
> attacker can try billions of guesses per second on stolen hashes. Use a deliberately
> slow, salted, memory-hard algorithm: **bcrypt**, scrypt, or Argon2. Spring Security
> ships `BCryptPasswordEncoder`; it is three lines to adopt. Login becomes
> `encoder.matches(submittedPassword, storedHash)` rather than a SQL equality check.
> Exercise 11.

> **🔴 Production trap 2: the password is written to the log.**
> `controller/CustomerController.java:35`:
> ```java
> log.info("Customer Login - Email : {}, Password : {}", request.getEmail(), request.getPassword());
> ```
> Every login writes a plaintext password to the application log. Logs get shipped to
> aggregators, backed up, indexed, and read by people who have no business seeing
> credentials — and they typically have far weaker access controls than the database.
> **Never log credentials, tokens, card numbers, or personal data.** Chapter 11 covers
> what to log instead.

> **🔴 Production trap 3: the unique key is on the wrong columns.**
> `UNIQUE (name, email)` — so `("Alice", "a@x.com")` and `("Bob", "a@x.com")` can both
> exist. Two accounts, one email address. Since login is `where email = ? and password = ?`
> with `limit 1`, which account you get depends on which row MySQL returns first.
>
> The constraint should be `UNIQUE (email)`. And notice how this compounds with the
> application logic: `CustomerService.addCustomerToDB()` checks
> `isCustomerRegistered(email, password)` — email **and** password. So registering with an
> existing email but a *different* password passes the check, and then
> `CustomerDAO.insert()` runs its own `getCustomerBy(email, password)`, also finds
> nothing, and inserts a second account. The database does not stop it because the unique
> key includes `name`.
>
> This is a good illustration of the earlier rule: the application check was wrong *and*
> the constraint was wrong, so nothing caught it. Exercise 14.

**`role enum('USER','ADMIN')`** maps to `enums/CustomerRole.java` via the
`<columnOverride>` in `generatorConfig.xml`. It gates exactly one endpoint —
`ShowController.addShow()` checks `customerService.isAdmin(...)`. Note there is no
authentication anywhere: the client simply *asserts* `customerId` in the request body, and
the server trusts it. Anyone can send an admin's customer ID. See chapter 11 on why
identity must come from a verified token, not from the request body.

---

## Indexes: what exists, and what is missing

MySQL creates an index automatically for every `PRIMARY KEY`, every `UNIQUE` key, and
every `FOREIGN KEY`. So this schema already has useful ones:

- `TBL_SeatsBooking (seat_id, scheduled_live_show_id)` — from the unique key
- `TBL_ShowBooking (scheduled_live_show_id)`, `(status_id)`, `(customer_id)` — from FKs
- `TBL_Hall (cinema_id)`, `TBL_LiveShow (hall_id)`, and so on

**What is missing, given the actual query patterns in the code:**

`SeatBookingDAO.getSeatsForBookingId()` filters `where booking_id = ?`. There *is* an
index on `booking_id` (from the FK) — good. `getOccupiedSeatIds()` filters on
`(seat_id, scheduled_live_show_id)`, which the unique key covers exactly, so that lookup is
served by an index. (It classifies the status in Java rather than in SQL — deliberately, so
the availability rule lives in one place; see chapter 08.)

More importantly, `ShowBookingDAO.getBookedShowByCustomerId()` filters
`customer_id = ? AND status_id IN (...)`. Two single-column indexes exist, but MySQL will
generally pick one and filter the rest — a **composite index on `(customer_id, status_id)`**
would serve this query directly.

> **How to think about indexes, in one paragraph.** An index is a sorted copy of some
> columns with pointers back to the rows. It converts a full table scan (read every row)
> into a tree lookup. The cost is real: extra disk, and every `INSERT`/`UPDATE`/`DELETE`
> must also update every affected index. So do not index everything. Index the columns you
> filter, join, and sort on. **Column order in a composite index matters**: an index on
> `(a, b)` serves `where a = ?` and `where a = ? and b = ?`, but not `where b = ?` — think
> of a phone book sorted by (surname, first name), which is useless for finding everyone
> named "James". The practical tool is `EXPLAIN`: put it in front of any query and MySQL
> tells you which index it chose and how many rows it expects to examine. Try it now:
>
> ```sql
> EXPLAIN SELECT * FROM TBL_SeatsBooking WHERE booking_id = 1;
> EXPLAIN SELECT * FROM TBL_ShowBooking WHERE customer_id = 1 AND status_id IN (1,3);
> ```

---

## Checkpoint

- [ ] Explain the `Show → LiveShow → ScheduledLiveShow` chain to someone else, with an
      example, and say why one flat table would be worse
- [ ] Explain why seat availability is stored in `TBL_SeatsBooking` rather than on `TBL_Seat`
- [ ] Explain why `UNIQUE (seat_id, scheduled_live_show_id)` is stronger than any check
      you could write in Java
- [ ] Name three things wrong with how `TBL_Customer` handles passwords
- [ ] Run `EXPLAIN` on one query and read the output

Next: [04-api-reference.md](04-api-reference.md).
