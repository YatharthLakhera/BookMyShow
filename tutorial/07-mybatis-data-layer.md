# 07 — The MyBatis Data Layer

This is the chapter you cannot Google. Everything here is specific to how *this* project
wires itself to MySQL, and almost all of it is done in an unusual way.

---

## First: what is MyBatis, and why is it here instead of JPA?

You know Hibernate/JPA. It is an **ORM** — you annotate `@Entity` classes, and the
framework generates SQL, manages a persistence context, tracks dirty objects, and handles
lazy loading. You mostly do not write SQL.

MyBatis is a **SQL mapper**, a deliberately smaller idea. *You* own the SQL; MyBatis maps
between statement parameters and Java objects, and back from result sets to objects. There
is no persistence context, no dirty tracking, no lazy loading, no magic.

|  | JPA / Hibernate | MyBatis |
|--|-----------------|---------|
| Who writes the SQL | the framework | you |
| Tuning a slow query | fight the ORM's generated SQL | edit the SQL |
| Object graph loading | automatic, lazy | explicit, manual |
| Surprising behaviour | plenty (N+1, `LazyInitializationException`, cascade semantics) | little |
| Boilerplate | little | more |
| Learning curve | steep, and long | shallow |

Neither is "better". Teams choose MyBatis when queries are complex or performance-critical
and they want to see exactly what hits the database; they choose JPA when the domain maps
cleanly to tables and they want the productivity.

> **A genuinely confusing detail in this project:** `pom.xml` declares
> **`spring-boot-starter-data-jpa`** (line 78). JPA and Hibernate are on the classpath and
> are being auto-configured at startup — and **nothing in the codebase uses them.** There
> is not one `@Entity`, not one `Repository` interface. The dependency is dead weight that
> slows startup, enlarges the artifact, and actively misleads readers into thinking this
> is a JPA project.
>
> There is a second: `spring-boot-starter-test` is declared **twice** (lines 36 and 102),
> once with `<scope>test</scope>` and an exclusion, once with neither. Maven warns about
> it on every build:
> ```
> [WARNING] 'dependencies.dependency.(groupId:artifactId:type:classifier)' must be unique:
> org.springframework.boot:spring-boot-starter-test:jar -> duplicate declaration of version (?) @ line 102
> ```
> The second declaration wins, so the exclusion is discarded and the test starter is on the
> **compile** classpath, meaning JUnit and Mockito ship inside your production artifact.
>
> **Lesson: read your build warnings.** They are not noise. Run `mvn dependency:tree`
> occasionally and ask what each top-level dependency is for. Unused dependencies are not
> free — they are attack surface (every one is code you ship and must patch for CVEs),
> startup time, and confusion for the next reader.

---

## The two database stacks

This is the single most important structural fact in the codebase. **This project connects
to MySQL two different ways, and they know nothing about each other.**

### Stack A — Spring's DataSource (configured, mostly unused)

`application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/movie_booking
    username: root
    password: ubona123
    driver-class-name: com.mysql.jdbc.Driver
```

Spring Boot sees this plus a JDBC driver on the classpath and auto-configures a
`DataSource` — specifically a **HikariCP connection pool**. `BookMyShowApplication`
injects it:

```java
@Autowired
private DataSource datasource;
```

…and then **never uses the field**. Not once. Verify:

```bash
grep -rn "datasource" src/main/java --include="*.java"
```

### Stack B — the hand-rolled ConnectionFactory (what actually runs your queries)

`db/ConnectionFactory.java`:

```java
public enum ConnectionFactory {
    INSTANCE;

    private SqlSessionFactory sqlSessionManager;

    public void init() throws Exception {
        if (sqlSessionManager == null) {
            synchronized (ConnectionFactory.class) {
                if (sqlSessionManager == null) {
                    String currentProjectPath = System.getProperty("user.dir");
                    String DB_PROPERTIES_FILE  = currentProjectPath + "/src/main/resources/db.properties";
                    String MYBATIS_CONFIG_FILE = currentProjectPath + "/src/main/resources/mybatisConfig.xml";
                    String ENVIRONMENT_TYPE = "environmentType";

                    Reader mybatisConfig = new FileReader(new File(MYBATIS_CONFIG_FILE));
                    Properties dbProperties = new Properties();
                    String environmentType = dbProperties.getProperty(ENVIRONMENT_TYPE);   // ← reads
                    dbProperties.load(new FileReader(new File(DB_PROPERTIES_FILE)));       // ← then loads
                    sqlSessionManager = new SqlSessionFactoryBuilder()
                            .build(mybatisConfig, environmentType, dbProperties);
                }
            }
        }
    }

    public SqlSession getSqlSession() { return sqlSessionManager.openSession(); }
}
```

Every DAO uses **this**, never the Spring `DataSource`.

There is a lot packed into 25 lines. Let us take it apart.

### The enum singleton idiom

```java
public enum ConnectionFactory { INSTANCE; ... }
```

A single-element enum is the recommended way to write a singleton in Java (*Effective
Java*, Item 3). The JVM guarantees exactly one instance, and it is free from the problems
that plague hand-written singletons — it is thread-safe by construction, immune to
reflection attacks, and correct across serialization.

> **🟢 Good call on the idiom** — and a good demonstration that "unusual-looking" is not
> the same as "wrong". This is textbook.
>
> **🟡 But a singleton was the wrong choice here.** Spring exists to manage exactly this
> kind of object. A `@Bean` that returns a `SqlSessionFactory` would give you the same
> single instance, plus lifecycle management, plus initialisation ordering, plus the
> ability to substitute a test factory pointing at an in-memory database. A static
> singleton reached via `ConnectionFactory.INSTANCE` from inside DAO methods is a global
> variable — and it is the reason (chapter 13) that **no DAO in this project can be unit
> tested.** There is no seam to substitute at.

### Double-checked locking

```java
if (sqlSessionManager == null) {
    synchronized (ConnectionFactory.class) {
        if (sqlSessionManager == null) { ... }
    }
}
```

This is the **double-checked locking** pattern: a cheap unsynchronised check to avoid
paying for a lock on every call, then a lock, then a re-check because another thread may
have initialised the field while you waited.

> **🔴 It is broken here, and the reason is worth learning.** For double-checked locking to
> be correct, the field **must be declared `volatile`**. It is not (line 18:
> `private SqlSessionFactory sqlSessionManager;`).
>
> Without `volatile`, the Java Memory Model permits the compiler and CPU to reorder the
> writes involved in constructing the object. Another thread can observe
> `sqlSessionManager != null` — and therefore skip initialisation and use it — while the
> object it points to is **still partially constructed**. You get a non-null reference to
> a half-built factory, and a failure that is intermittent, load-dependent, and impossible
> to reproduce on demand.
>
> This is one of the most famous bugs in Java (search "double-checked locking is broken"
> for the long history). Two ways to get it right: add `volatile`, or — far better — do not
> write lazy initialisation by hand at all. Let Spring build the object eagerly at startup.
>
> Here the bug is largely masked because `init()` is called once from `main()` before
> traffic arrives. That is luck, not design. **Concurrency bugs that are masked by
> accident are still bugs**, and they surface the moment someone calls `init()` from
> somewhere else.

### The initialisation-order bug

```java
Properties dbProperties = new Properties();
String environmentType = dbProperties.getProperty(ENVIRONMENT_TYPE);   // line 31 — reads
dbProperties.load(new FileReader(new File(DB_PROPERTIES_FILE)));       // line 32 — loads
```

Line 31 reads a property from an **empty** `Properties` object; line 32 then loads the
file. `environmentType` is therefore always `null`.

`db.properties` contains `environmentType=development`, and the intent was clearly to
select the MyBatis environment by name. (Note that `${url}`, `${username}` and `${password}`
*do* resolve correctly — they are substituted by `build()`, which runs after the `load`.) Passing `null` makes
`SqlSessionFactoryBuilder.build()` fall back to the `default` environment declared in
`mybatisConfig.xml`:

```xml
<environments default="development">
```

…which happens to be `development` too. **So the bug has no visible effect today.** Swap
the two lines and nothing changes. But the moment someone adds a `production` environment
and sets `environmentType=production` in a deployed properties file, the app will silently
keep using `development` — pointing production traffic at whatever `development` names.
A configuration that is *silently ignored* is far worse than one that fails loudly.

> **The general lesson: config that is read but not applied is a time bomb.** When you
> introduce a configuration knob, write a test or a startup log line that proves it took
> effect. `log.info("Using MyBatis environment: {}", environmentType)` would have made this
> obvious on the first run.

### The filesystem-path problem

```java
String currentProjectPath = System.getProperty("user.dir");
```

Covered in chapter 01: `user.dir` is the process's working directory, not the location of
the code, so the application only works when launched from the project source root. The
files are already inside the jar; the code just does not read them from the classpath.
This is still true of `db.properties` and `mybatisConfig.xml` and is Exercise 6 — note that
Spring's own `application.yaml` has never had the problem, because Spring loads it from the
classpath.

---

## `<dataSource type="UNPOOLED">` — the performance headline

`mybatisConfig.xml`:

```xml
<dataSource type="UNPOOLED">
    <property name="driver" value="com.mysql.jdbc.Driver" />
    <property name="url" value="jdbc:mysql://localhost:3306/movie_booking" />
    ...
</dataSource>
```

**UNPOOLED means: open a brand-new database connection for every request, and close it
afterwards.** MyBatis offers `POOLED` and `JNDI` as alternatives; this project chose the
one you should never use in a server.

### What a connection actually costs

Opening a MySQL connection is not cheap. It requires:

1. A TCP three-way handshake.
2. The MySQL protocol handshake — server sends its greeting and capabilities.
3. Authentication — a challenge/response round trip against the password.
4. Session setup — character set, timezone, SQL mode negotiation.

That is **several network round-trips before a single byte of your query is sent**. On
localhost it is roughly 1–5 ms. To a database in another availability zone, 10–50 ms. With
TLS enabled (which any production database requires), add a full handshake on top.

Now recall the DAO pattern — one session, therefore one connection, per method call:

```java
public Hall getHallById(int hallId) {
    @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();  // CONNECT
    ...
}                                                                                 // CLOSE
```

And recall `BookingService.getShowDetails()` from chapter 05: seven DAO calls to render
one ticket. **Seven full connect/authenticate/close cycles.** `GET /customer/1/history`
for a customer with 20 bookings: about 140.

### Measure it yourself

Turn on connection logging in `application.yaml`:

```yaml
logging:
  level:
    java.sql.Connection: DEBUG
    java.sql.PreparedStatement: DEBUG
```

Then:

```bash
curl -s http://localhost:8080/cinemas > /dev/null
```

Count the `Opening JDBC Connection` lines. With four cinemas × one hall × three showtimes,
expect somewhere in the dozens. Then:

```bash
mysql -u root -p -e "SHOW GLOBAL STATUS LIKE 'Connections';"
# hit the endpoint a few times
mysql -u root -p -e "SHOW GLOBAL STATUS LIKE 'Connections';"
```

The delta is how many connections your one page view cost.

### What a connection pool does

A pool opens N connections once at startup and keeps them open. Borrowing one is a
hashmap-speed operation — microseconds instead of milliseconds. "Closing" returns it to
the pool rather than tearing down the socket.

Spring Boot already configured one for you (HikariCP, via `spring.datasource`) and the
code ignores it. The fix is to make the DAOs use Spring's `DataSource` — which is the same
change that makes `@Transactional` work. See the migration section below.

> **This also explains a production failure mode you should recognise.** MySQL has a
> `max_connections` limit (default 151). With UNPOOLED and 200 Tomcat threads, a traffic
> spike can drive concurrent connection attempts past that limit, and MySQL starts
> rejecting with `Too many connections`. Every request then fails — including the health
> check, so your load balancer pulls the instance out, sending its traffic to the
> remaining instances, which then exceed the limit too. That is a **cascading failure**,
> and it is why bounded pools matter: a pool of 20 means the database sees at most 20
> connections per instance, no matter how much traffic arrives. Excess requests queue at
> the pool instead of overwhelming the database. **Bounding your resource usage is how you
> fail gracefully instead of catastrophically.**

---

## `@Cleanup` — what it compiles to

```java
@Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
```

Lombok's `@Cleanup` compiles to roughly:

```java
SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
try {
    ...rest of the method...
} finally {
    if (sqlSession != null) sqlSession.close();
}
```

Equivalent to try-with-resources. Its job is to guarantee the session closes even if an
exception is thrown, which prevents connection leaks — and a leaked connection is a slow,
mysterious outage.

> **🟢 Correct use.** Any resource with a `close()` — connections, streams, sockets, files
> — must be closed on **every** path including exceptions. `@Cleanup` or try-with-resources
> does that; a bare `close()` at the end of a method does not, because an exception skips
> it.

> **🟡 But note what closing means here.** Because the session was opened from an UNPOOLED
> data source, `close()` tears down the physical connection. And critically:
> **`SqlSession.close()` rolls back any uncommitted transaction.** That is why every DAO
> that writes must call `sqlSession.commit()` explicitly — miss it, and `@Cleanup` silently
> discards your work. Scan the write paths:
>
> ```bash
> grep -rn "sqlSession.commit()" src/main/java
> ```
>
> Check each `insert`/`update` DAO method has one. `ShowBookingDAO.insertBookingEntry()`
> (line 63) does **not** commit — but it receives the session from
> `initiateBooking()`, which commits after inserting the seats. That is deliberate and
> correct: it is what makes the booking and its seat rows a single atomic unit. It is also
> the *only* place in the codebase where two writes share a transaction.

---

## Transactions: where this design really hurts

A transaction is a group of statements that either all take effect or none do. The
classic example is a bank transfer — debit and credit must not be separable.

In this codebase the booking insert is such a group:

```java
public ShowBooking initiateBooking(BookingRequest req, double ticketPrice) throws BookingException {
    SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
    if (seatBookingDAO.areSeatsAvailable(req.getSeatIdList(), req.getScheduledLiveShowId())) {   // session #1
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();            // session #2
        ShowBooking showBooking = insertBookingEntry(req, ticketPrice, sqlSession);             //   write A
        int seatEntries = seatBookingDAO.insertSeatsForBooking(
                req.getSeatIdList(), showBooking, sqlSession);                                  //   write B
        sqlSession.commit();                                                                    //   commit both
        return showBooking;
    } else {
        throw new BookingException(ErrorMessages.SEAT_NOT_AVAILABLE);
    }
}
```

Writes A and B share `sqlSession`, so they commit together. Good — a booking can never
exist without its seat rows.

**But look where the availability check happens: on a different session, before the
transaction even opens.** That is the race condition, and chapter 08 is devoted to it.

And now the deeper structural problem:

> **🔴 `@Transactional` does not work in this codebase.**
>
> Spring's declarative transaction management works by binding a connection to the current
> thread. `@Transactional` opens a transaction on a connection from **Spring's**
> `DataSource`, stashes it in a `ThreadLocal`, and Spring-managed data access (Spring JDBC,
> JPA, `mybatis-spring`) then finds and reuses that same connection.
>
> These DAOs call `ConnectionFactory.INSTANCE.getSqlSession()`, which opens a completely
> independent connection from MyBatis' own UNPOOLED data source. Spring has never heard of
> it. So you can annotate `ShowService.addNewShowToDB()` with `@Transactional`, watch
> Spring dutifully open and commit an empty transaction on a connection nobody uses, and
> your three inserts will still commit independently — with no error and no warning.
>
> **This is the real cost of bypassing the framework.** It is not style. It is that the
> framework's most valuable feature — correct, declarative transaction boundaries across
> multiple repository calls — is unavailable, and unavailable *silently*. The next
> developer will add `@Transactional`, see no error, and reasonably assume they are
> protected.

### What the migration looks like

Adopting `mybatis-spring-boot-starter` (already a dependency, line 73 of the pom) makes
the whole problem disappear:

```java
// 1. Delete ConnectionFactory entirely.

// 2. Point the starter at the generated mappers.
@SpringBootApplication
@MapperScan("com.project.bookmyshow.db.mappers")
public class BookMyShowApplication { ... }

// 3. Inject mappers directly — the starter creates a bean for each interface.
@Repository
public class HallDAO {
    @Autowired private HallMapper hallMapper;

    public Hall getHallById(int hallId) {
        return hallMapper.selectByPrimaryKey(hallId);      // no session management at all
    }
}

// 4. Transactions are now declarative and actually work.
@Service
public class ShowService {
    @Transactional
    public ShowDetail addNewShowToDB(AddShowRequest request) {
        Show show = addShowToDB(request);
        LiveShow liveShow = addLiveShowToDB(show.getShowId(), request.getHallId());
        addScheduledLiveShowToDB(liveShow.getLiveShowId(), request);
        return getShowDetails(show);      // all three commit together, or none do
    }
}
```

Everything you gain in one change: connection pooling, working `@Transactional`, no
session boilerplate in nine DAOs, no `user.dir` filesystem dependency, no
`mybatisConfig.xml`, no `db.properties`, and configuration consolidated into
`application.yaml` where it can be overridden by environment variables. This is
Exercise 15 and it is the highest-value refactor in the project.

> **Two things to know about `@Transactional` before you use it**, because both bite
> beginners:
>
> **1. Self-invocation does not work.** Spring implements `@Transactional` with a **proxy**
> — a wrapper object around your bean that starts a transaction, calls your method, and
> commits. Callers get the proxy, so the wrapper runs. But if method `a()` inside your
> class calls `this.b()`, that call goes directly to the real object, bypassing the proxy
> entirely, and `@Transactional` on `b()` does nothing. Same reason `@Transactional` on a
> `private` or `final` method is silently ignored — the proxy cannot intercept it. This
> catches everyone once.
>
> **2. Rollback rules are asymmetric.** By default Spring rolls back on `RuntimeException`
> and `Error`, but **commits** on checked exceptions. So `throws BookingException`
> (a checked exception, chapter 06) would **commit** your partial work on failure. Either
> make your exceptions unchecked or write `@Transactional(rollbackFor = BookingException.class)`.

---

## The generated mapper code, briefly

You will read these files, so here is enough to follow them.

**`CustomerDynamicSqlSupport`** provides typed column references:

```java
public static final class Customer extends SqlTable {
    public final SqlColumn<Integer> customerId = column("customer_id", JDBCType.INTEGER);
    public final SqlColumn<String>  email      = column("email", JDBCType.VARCHAR);
    public Customer() { super("TBL_Customer"); }
}
```

The `SqlColumn<T>` type parameter is what makes the DSL type-safe: `isEqualTo` on a
`SqlColumn<String>` demands a `String`, so `where(email, isEqualTo(42))` will not compile.
This is why the generated approach is worth its bulk — a whole class of typo bugs becomes
impossible.

**`CustomerMapper`** is an interface of `default` methods built on that support class:

```java
default Customer selectByPrimaryKey(Integer customerId_) {
    return SelectDSL.selectWithMapper(this::selectOne, customerId, name, email, password, role, createdAt, modifiedAt)
            .from(customer)
            .where(customerId, isEqualTo(customerId_))
            .build()
            .execute();
}
```

There is no implementation class. `sqlSession.getMapper(CustomerMapper.class)` returns a
**JDK dynamic proxy** — MyBatis creates an object implementing the interface at runtime,
where the annotated abstract methods (`@SelectProvider`, `@InsertProvider`, …) execute SQL
and the `default` methods run as written. Understanding "the implementation is generated at
runtime" removes most of the mystery from MyBatis, Spring Data, and Spring's own AOP.

**`insertSelective` vs `insert`** is a distinction that matters:

```java
default int insert(Customer record)            // every column, nulls included
default int insertSelective(Customer record)   // only non-null fields  ← what the DAOs use
```

`insertSelective` omits null fields from the `INSERT` statement, which lets the database
apply its `DEFAULT` values. That is why `created_at`/`modified_at` get
`CURRENT_TIMESTAMP` and `role` gets `'USER'` even though the code never sets them. Use
`insert` and you would write explicit `NULL`s and get constraint violations.

**How generated inserts return the new ID:**

```java
@InsertProvider(type=SqlProviderAdapter.class, method="insert")
@SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.customerId", before=false, resultType=Integer.class)
int insert(InsertStatementProvider<Customer> insertStatement);
```

`@SelectKey` with `before=false` runs `SELECT LAST_INSERT_ID()` **after** the insert and
writes the result back into your object. That is why `ShowBookingDAO.insertBookingEntry()`
can insert a `ShowBooking` and then immediately use `showBooking.getBookingId()` on the
next line — MyBatis populated it. (`LAST_INSERT_ID()` is per-connection, so it is safe
under concurrency.)

---

## The custom type handler

`db/typehandlers/AmountTypeHandler.java` converts between the `int` paisa in MySQL and
`Double` rupees in Java. It is registered in `generatorConfig.xml`, not in code:

```xml
<columnOverride column="tickets_price" javaType="Double"
                typeHandler="com.project.bookmyshow.db.typehandlers.AmountTypeHandler"
                jdbcType="INTEGER"/>
```

```java
public void setParameter(PreparedStatement ps, int i, Double t, JdbcType jdbcType) throws SQLException {
    ps.setObject(i, (int) (t * 100));
}

public Double getResult(ResultSet rs, String s) throws SQLException {
    return Integer.parseInt(rs.getString(s)) / 100.0;
}
```

> **Type handlers are a genuinely useful MyBatis feature** — the right place to put a
> conversion that must apply everywhere a column is read or written. This one has four
> bugs.
>
> **1. `Double` is the wrong Java type for money** (chapter 03). The database gets it
> right and the handler undoes that.
>
> **2. `(int)(t * 100)` truncates rather than rounds.** `250.99 * 100` in IEEE-754 is
> `25098.999999999996`, and the cast to `int` truncates to `25098`. You have just lost a
> paisa. Multiply that across a million transactions and your books do not balance. If you
> must do this, `Math.round()`.
>
> **3. `rs.getString()` then `Integer.parseInt()`** takes an integer column, asks the
> driver to render it as text, then parses it back. Pointless work, and it introduces a
> locale/format dependency where none existed. Use `rs.getInt(s)`.
>
> **4. No null handling.** If the column is NULL, `rs.getString()` returns `null` and
> `Integer.parseInt(null)` throws `NumberFormatException` — not a helpful error, and thrown
> from deep inside the mapping layer. A type handler must always handle null; the
> conventional way is to extend `BaseTypeHandler<T>`, which handles nulls for you and
> leaves you only the value cases.
>
> Exercise 13 rewrites this class.

---

## The N+1 problem, concretely

You have now met the two multipliers. Put them together.

`GET /cinemas` executes, roughly:

```
1 query   : SELECT * FROM TBL_Cinema WHERE is_open = true            → 4 cinemas
  per cinema (×4):
    1 query : SELECT * FROM TBL_Hall WHERE cinema_id = ?             → 1 hall
      per hall (×1):
        1 query : SELECT * FROM TBL_LiveShow WHERE hall_id = ?       → 1 live show
          per live show (×1):
            1 query : SELECT * FROM TBL_ScheduledLiveShow WHERE live_show_id = ?
```

That is 1 + 4 + 4 + 4 = **13 queries and 13 connections** for four cinemas. The shape is
`1 + N + N×M + N×M×P` — it multiplies with your data. Four hundred cinemas with eight
halls each: over three thousand queries, each with its own TCP connect and MySQL
authentication, all on one Tomcat thread while the user waits.

**This is the N+1 problem**: one query to fetch a list, then N more to fetch each item's
children. It is the most common performance bug in backend code, in every language and
every ORM.

**Three standard fixes:**

**1. Join.** Fetch everything in one statement and assemble the object graph in memory:

```sql
SELECT c.cinema_id, c.cinema_name, h.hall_id, h.hall_code, s.scheduled_live_show_id, s.show_start_time
FROM TBL_Cinema c
JOIN TBL_Hall h              ON h.cinema_id = c.cinema_id AND h.is_available = 1
JOIN TBL_LiveShow lv         ON lv.hall_id = h.hall_id
JOIN TBL_ScheduledLiveShow s ON s.live_show_id = lv.live_show_id
WHERE c.is_open = 1
```

One query, one connection. MyBatis supports mapping this into a nested object graph
directly via `<collection>` in a result map — and this is exactly the kind of query MyBatis
is *good* at and an ORM makes awkward. If the project is going to use MyBatis, it should
use it for this.

**2. Batch the children.** Fetch all cinemas, collect their IDs, then one query for all
their halls: `WHERE cinema_id IN (1,2,3,4)`. Group in memory. Turns 1+N into 1+1 per
level, and is often easier to retrofit than a big join.

**3. Cache.** Cinema and hall data changes maybe once a month, and is read on every page
load. A cache with a short TTL removes most of the load. But **fix the query pattern
first** — caching an N+1 just hides it until the cache misses, and cache misses cluster
(a restart, a deploy, an eviction storm), so you get a thundering herd exactly when you
can least afford it.

> **How to find N+1 in any codebase:** turn on SQL logging, exercise one endpoint, count
> the statements. If the count grows with the size of the result set, you have N+1. Make
> this a habit — it takes two minutes and finds the single most common performance bug
> there is.

---

## Checkpoint

- [ ] Explain the difference between an ORM and a SQL mapper
- [ ] Name both database stacks in this project and say which one the DAOs use
- [ ] Explain why `@Transactional` would silently do nothing here
- [ ] Explain what UNPOOLED costs, and what a pool changes
- [ ] Explain why double-checked locking needs `volatile`
- [ ] Count the queries behind `GET /cinemas` and identify the N+1 shape

Next: [08-concurrency-and-the-booking-race.md](08-concurrency-and-the-booking-race.md) —
the hardest and most valuable chapter.
