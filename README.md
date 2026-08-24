# BookMyShow

This project is the Prototype for BookMyShow Backend Design which containing :

- DB Design with sample data
- API exposing for frontend

This microservice is built using Spring Boot for REST API, Mybatis3 for DB interaction and Mockito for Testing.

## Tutorial

A full walkthrough of this codebase lives in [`tutorial/`](tutorial/) — written for someone
joining the project who knows Java, Spring Boot and MySQL but has not run a service in
production. It covers the API surface, the database design, the request lifecycle, the
booking concurrency model, and what would need to change before this served real customers.

Start at [`tutorial/README.md`](tutorial/README.md).

## Running locally

Requires JDK 11+ and MySQL. Create the database, then start the app with the `local`
profile to get the sample cinemas and shows:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS movie_booking;"
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The schema is managed by Flyway (`src/main/resources/db/migration`) and is applied
automatically at startup. Migrations are forward-only — nothing is ever dropped.

Database credentials come from the `DB_URL`, `DB_USER` and `DB_PASSWORD` environment
variables, defaulting to a local MySQL if unset.

## API's

This project provides with the working implementation for following API's :

1. Getting all the Movie details.
  - This functionality is implemented in multiple formats like :
    a. List of Cinemas and the halls the cinema contains and at what timings which movie will be played in those halls.
    b. List of Movies and in which cinemas and containing halls and timings at which the particular movie will play.
2. Getting seat arrangement and seats status details.
  - This gives all the seats available for a particular movie playing at a particular time.
3. Customer registration
4. Customer Login
5. Initiation of Booking for a movie and the number of seats.
  - This API holds the seats for which the booking is initiate for 5 mins which can be configured.
6. Payment of the Initiated Booking
  - This API confirms the seats for which booking is initiated if done in 5 mins of initiating(configured hold time) else the booking is rejected.
