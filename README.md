# BookMyShow

This project is the Prototype for BookMyShow Backend Design which containing :

- DB Design with sample data
- API exposing for frontend

This microservice is built using Spring Boot for REST API, Mybatis3 for DB interaction and Mockito for Testing.

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
