# 09 — High-Level Design

Chapters 02–08 were LLD: classes, methods, queries. This chapter zooms out to the level
you would be asked about in a system design interview, and — more usefully — the level at
which you decide what to build next.

We start from what exists and evolve it, so every step is motivated by a real problem
rather than added because it is fashionable.

---

## Stage 0 — What exists today

```
   ┌──────────┐        ┌──────────────────────────┐        ┌───────────┐
   │  client  │───────▶│  one JVM process         │───────▶│   MySQL   │
   │  (curl)  │  HTTP  │  embedded Tomcat :8080   │  JDBC  │   :3306   │
   └──────────┘        │  controllers/services/DAO│        └───────────┘
                       └──────────────────────────┘
                          both on one laptop
```

Everything on one machine, one process, one database, no pool, no cache, no queue.

**This is a fine place to start.** A monolith with one database is the correct
architecture for the overwhelming majority of products, and staying here longer than feels
impressive is usually right. Every box you add is a thing that can fail at 3 AM.

The honest assessment of what it can serve: bounded by 200 Tomcat threads, N+1 queries,
UNPOOLED connections, and a global `synchronized` on booking. Realistically **tens of
requests per second**, with the booking endpoint capped near 20/s regardless of hardware.

---

## Stage 1 — Fix the code before you add machines

The first instinct when a service is slow is to add servers. Resist it. The fixes from the
previous chapters cost nothing in infrastructure and buy an order of magnitude:

| Fix | Chapter | Effect |
|-----|---------|--------|
| Connection pooling instead of UNPOOLED | 07 | removes a TCP+auth handshake from every query |
| Kill the N+1 (join or batch) | 07 | 13 queries → 1 on the catalogue endpoints |
| Replace `synchronized` with a DB constraint | 08 | booking becomes parallel and *correct* |
| Add the missing indexes | 03 | index lookups instead of scans |
| Pagination on list endpoints | 04 | bounded response size and query count |

> **This is the most important lesson in the chapter.** Architecture diagrams are exciting
> and refactoring is not, but a single N+1 fix routinely outperforms doubling your server
> count — and it costs no money, adds no operational surface, and cannot page you at
> night. **Profile before you scale.** The most common cause of "we need microservices" is
> an unprofiled query.

Only when the code is efficient does adding hardware make sense.

---

## Stage 2 — More than one instance

Two reasons, and note that *load is the second one*:

1. **Availability.** One instance means every deploy is an outage, and every crash is an
   outage until someone notices. Two instances let you deploy one at a time.
2. **Capacity.** Two instances serve roughly twice the traffic.

```
                        ┌───────────────┐
        users ─────────▶│ Load Balancer │  (nginx / AWS ALB)
                        └───────┬───────┘
                     ┌──────────┼──────────┐
                     ▼          ▼          ▼
                ┌────────┐ ┌────────┐ ┌────────┐
                │ app 1  │ │ app 2  │ │ app 3  │
                └────┬───┘ └────┬───┘ └────┬───┘
                     └──────────┼──────────┘
                                ▼
                          ┌───────────┐
                          │   MySQL   │
                          └───────────┘
```

**What this immediately breaks in the current code:**

- **`synchronized` stops protecting anything** (chapter 08). This must be fixed *before*
  you scale out, not after. It is the difference between a working system and one that
  silently double-books.
- **Any in-memory state becomes inconsistent.** There is none today, which is lucky. The
  moment someone adds a `HashMap` cache to a service, instance 1 and instance 2 disagree.
- **Load balancer health checks are now required.** The LB needs an endpoint to decide
  whether an instance can take traffic. There is none — add `spring-boot-starter-actuator`
  and you get `/actuator/health` for free.

> **The property that makes scaling out possible is called *statelessness*.** Any instance
> must be able to serve any request, because the load balancer will not send the same user
> to the same instance. That means no session state in memory, no local file uploads, no
> in-process caches that must agree. **All shared state goes into shared infrastructure** —
> the database, Redis, S3. Keep your application processes disposable, and you can add,
> remove, restart, and redeploy them freely. That single property is what makes modern
> deployment work.
>
> Today's login (chapter 04) accidentally satisfies this by storing no session at all. When
> you add real authentication, this is the moment to prefer a **signed JWT** (verifiable by
> any instance with no lookup) or a **session store in Redis** (shared) over an in-memory
> `HttpSession` (instance-local, and broken behind a load balancer).

---

## Stage 3 — The database becomes the bottleneck

You can add app instances cheaply. You cannot add primary databases — there is one, and
every write goes to it.

**Step 1: read replicas.** Almost all traffic here is reads — browsing cinemas, shows,
seat maps. MySQL replicates the primary to replicas; send reads there and writes to the
primary.

```
                    writes │                       │ reads
                           ▼                       ▼
                  ┌─────────────┐  replication  ┌─────────┐
                  │   PRIMARY   │──────────────▶│ replica │
                  └─────────────┘               ├─────────┤
                                                │ replica │
                                                └─────────┘
```

> **The catch you must design around: replication lag.** A replica is milliseconds to
> seconds behind. So "write, then immediately read" breaks — a customer completes a
> booking, the app writes to the primary and reads the confirmation from a replica that
> does not have it yet, and the customer sees "booking not found" for their own purchase.
>
> The standard remedy is **read-your-writes**: route a user's reads to the primary for a
> short window after they write. More generally, classify each read as "must be current"
> (booking confirmation, seat availability at checkout) or "may be slightly stale"
> (browsing the catalogue). Only the second kind can go to a replica.
>
> **Seat availability must never be read from a replica.** Showing seats as available
> based on stale data is exactly the failure mode you spent chapter 08 preventing.

**Step 2: caching.** Cinema, hall, show, and seat-layout data changes rarely and is read
constantly.

```
   request ──▶ app ──▶ Redis ──hit──▶ return
                 │
                 └────miss──▶ MySQL ──▶ store in Redis ──▶ return
```

Cache the catalogue with a short TTL. **Never cache seat availability** — it changes by the
second and a stale answer causes double-booking.

> **The hard part of caching is invalidation.** An admin adds a showtime; the cached
> catalogue is now wrong until the TTL expires. Options: accept the staleness (a TTL of 60
> seconds is often fine for a catalogue), or explicitly evict on write. Start with TTLs —
> they are simple and self-healing. Reach for explicit invalidation only when the staleness
> actually hurts, and know that you are taking on a hard correctness problem when you do.

**Step 3: sharding.** Splitting data across multiple databases — say by city or by cinema.
This is a large step: cross-shard queries and transactions become hard or impossible, and
rebalancing is painful. **Do it last, and only when you have exhausted replicas, caching,
and query optimisation.** Most companies never need it.

---

## Stage 4 — The opening-day problem

The defining event for a ticketing platform: a blockbuster's bookings open at 10:00 AM and
a hundred thousand people hit the same handful of showtimes in the same minute. Average
traffic is irrelevant; this minute is the whole design problem.

Three specific weapons:

**1. A virtual waiting room.** Admit users to the booking flow at a controlled rate and
show everyone else a queue position. This turns "the site is down" — a total failure — into
"you are number 4,102 in line" — a slow but working experience. It is the single most
effective technique for extreme, predictable spikes, and it is why you have seen queue
screens on ticket sites.

**2. Rate limiting.** Cap requests per user and per IP. This protects you from bots
sweeping inventory and from one client's retry loop consuming the capacity of a thousand
real users. Implement at the edge (the load balancer or an API gateway) so the traffic never
reaches your application.

**3. Move the contention off the database.** For a single hot showtime, thousands of
requests contend for a few hundred seats. Options: hold that showtime's seat inventory in
Redis and let atomic Redis operations arbitrate; or serialise per showtime through a queue
partitioned by `scheduledLiveShowId`, so requests for one showtime are processed in order
while different showtimes proceed in parallel.

> **Notice the pattern in all three: shed load early, and reduce contention rather than
> fighting it.** The cheapest request is the one you never process. Rejecting at the edge
> costs microseconds; rejecting after seven database round-trips costs a connection, a
> thread, and 50 ms you needed for someone else.

---

## Stage 5 — Where you might split the monolith

Only after the above. And note that the first split below is not about scale at all.

**Payments should be separated first, for correctness rather than performance.** Payment
has different requirements from everything else: it must be idempotent, it must reconcile
against an external provider, it must be auditable, and its failures need a different
on-call response. Isolating it means a bug in the catalogue code cannot take payments
down.

```
   ┌─────────────┐   ┌──────────────┐   ┌──────────────┐
   │  Catalogue  │   │   Booking    │   │   Payments   │
   │ (read-heavy)│   │(write-heavy) │   │(external I/O)│
   │  cache hard │   │ correctness  │   │ idempotency, │
   │             │   │ critical     │   │ reconciliation│
   └─────────────┘   └──────────────┘   └──────────────┘
```

> **Be sceptical of microservices.** They convert simple in-process method calls into
> network calls that can fail, time out, and arrive twice. They turn one transaction into a
> distributed transaction, which has no clean solution — only patterns like sagas and
> compensating actions that are far harder than `@Transactional`. They multiply your
> deployment, monitoring, and debugging surface.
>
> Split when a *team* boundary or a *reliability* boundary demands it, not because a
> diagram looks more professional with more boxes. "One database, one deployable, several
> well-separated packages" is a perfectly respectable architecture for a service serving
> millions of users, and it is far easier to change.

---

## The payment problem, properly

Chapter 04 flagged that `PaymentService` hides the hard parts. Here is what a real design
looks like, because this is the highest-stakes flow in the system.

```
 customer          your service              payment gateway
    │                    │                          │
    ├─ POST /pay ───────▶│                          │
    │                    ├─ write intent (INPROGRESS, idempotency key)
    │                    ├─ charge(key) ───────────▶│
    │                    │                          ├─ may take seconds
    │◀─ 202 "pending" ───┤                          │
    │                    │                          │
    │                    │◀── webhook: SUCCESS ─────┤
    │                    ├─ verify signature        │
    │                    ├─ idempotent apply → SUCCESS
    │◀─ push / poll ─────┤                          │
```

The five things that make it correct:

1. **Write your intent before you act.** The `INPROGRESS` row must be committed *before*
   the gateway call. If you crash mid-call, the durable record tells reconciliation what
   was in flight. (The current code does do this — `BookingService:68`. Credit where due.)
2. **Idempotency keys.** The client generates a unique key per logical payment attempt.
   The server stores the outcome against it. Retries — from the browser, from the network,
   from your own retry logic — return the stored result instead of charging again.
   **Every payment API you will ever integrate with expects this.**
3. **Webhooks, not polling.** The gateway calls you back when the payment resolves.
   Verify the signature — an unauthenticated webhook endpoint is a way for anyone to mark
   any booking as paid.
4. **Reconciliation.** A scheduled job finds bookings stuck in `INPROGRESS` beyond a
   threshold, asks the gateway what really happened, and resolves them. Without this, the
   dead-end `INPROGRESS` state from chapter 04 permanently removes seats from sale.
5. **Decide what happens when the money succeeds and the ticket fails.** It will happen.
   Either auto-refund, or flag for manual review — but decide deliberately, because the
   default is a customer who paid for nothing and a support team with no tooling.

---

## What to monitor

An unmonitored service is one you learn about from Twitter. The standard starting set —
the **four golden signals**:

| Signal | For this service | Alert when |
|--------|------------------|------------|
| **Latency** | p50/p95/p99 per endpoint | p99 of `/booking` > 1s |
| **Traffic** | requests/sec | sudden drop (something upstream is broken) |
| **Errors** | 5xx rate, exception count | > 1% of requests |
| **Saturation** | connection pool usage, thread pool, heap, CPU | pool > 80% used |

> **Use percentiles, not averages.** An average latency of 200 ms can hide the fact that
> 1% of users wait 10 seconds. With a million requests that is ten thousand angry people,
> and the average will never show them to you. **p99 is where the pain lives.**

Business metrics matter as much as technical ones, and often catch incidents first: holds
created per minute, hold→payment conversion rate, payment success rate, bookings stuck in
`INPROGRESS`. A conversion rate that drops from 85% to 40% tells you something is broken
even when every technical dashboard is green.

Also essential:

- **Structured logging** with a **correlation ID** per request, propagated through every
  log line and every downstream call, so you can reconstruct one user's journey from a
  million interleaved log lines. See chapter 11.
- **Health checks** — `/actuator/health` — that check dependencies, so the load balancer
  removes an instance whose database connection is dead.
- **Alerting that a human actually acts on.** An alert nobody responds to is worse than no
  alert: it trains the team to ignore alerts.

---

## Answering "design BookMyShow" in an interview

Since you will be asked. A structure that works:

1. **Clarify scope.** Read-heavy or write-heavy? How many cinemas, shows, concurrent
   users? Does the opening-day spike matter? Payments in scope? *Asking these first is
   half the assessment.*
2. **Sketch the API.** Roughly what this project has: browse, seat map, hold, pay, ticket.
3. **Sketch the data model.** The `Show → LiveShow → ScheduledLiveShow` chain from chapter
   03 — this is the part most candidates get muddled, and being crisp about it stands out.
4. **Identify the hard problem and say so out loud.** It is not the CRUD. It is **seat
   allocation under concurrency**. Name TOCTOU, then present the options from chapter 08
   with their trade-offs.
5. **Then scale**, in the order of this chapter: efficient code → stateless instances →
   read replicas and caching → spike handling.
6. **Volunteer the failure modes.** Payment succeeds but booking fails. Hold expires
   mid-payment. Replica lag showing stale seats. An interviewer learns more from a
   candidate who names the ways their design breaks than from one who claims it does not.

> The single biggest differentiator: **name your trade-offs**. "I would use the unique
> constraint rather than a distributed lock, because the database is already the shared
> point of truth and Redis adds a failure mode without adding a guarantee" is a much
> stronger answer than naming any technology.

---

## Checkpoint

- [ ] Explain why you fix N+1 before you add servers
- [ ] Explain statelessness and what breaks without it behind a load balancer
- [ ] Explain replication lag and name one read that must not go to a replica
- [ ] Explain why an idempotency key is mandatory for payments
- [ ] Explain why p99 latency matters more than average
- [ ] Give one good reason to split a service, and one bad one

Next: [10-design-principles-in-this-code.md](10-design-principles-in-this-code.md).
