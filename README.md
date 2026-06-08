# Mini Bank System – Java Spring Boot

![Java](https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange?style=for-the-badge&logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)
![CI/CD](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-brightgreen?style=for-the-badge&logo=githubactions)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

## Overview

**Mini Bank System** is a backend banking application built with **Java 21** and **Spring Boot 3.5**, designed to simulate real-world core banking operations.

The system handles user authentication, account management, and financial transactions with a strong focus on **security**, **data integrity**, and **concurrency safety**. It uses **JWT-based authentication**, **Role-Based Access Control (RBAC)**, and **Pessimistic Write Locking** to prevent race conditions during concurrent balance updates.

---

## Features

- **JWT Authentication** with stateless session management
- **Role-Based Authorization** — USER & ADMIN roles
- User Registration & Secure Login
- Multi-Account Support per User
- Deposit, Withdrawal & Transfer Operations
- **Configurable Transaction Limits** — per-transaction amount & daily transfer count via `application.yml`
- **Pessimistic Write Locking** to prevent race conditions on concurrent transactions
- **Deadlock Prevention** via consistent ordered account locking in transfers
- Paginated Transaction History per Account (owner-only access)
- Admin Dashboard — All Users, All Accounts, All Transactions
- Global Exception Handling with structured JSON error responses
- Input Validation on all endpoints
- API Documentation with **Swagger UI**
- **Java 21 Virtual Threads** for high concurrency
- **Dockerized** with Docker Compose and MySQL healthcheck
- **CI/CD Pipeline** with GitHub Actions — Build, Test & Coverage Report

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito + MockMvc + H2 |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/minibank/
│   │   ├── config/          # Security configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Request & Response DTOs
│   │   ├── exception/       # Custom exceptions & GlobalExceptionHandler
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── security/        # JWT filter, entry point, UserDetailsService
│   │   └── service/         # Business logic (interfaces + implementations)
│   └── resources/
│       ├── application.yml              # Main configuration
│       ├── application-local.yml        # Local overrides (gitignored)
│       ├── application-local-sample.yml # Template for local setup
│       └── application-test.yml         # H2 config for tests
└── test/
    ├── controller/   # Integration tests
    ├── repository/   # Repository tests
    └── service/impl/ # Unit tests
```

### Request Flow

```
Client Request
      ↓
  JWT Filter  ──► 401 Unauthorized
      ↓
  Controller
      ↓
   Service   ──► 400 / 403 / 404 / 429
      ↓
 Repository
      ↓
MySQL Database
```

---

## API Endpoints

### Authentication — Public

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |

### Accounts — Requires Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/accounts` | Create a new account (min opening balance: 100) |
| GET | `/api/accounts/{accountNumber}` | Get account details |
| GET | `/api/accounts/my-accounts` | Get all accounts for the logged-in user |

### Transactions — Requires Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions/deposit` | Deposit money into account |
| POST | `/api/transactions/withdraw` | Withdraw money (max: 10,000 per transaction) |
| POST | `/api/transactions/transfer` | Transfer between accounts (max: 50,000 / max 5 per day) |
| GET | `/api/transactions/history/{accountNumber}?page=0&size=10` | Paginated transaction history (owner only) |

### Admin — Requires ADMIN Role

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users?page=0&size=10` | Get all users (paginated) |
| GET | `/api/admin/accounts?page=0&size=10` | Get all accounts (paginated) |
| GET | `/api/transactions/history/admin?page=0&size=10` | Get all transactions (paginated) |

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Request & Response Examples

### Register

```http
POST /api/auth/register
Content-Type: application/json
```
```json
{
  "username": "ayman",
  "email": "ayman@example.com",
  "password": "password123"
}
```
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "ayman",
  "email": "ayman@example.com"
}
```

### Deposit

```http
POST /api/transactions/deposit
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "accountNumber": "MB00123456789012",
  "amount": 500.00
}
```
```json
{
  "id": 1,
  "type": "DEPOSIT",
  "amount": 500.00,
  "note": "Deposit operation",
  "timestamp": "2026-06-08T14:30:00"
}
```

### Transfer

```http
POST /api/transactions/transfer
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "accountNumber": "MB00123456789012",
  "targetAccountNumber": "MB00987654321098",
  "amount": 200.00
}
```

### Error Response

```json
{
  "statusCode": 400,
  "error": "Bad Request",
  "message": "Insufficient funds in account MB00123456789012: available 100.00, requested 500.00",
  "path": "uri=/api/transactions/withdraw",
  "timestamp": "2026-06-08T14:35:00"
}
```

### Paginated Response

```json
{
  "content": [
    {
      "id": 1,
      "type": "DEPOSIT",
      "amount": 500.00,
      "note": "Deposit operation",
      "timestamp": "2026-06-05T17:48:20.3433156"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "unsorted": false,
      "sorted": true,
      "empty": false
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "numberOfElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "unsorted": false,
    "sorted": true,
    "empty": false
  },
  "empty": false
}
```

---

## Transaction Limits

Limits are fully configurable in `application.yml` without touching the code:

```yaml
app:
  transaction:
    limits:
      max-withdraw-amount: 10000
      max-transfer-amount: 50000
      max-daily-transfers: 5
```

| Operation | Limit |
|-----------|-------|
| Withdraw | Max 10,000 per transaction |
| Transfer amount | Max 50,000 per transaction |
| Transfer count | Max 5 transfers per day |

Exceeding the daily transfer limit returns **429 Too Many Requests**.

---

## Concurrency & Data Integrity

### Pessimistic Write Locking
All balance-modifying operations (`deposit`, `withdraw`, `transfer`) use `SELECT ... FOR UPDATE` to prevent dirty reads and lost updates under concurrent access.

### Deadlock Prevention
Transfers always acquire locks in a consistent order based on account ID, eliminating circular wait conditions.

```
Thread A: transfer Account #1 → Account #2   (locks #1 first, then #2)
Thread B: transfer Account #2 → Account #1   (locks #1 first, then #2)

Result: No deadlock — both threads follow the same locking order.
```

---

## How to Run Locally

### Prerequisites

- Java 21
- Maven
- MySQL 8

### 1. Clone the repository

```bash
git clone https://github.com/Ayman2004iu/MiniBank-Application.git
cd MiniBankApp
```

### 2. Create local configuration

Copy the sample file and fill in your credentials:

```bash
cp src/main/resources/application-local-sample.yml src/main/resources/application-local.yml
```

Then edit `application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/minibank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
 
jwt:
  secret: your_secret_key_at_least_32_characters_long
 
app:
  cors:
    allowed-origins: http://localhost:3000
```

> `application-local.yml` is in `.gitignore` and will never be committed.

### 3. Database Role Format

> **Note:** If you have existing users in the database with `ROLE_USER` or `ROLE_ADMIN` values, run the following SQL once to align the role format with the application:
>
> ```sql
> SET SQL_SAFE_UPDATES = 0;
> UPDATE users SET role = 'USER' WHERE role = 'ROLE_USER';
> UPDATE users SET role = 'ADMIN' WHERE role = 'ROLE_ADMIN';
> SET SQL_SAFE_UPDATES = 1;
> ```
>
> New users registered through the API do not require this step.

### 4. Build the project

```bash
.\mvnw clean package -DskipTests
```

### 5. Run the application

```bash
java -jar target/MiniBankApp-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Application available at `http://localhost:8080`
 
---

## Run with Docker

### Prerequisites

- Docker
- Docker Compose

### 1. Create `.env` file

Copy the example and fill in your values:

```bash
cp example.env .env
```

```env
MYSQL_ROOT_PASSWORD=yourpassword
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword
JWT_SECRET=your-secret-key-at-least-32-characters-long
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000
```

> `.env` is in `.gitignore` and will never be committed. Use `example.env` as a reference.

### 2. Build and start containers

```bash
docker compose up --build -d
```

> First startup may take a few minutes while MySQL initializes and passes its healthcheck.

### 3. Stop containers

```bash
docker compose down
```

Application available at `http://localhost:8080`

---

## Running Tests

Tests use an **H2 in-memory database** — no MySQL or Docker required.

```bash
.\mvnw test "-Dspring.profiles.active=test"
```

### Test Coverage

| Type | Test Class | What It Tests |
|------|-----------|---------------|
| Unit | `AuthServiceImplTest` | Register, login, duplicate user |
| Unit | `AccountServiceImplTest` | Create, get, list accounts |
| Unit | `TransactionServiceImplTest` | Deposit, withdraw, transfer, all limits |
| Integration | `AuthControllerIntegrationTest` | Auth endpoints end-to-end |
| Integration | `AccountControllerIntegrationTest` | Account endpoints end-to-end |
| Integration | `TransactionControllerIntegrationTest` | Transaction endpoints end-to-end |
| Integration | `AdminControllerIntegrationTest` | Admin endpoints, role enforcement |
| Repository | `AccountRepositoryTest` | Repository queries |

---

## CI/CD Pipeline

GitHub Actions runs automatically on every push and pull request to `main`:

| Step | Action |
|------|--------|
| 1 | Checkout repository |
| 2 | Setup JDK 21 (Temurin) |
| 3 | Cache Maven dependencies |
| 4 | Build & run all tests with `mvn clean verify` |
| 5 | Upload JaCoCo coverage report as artifact |
| 6 | Upload JAR as build artifact |

---

## Future Improvements

- Redis Caching for frequently read data
- Email Notifications on transactions
- Cloud Deployment (AWS / Railway)
- Frontend Application with Angular

---

## Author

**Ayman Ibrahim Seddik**

[![Email](https://img.shields.io/badge/Email-ayman.ibrahim.seddik%40gmail.com-red?style=flat-square&logo=gmail)](mailto:ayman.ibrahim.seddik@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-Ayman2004iu-black?style=flat-square&logo=github)](https://github.com/Ayman2004iu)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-ayman--ibrahim--dev-blue?style=flat-square&logo=linkedin)](https://www.linkedin.com/in/ayman-ibrahim-dev/)
