# BookMyShow Backend — Intern Tutorial

Welcome. You are about to read a real backend service end to end: every table, every
endpoint, every layer, and — importantly — every place where it is *wrong*.

This tutorial deliberately does **not** teach you what `@RestController` does. You can
Google that in ten seconds. What you cannot Google is:

- why *this* project has two completely separate database connection systems,
- why `synchronized` on a booking method looks correct and is useless in production,
- why `mvn package` on this repo fails today, and what that failure teaches you about builds,
- why the seat-availability check has a one-word bug that lets you steal another
  customer's confirmed ticket.

That is the gap this tutorial fills: **project context**, not API documentation.

---

## Who this is written for

You already know:

- basic Java (classes, interfaces, generics, collections, exceptions)
- basic Spring Boot (you have made a `@RestController` return JSON)
- basic Hibernate/JPA concepts (entities, repositories)
- basic MySQL (`SELECT`, `JOIN`, primary keys)

You have never:

- run a service in production
- deployed anything to a server and exposed it on an `ip:port`
- had to think about two users clicking "Book" on the same seat at the same instant
- been asked "why is this class designed this way?" and had a good answer

By the end you should be able to open any file in this repo, explain why each line is
there, and — more valuable — explain what you would change and why.

---

## Important: this project is a *prototype*, not a model to copy

Read this paragraph twice.

This codebase is a good learning vehicle precisely because it is honest prototype code.
It works well enough to demo, and it contains a dozen mistakes that are extremely
common in real teams. Some of them are the *exact* mistakes that cause 3 AM production
incidents. Throughout the tutorial you will see callouts like this:

> **🔴 Production trap** — something that would cause a real outage or data loss.
>
> **🟡 Smell** — works, but will hurt you as the codebase grows.
>
> **🟢 Good call** — something the author got right; notice it and reuse it.

Do not copy patterns from this repo into new work without reading the callout attached
to them.

---

## What has already been fixed

Some of the problems this tutorial describes have been repaired in the code you are
holding, because they were dangerous enough that nobody should run the project without
them fixed. Where that is the case, the tutorial shows you **what was wrong, why it was
wrong, and what the fix looks like** — you get the analysis without having to hunt.

| Fixed | Was | Now | Chapter |
|-------|-----|-----|---------|
| Build on modern JDKs | Lombok 1.18.10 failed on JDK 9+ | Lombok 1.18.30 | 01 |
| Startup | `CustomerService` had no `@Service`, so the context failed to build | annotated | 01, 06 |
| Schema management | every startup dropped and recreated all 11 tables, destroying all data | Flyway forward-only migrations | 01, 11 |
| Seat availability | the rule was implemented twice, and one copy reported a **paid seat as free** | one shared rule in `BookingUtils.occupiesSeat` | 08 |
| Seat claiming | an existing seat row was reassigned to the new booking, silently stealing a confirmed ticket | only genuinely free rows are reused | 08 |

**Everything else in this tutorial is still true of the code**, including the biggest
remaining problem: booking is still not safe under concurrency. `synchronized` still
guards the booking methods, the check and the write still happen in different
transactions, and the TOCTOU race of chapter 08 is still open. That is deliberate — it is
Exercise 18, and it is the most valuable thing in the curriculum.

Chapter 08 is the one to read most carefully: it now contains both a fixed bug you can
study and a live one you have to fix.

---

## Reading order

Work through these in order. Sections 1–7 are "understand what exists". Sections 8–13
are "understand what production demands". Section 14 is where you actually do the work.

| # | File | What you get out of it |
|---|------|------------------------|
| 01 | [Setup and first run](01-setup-and-first-run.md) | Get it compiling, get MySQL up, make your first successful API call. Includes every error you will hit, with the real message. |
| 02 | [How a request flows](02-how-a-request-flows.md) | LLD. Follow one HTTP request from the TCP socket to the SQL statement and back. The mental model everything else hangs off. |
| 03 | [Domain model and DB schema](03-domain-model-and-db-schema.md) | Every table, every column, every index — and why the `Show → LiveShow → ScheduledLiveShow` chain exists. This is the single most important design idea in the repo. |
| 04 | [API reference](04-api-reference.md) | All 16 endpoints with real request/response payloads, plus what is wrong with each signature. |
| 05 | [User flows](05-user-flows.md) | End-to-end sequences: browse → register → login → hold seats → pay → view ticket. With the actual curl commands. |
| 06 | [Code tour](06-code-tour.md) | Package by package, class by class. Why each layer exists and what belongs in it. |
| 07 | [The MyBatis data layer](07-mybatis-data-layer.md) | The generated code, the two connection systems, sessions, transactions, the type handler. The most project-specific chapter. |
| 08 | [Concurrency and the booking race](08-concurrency-and-the-booking-race.md) | The hardest and most interesting chapter. Seat holds, TOCTOU races, why `synchronized` fails, and the four real fixes. |
| 09 | [HLD / system design](09-hld-system-design.md) | Zoom out. How this becomes a system that serves a million users on ticket-opening day. |
| 10 | [Design principles in this code](10-design-principles-in-this-code.md) | SOLID, open–closed, DI — taught *only* through concrete refactors of files in this repo. No abstract shape/animal examples. |
| 11 | [Production readiness](11-production-readiness.md) | Config, secrets, logging, error handling, validation, migrations, security. The gap between "runs on my laptop" and "runs for customers". |
| 12 | [Running on a server](12-running-on-a-server.md) | Processes, ports, `0.0.0.0` vs `127.0.0.1`, systemd, nginx, firewalls, TLS, scaling to more than one instance. Written for someone who has never done it. |
| 13 | [Testing](13-testing.md) | What the one existing test does, why most of this code is untestable, and how design causes that. |
| 14 | [Exercises](14-exercises.md) | 20 graded tasks, from "make it run" to "make booking correct under concurrency". This is the real curriculum. |
| 15 | [Answer key](15-answer-key.md) | Solutions and explanations. Do not open early — the struggle is the lesson. |
| 16 | [Glossary](16-glossary.md) | Every term used, defined plainly. |

---

## How to use this well

**Read with the code open.** Every claim in these files carries a `file:line`
reference, like `services/BookingService.java:49`. Open it. Confirm it. If a reference
looks wrong to you, you may have found something the tutorial missed — say so.

**Do not trust the tutorial blindly.** Several statements are phrased as "verify this
yourself" on purpose. Verifying claims about a codebase is the core skill of this job.

**Keep a running "questions" file.** Anything you cannot answer after reading the
relevant chapter is a good question to bring to your mentor. Vague confusion is not; a
specific "why does `SeatDAO:66` construct a DAO with `new` when Spring could inject it?"
is excellent.

**Type the commands.** Do not read the curl examples — run them, break them, change a
field, see what the API does with garbage input. You will learn more from one 500 error
you caused than from five chapters.

---

## The 90-second summary of the whole system

So you have a skeleton to hang details on:

A **cinema** contains **halls**. A hall has a grid of **seats** (rows × columns), created
once when the hall is created. A **show** is a movie ("Avenger"). Putting that movie into
a specific hall makes it a **live show**. Giving that live show a start and end time and
a ticket price makes it a **scheduled live show** — which is the thing a customer
actually buys a ticket to.

A **customer** registers and logs in. To buy, they pick a scheduled live show and a set
of seat IDs. The service creates a **show booking** in status `INITIATED` and one
**seats booking** row per seat, also `INITIATED`. Those rows constitute a *hold*: for
five minutes, `INITIATED` + recently modified means "someone else is mid-checkout, hands
off". The customer then calls the payment endpoint; a mocked payment always succeeds,
and the booking and its seats flip to `SUCCESS`. If they take longer than five minutes,
the hold has lapsed and the booking is rejected.

There is no background job that expires holds. Expiry is computed at read time by
comparing `modified_at` against the current clock. That single design decision has
consequences all over the codebase, and chapter 08 is largely about them.

Everything else is detail. Start with [01-setup-and-first-run.md](01-setup-and-first-run.md).
