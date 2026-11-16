# Hospital Appointment Booking System

A production-ready full-stack web application for hospital appointment management where patients can search doctors by specialty and book/reschedule/cancel slots. Doctors manage consultation schedules, and administrators track hospital metrics through an interactive reporting dashboard.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue.svg)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-green.svg)](https://swagger.io/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED.svg)](https://www.docker.com/)

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.5, Spring Data JPA |
| **Security** | Spring Security 6 with JJWT (Stateless Bearer Tokens) & BCrypt |
| **Database** | PostgreSQL 16 (H2 in-memory for unit/integration tests) |
| **Frontend** | Thymeleaf, HTML5, Bootstrap 5, Bootstrap Icons, Vanilla JS |
| **Notifications** | JavaMail (`JavaMailSender`) with asynchronous (`@Async`) delivery |
| **Scheduling** | `@EnableScheduling` with automated cron-based reminder tasks |
| **API Docs** | SpringDoc OpenAPI 3 / Swagger UI |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| **DevOps** | Multi-stage Dockerfile, Docker Compose, Render Blueprint (`render.yaml`) |

---

## 🌟 Key Features

1. **Role-Based Authentication (RBAC)**
   - Roles: `PATIENT`, `DOCTOR`, `ADMIN`
   - Secure stateless authentication with JSON Web Tokens (JWT) and BCrypt password hashing.
2. **Doctor Discovery & Specialty Search**
   - Filter specialists by specialty (Cardiology, Neurology, Pediatrics, Dermatology, etc.).
   - Full-text search by doctor name or qualifications.
3. **Double-Booking Prevention**
   - Synchronized atomic slot reservation flipping `available = false`.
   - Unique constraints on database level: `(doctor_id, slot_date, start_time)`.
4. **Appointment Management Lifecycle**
   - Booking with patient symptoms/notes.
   - Rescheduling to a new available slot with automatic release of the previous slot.
   - Cancellation with instant reopening of the slot for other patients.
5. **Automated Email Notifications**
   - Asynchronous booking confirmations, reschedule notices, and cancellation alerts.
   - Automated morning `@Scheduled` cron job sending reminders for appointments scheduled for tomorrow.
6. **Administrative Reporting Dashboard**
   - High-level metrics: total patients, total doctors, total bookings, cancellations.
   - Global appointments log with date-range filters and status controls.
7. **Interactive OpenAPI / Swagger Documentation**
   - Test endpoints directly from the browser with built-in JWT Bearer authorization.

---

## 🚀 Quick Start (Local Setup)

### Option 1: Run with Docker Compose (Recommended)
Make sure Docker Desktop is running, then execute:
```bash
docker-compose up --build
```
The application will start at `http://localhost:8080`.

### Option 2: Run with Maven & Local PostgreSQL
1. Ensure PostgreSQL is running on port `5432` with a database named `hospital_db`.
2. Start the application:
```bash
./mvnw spring-boot:run
```

---

## 🔑 Demo Seed Accounts

The system automatically initializes demo data on first boot:

| Role | Username | Password | Notes |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | System oversight & reports |
| **Patient** | `patient` | `patient123` | Book, reschedule & cancel slots |
| **Doctor** | `dr.smith` | `doctor123` | Cardiology specialist |
| **Doctor** | `dr.sarah` | `doctor123` | Neurology specialist |
| **Doctor** | `dr.raj` | `doctor123` | Pediatrics specialist |
| **Doctor** | `dr.emily` | `doctor123` | Dermatology specialist |

---

## 📖 API Documentation (Swagger)

When running the application, navigate to:
```text
http://localhost:8080/swagger-ui.html
```
- Click **Authorize** and input `Bearer <your_jwt_token>` to test secured endpoints.

---

## 🧪 Running Automated Tests

Run all unit and integration tests using:
```bash
./mvnw test
```
The test suite utilizes an in-memory **H2 Database** profile (`application-test.properties`), requiring zero external database setup.

---

## ☁️ Cloud Deployment (Render)

Refer to [DEPLOYMENT.md](./DEPLOYMENT.md) for step-by-step instructions to deploy to Render's free tier using `render.yaml`.
