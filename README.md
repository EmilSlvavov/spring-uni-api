# spring-uni-api

A REST API for a university system, built with Spring Boot. It manages users, departments, courses (online and onsite) and enrollments. Access is secured with JWTs and role-based authorization.

## Tech stack

- Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security)
- Microsoft SQL Server (main database)
- Redis (stores refresh tokens)
- MapStruct and Lombok
- H2 in-memory database for tests

## Requirements

- JDK 21
- SQL Server running on localhost:1433 with a database named `university_db` and a login `hibernate_user` / `hibernate`
- Redis running on localhost:6379

## Environment variables

- `JWT_SECRET`: key used to sign access tokens (base64-encoded)
- `SEED_PASSWORD`: password given to the seeded users on first startup

## Running

    ./mvnw spring-boot:run

The app starts on http://localhost:8080. Hibernate creates and updates the tables automatically (`ddl-auto=update`).

## Seeded data

On first startup (when not using the `test` profile), the app creates the roles STUDENT, PROFESSOR and ADMIN. If the users table is empty, it also creates one user per role:

- student@email.test
- professor@email.test
- admin@email.test

All three use the password from `SEED_PASSWORD`.

## Authentication

- `POST /api/auth/register`: create an account
- `POST /api/auth/login`: returns an access token and a refresh token
- `POST /api/auth/refresh`: exchange a refresh token for a new pair (the refresh token is rotated)
- `POST /api/auth/logout`: invalidate the refresh token

Send the access token as `Authorization: Bearer <token>`. Access tokens last 15 minutes and refresh tokens last 7 days.

## Main endpoints

- `/api/users`: user CRUD, profile updates, enrolling in and leaving courses (`/api/users/{userId}/courses/{courseId}`), and changing a user's role (admin only)
- `/api/courses`: list, summary and details; create with `/online` or `/onsite`; update; soft delete and hard delete
- `/api/departments`: department CRUD and contact info
- `/api/roles`: read-only role lookup (admin only)

Most resources support soft delete: `PATCH` or `DELETE` on `/softDeleted/{id}`, and `GET /softDeleted/{true|false}` to list by deleted status.

## Permissions (summary)

- Public: reading courses and departments, all `/api/auth` endpoints
- STUDENT: manage their own account and enrollments
- PROFESSOR: create, update and soft-delete courses
- ADMIN: everything, including hard deletes, departments, roles and managing all users

## CORS

Requests are allowed from http://localhost:4200, which is where the Angular frontend runs.

## Tests

    ./mvnw test

Tests use an in-memory H2 database and the `test` profile, so SQL Server isn't needed.

## Postman

There's a ready-made request collection in `postman/spring-uni-api.postman_collection.json`.

## Project layout

- `controller`: REST endpoints
- `service`: business logic; `facade` holds services that coordinate several domains
- `repository`: Spring Data JPA repositories and projections
- `entity`: JPA entities
- `dto` and `mapper`: request/response objects and MapStruct mappers
- `security`: JWT filter, security config, authorization checks
- `exception`: custom exceptions and the global exception handler
- `logging`: request logging and correlation IDs
- `config`: data seeding and JPA auditing
