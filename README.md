# Hospital Appointment Booking System

A full-stack web application where patients can search doctors by specialty and book or cancel appointment slots. Doctors manage their availability, and admins monitor activity through a reports dashboard.

## Tech Stack

| Layer        | Technology                        |
|-------------|-----------------------------------|
| Backend     | Java 17, Spring Boot 3.2          |
| Security    | Spring Security + JWT             |
| Database    | PostgreSQL                        |
| ORM         | Spring Data JPA / Hibernate       |
| Frontend    | Thymeleaf + Bootstrap             |
| Email       | JavaMail (Gmail SMTP)             |
| Docs        | Swagger / OpenAPI 3               |
| Testing     | JUnit 5 + Mockito                 |
| Containers  | Docker + Docker Compose           |
| Deployment  | Render (free tier)                |

## Key Features

- Role-based auth (Patient / Doctor / Admin) with JWT
- Doctor search & filter by specialty
- Slot management with double-booking prevention
- Appointment booking, rescheduling & cancellation
- Automated email reminders
- Admin dashboard with reports
- RESTful APIs documented with Swagger
- Unit & integration tests

## Quick Start

```bash
# Start PostgreSQL + app with Docker Compose
docker-compose up --build

# Or run locally (requires PostgreSQL on localhost:5432)
mvn spring-boot:run
```

Open http://localhost:8080

## API Docs

http://localhost:8080/swagger-ui.html

