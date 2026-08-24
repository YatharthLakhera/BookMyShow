# 13 — Testing

There is exactly one test class in this project. Reading it, and understanding why there
is only one, teaches more about design than about testing.

---

## What exists

Two test classes:

- `BookMyShowTestApplication.java` — two tests for `CustomerService`, written by the
  original author.
- `BookingUtilsTest.java` — eight tests added alongside the seat-availability fix in
  chapter 08. Read it after this chapter; it is what a focused unit test should look like.

Start with the original.

```java
@RunWith(MockitoJUnitRunner.class)
public class BookMyShowTestApplication {

    @Mock        private CustomerDAO customerDAO;
    @InjectMocks private CustomerService customerService;

    @Before
    public void init() { MockitoAnnotations.initMocks(this); }

    @Test
    public void getCustomerByIdTest() {
        int customerId = 1;
        String email = "testuser1@gmail.com", pass = "TestPass1";
        when(customerDAO.getCustomerBy(email, pass)).thenReturn(getCustomerByID(customerId, email, pass));

        CustomerDetail customerDetail = customerService.getCustomerDetails(email, pass);

        Assert.assertEquals(email, customerDetail.getCustomerEmail());
        Assert.assertEquals(customerId, customerDetail.getCustomerId());
    }
}
```

### What it does, piece by piece

**`@Mock CustomerDAO`** — Mockito creates a fake `CustomerDAO`. Every method returns a
default (`null`, `0`, `false`) until you tell it otherwise. **No database is involved.**

**`@InjectMocks CustomerService`** — Mockito instantiates the real `CustomerService` and
pushes the mocks into its `@Autowired` fields **by reflection**. No Spring context starts.

**`when(...).thenReturn(...)`** — "when this exact call happens, return this". This is
**stubbing**: you script the collaborator so you can test your class in isolation.

**The Arrange–Act–Assert shape** — set up the world, perform one action, assert on the
result. Every good unit test has these three visible sections; when you cannot see them,
the test is usually doing too much.

**What makes this a *unit* test:** no database, no HTTP server, no filesystem, no clock, no
network. It runs in milliseconds and its result depends only on the code under test.

> **🟢 Good call.** This is a correct, well-formed unit test, and it demonstrates the
> payoff of layering: `CustomerService` could be tested precisely because its dependency
> was an injectable field rather than something it constructed itself.

---

## What is wrong with it

**1. `MockitoAnnotations.initMocks(this)` is redundant.** `@RunWith(MockitoJUnitRunner.class)`
already initialises the mocks. Calling both re-creates them, which is harmless here but
would silently discard any stubbing done in a `@Before` that ran earlier. (`initMocks` is
also deprecated in favour of `openMocks`.)

**2. `addCustomerTest` asserts either outcome.**

```java
try {
    ErrorResponse errorResponse = customerService.addCustomerToDB(...);
    Assert.assertEquals(ErrorMessages.USER_REGISTER_SUCCESSFUL, errorResponse.getErrorMessage());
} catch (CustomerException e) {
    Assert.assertEquals(ErrorMessages.USER_ALREADY_REGISTERED, e.getMessage());
}
```

This passes whether registration succeeds **or** throws. A test that accepts both branches
asserts nothing about which one happened — it only checks that if an exception occurs, its
message is one of two strings.

> **A test that cannot fail is worse than no test.** It costs runtime, it appears in the
> coverage report, and it gives false confidence. Write two tests, each with a definite
> expectation:
>
> ```java
> @Test
> public void registerNewCustomer_succeeds() throws Exception {
>     when(customerDAO.getCustomerBy(EMAIL, PASS)).thenReturn(null);      // not registered
>     ErrorResponse r = customerService.addCustomerToDB(request());
>     assertEquals(USER_REGISTER_SUCCESSFUL, r.getErrorMessage());
>     verify(customerDAO).insert(NAME, EMAIL, PASS);                      // and it wrote
> }
>
> @Test(expected = CustomerException.class)
> public void registerExistingCustomer_throws() throws Exception {
>     when(customerDAO.getCustomerBy(EMAIL, PASS)).thenReturn(existingCustomer());
>     customerService.addCustomerToDB(request());
> }
> ```
>
> Note `verify(...)`: `when` stubs an input, `verify` asserts an interaction happened.
> Together they let you test both what your code returned and what it *did*.

**3. 🔴 The class never runs.** This is the important one, and it is verified:

```bash
mvn test
# [INFO] Running com.test.bookmyshow.BookingUtilsTest
# [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

Eight tests — all from `BookingUtilsTest`. `BookMyShowTestApplication` is not there. Force
it and it passes fine:

```bash
mvn test -Dtest=BookMyShowTestApplication
# [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

So the tests work; **Maven simply never collects them.** Surefire's default include
patterns are `**/Test*.java`, `**/*Test.java`, `**/*Tests.java`, `**/*TestCase.java`.
`BookMyShowTestApplication` matches none of them — it neither starts with `Test` nor ends
with it. Renaming it to `CustomerServiceTest` is the entire fix.

> **A test suite that silently does not run is worse than having no tests**, because the
> build is green and everyone believes they are covered. Nothing errors, nothing warns —
> the count is just quietly lower than you think.
>
> The habit that catches this: **whenever you add a test, watch it fail first.** Break the
> assertion, run it, confirm red, then fix it. If you never see red, you have no evidence
> the test is connected to anything. That single discipline catches misnamed classes,
> misconfigured runners, and assertions that cannot fail — all three of which are present
> in this project.
>
> Also worth knowing: this project uses JUnit 4 (`org.junit.Test`, `@RunWith`) while Spring
> Boot 2.2 ships JUnit 5, so JUnit 4 tests run through the **vintage engine**. The pom tries
> to exclude vintage (line 40) but declares `spring-boot-starter-test` a second time without
> the exclusion (line 102), so the exclusion is discarded and vintage stays — which is why
> these run at all. It works by accident. Chapter 07 covers the duplicate declaration.

**4. The class name is misleading — and that is what breaks it.**
`BookMyShowTestApplication` sounds like a Spring Boot test application; it is a
`CustomerService` unit test and should be `CustomerServiceTest`, which would also make
Surefire collect it. The package (`com.test.bookmyshow`) differs from the main package
(`com.project.bookmyshow`) too — conventionally test packages mirror main packages exactly,
so tests sit beside what they test and can reach package-private members.

**Naming conventions are not cosmetic when a tool depends on them.**

---

## Why there is only one test

This is the important part.

**Almost nothing else in this codebase can be unit tested.** Not because nobody tried —
because the design forbids it. (`BookingUtilsTest` is the exception that proves the rule:
`BookingUtils` is pure, takes its inputs as parameters, and reaches for nothing global.)

### DAOs cannot be tested

```java
public Hall getHallById(int hallId) {
    @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
    ...
}
```

`ConnectionFactory.INSTANCE` is a static singleton reached from inside the method body.
There is no parameter, no field, no constructor argument — **no seam** where a test could
substitute a fake or point it at a test database. To test `getHallById` you must have a
real MySQL running, with the real schema, at the exact URL in `mybatisConfig.xml`, and you
must run the test from the project root directory (`user.dir`, chapter 07).

### `BookingService.finalizeBooking` cannot be tested

```java
PaymentService.Status status = PaymentService.processPayment();
```

A static call to a class the test cannot influence. You cannot write "what happens when
payment fails?" — which is *the* case worth testing — without extra machinery
(`mockito-inline` and `mockStatic`), and reaching for that is a signal the design is wrong
rather than a solution.

### `ShowBookingDAO.initiateBooking` cannot be tested

```java
SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
```

Constructed inside the method. Same problem: no seam.

> **The lesson, and it is the main lesson of this chapter:**
>
> **Testability is not a property you add to code. It is a consequence of design.**
>
> Code that receives its dependencies can be tested. Code that reaches out and grabs them —
> `new`, a static call, a singleton — cannot. That is the same property, viewed from a
> different angle, that chapter 10 called dependency inversion.
>
> This is why "I'll write the tests afterwards" so often fails. By the time you try, the
> design has already made it impossible, and writing the tests means rewriting the code.
> Trying to write a test is one of the fastest ways to discover that a design is too
> coupled — the difficulty is the feedback.

---

## The test pyramid

```
              ╱╲
             ╱E2E╲            few — slow, brittle, but prove the whole thing works
            ╱──────╲
           ╱ Integr.╲         some — real DB, real HTTP; catch wiring and SQL bugs
          ╱──────────╲
         ╱    Unit    ╲       many — fast, isolated; catch logic bugs
        ╱──────────────╲
```

**Unit** — one class, all collaborators mocked. Milliseconds. Run on every save.
**Integration** — several real components together (a real database, a real HTTP request).
Seconds. Run on every commit.
**End-to-end** — the whole system through its public interface. Minutes. Run before release.

The pyramid shape matters: many fast tests at the bottom, few slow ones at the top. Invert
it — a handful of unit tests and a large E2E suite — and your build takes an hour, failures
tell you "something broke somewhere", and people start ignoring red builds.

---

## What this project should have

### Unit tests (after making the code testable)

- ~~`BookingUtils.isOnHold` — the hold-expiry rule at the boundary~~ — **done**, see
  `BookingUtilsTest`. Read it: eight tests, no database, runs in 3 ms, and it pins down the
  rule that the chapter 08 bug got wrong. This is the shape to copy.
- `AmountTypeHandler` — round-tripping ₹250.99 without losing a paisa (this currently fails,
  chapter 07).
- `ApplicationUtils.getParsedDate` — valid input, invalid input, null.
- `CinemaService.getCinemaDetailsForShow` — the merge branch that currently throws
  `UnsupportedOperationException` (chapter 06). Write the test first; watch it fail; fix it.

### Integration tests

Use **Testcontainers**, which starts a real MySQL in Docker for the duration of the test:

```java
@Testcontainers
@SpringBootTest
class BookingIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void bookingHoldsSeatsAndPaymentConfirmsThem() { ... }
}
```

> **Why a real MySQL rather than H2 in-memory?** Because you are testing SQL behaviour, and
> H2 is not MySQL. It differs in unique-constraint handling, `ON UPDATE CURRENT_TIMESTAMP`,
> JSON columns, `ENUM` types, and locking semantics — precisely the features this schema
> depends on. A test suite that passes on H2 and fails on MySQL has taught you nothing.
> **Test against what you run in production.** Testcontainers makes that cheap.

The tests worth writing here, in priority order:

1. **The concurrency test from chapter 08** — N threads, one seat, exactly one winner. This
   test currently fails, which is the point.
2. **Hold expiry** — hold seats, advance time past the window, verify the seats are bookable
   again and the payment is rejected.
3. **The double-booking bug** — pay for a seat, book it again, assert rejection. Fails today
   (chapter 08).
4. **Transaction atomicity** — force a failure between the booking insert and the seat
   inserts; assert neither exists.

### Web-layer tests

```java
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private BookingService bookingService;
    @MockBean  private CustomerService customerService;

    @Test
    void unavailableSeatsReturn409() throws Exception {
        when(customerService.isCustomerRegistered(1)).thenReturn(true);
        when(bookingService.initateBooking(any())).thenThrow(new BookingException(SEAT_NOT_AVAILABLE));

        mockMvc.perform(post("/booking")
                        .contentType(APPLICATION_JSON)
                        .content("{\"customerId\":1,\"scheduledLiveShowId\":1,\"seatIdList\":[1]}"))
               .andExpect(status().isConflict());          // fails today — it returns 200
    }
}
```

`@WebMvcTest` starts only the web layer — no database, no full context — so these run fast.
`MockMvc` sends a request through the real Spring MVC stack without opening a port. This is
how you test status codes, JSON shape, and validation.

---

## How to write a good test

**Name it after the behaviour, not the method.**
`getCustomerByIdTest` → `loginWithValidCredentials_returnsCustomerDetail`. A failing test
name should tell you what broke without opening the file.

**One reason to fail per test.** If a test asserts five unrelated things, a failure requires
investigation to locate.

**Arrange–Act–Assert, visibly.** Three blocks, ideally separated by blank lines.

**Test behaviour, not implementation.** Assert on what the method returns and the
externally-visible effects it causes. A test that asserts on private fields or exact call
ordering breaks every time you refactor, which trains people to delete tests.

**Test the edges.** Zero, one, many, null, empty, maximum, and just-over-maximum. Bugs
cluster at boundaries — which is exactly why `isOnHold` should be tested at 4:59 and 5:01,
not at 2 minutes.

**Make failures informative.** `assertEquals(expected, actual)` beats
`assertTrue(expected.equals(actual))`, because the first prints both values.

> **Do not chase a coverage percentage.** Coverage tells you which lines *executed*, not
> which behaviours were *verified*. A test that calls every method and asserts nothing gives
> you 100% coverage and zero safety — `addCustomerTest` above is a small version of exactly
> that. Coverage is useful in one direction only: **0% coverage on a file definitely means
> it is untested.** High coverage does not mean the opposite.

---

## Running the tests

```bash
mvn test                                     # unit tests (surefire)
mvn verify                                   # + integration tests (failsafe)
mvn test -Dtest=BookingUtilsTest             # a single class
```

By convention, surefire runs `*Test` classes during `test`, and failsafe runs `*IT`
classes during `verify` — so slow integration tests do not run on every build.

> **Tests must run in CI, on a machine that has never seen your laptop.** That means no
> hardcoded paths, no dependence on a locally-running MySQL, no dependence on the working
> directory. This project fails all three: `ConnectionFactory` reads `user.dir`,
> `generatorConfig.xml` contains an absolute path to one person's home directory, and
> nothing provides a database. **If a test only passes on your machine, it is not a test —
> it is a ritual.**

---

## Checkpoint

- [ ] Explain what `@Mock` and `@InjectMocks` do, and why no Spring context starts
- [ ] Run `mvn test` and explain why only one of the two test classes appears
- [ ] Explain why `addCustomerTest` can never fail
- [ ] Explain why DAOs in this project cannot be unit tested, in one sentence about seams
- [ ] Explain the test pyramid and why inverting it hurts
- [ ] Explain why Testcontainers is preferable to H2 for this schema
- [ ] Write one real unit test for `BookingUtils.isOnHold` covering both sides of the boundary

Next: [14-exercises.md](14-exercises.md) — where you actually do the work.
