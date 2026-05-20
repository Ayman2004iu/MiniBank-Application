# Mini Bank – Java Spring Boot Project

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![MySQL](https://img.shields.io/badge/Database-MySQL-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

## Overview
**Mini Bank** is a backend banking application built with **Java, Spring Boot, and MySQL**.  
It simulates core banking operations such as account creation, deposits, withdrawals, transfers, and viewing transaction history — all secured with **JWT-based authentication**.

---

## Features
- **JWT Authentication & Role-based Authorization**
- User registration & login
- Account creation and balance inquiry
- Deposit & Withdraw money
- Transfer money between accounts
- Transaction history (user & admin views)

---

## Tech Stack
- **Backend:** Java 17, Spring Boot
- **Database:** MySQL (Spring Data JPA / Hibernate)
- **Security:** Spring Security + JWT
- **Build Tool:** Maven


---

## API Endpoints

### Authentication
- `POST /api/auth/register` → Register new user
- `POST /api/auth/login` → Login & retrieve JWT token

### Accounts
- `POST /api/accounts` → Create account
- `GET /api/accounts/{accountNumber}` → Get account by number

### Transactions
- `POST /api/transactions/deposit` → Deposit money
- `POST /api/transactions/withdraw` → Withdraw money
- `POST /api/transactions/transfer` → Transfer money
- `GET /api/transactions/history/{accountId}` → Transaction history
- `GET /api/transactions/history/admin` → Transaction full history (**admin only**)

---
## Example Request/Response

### Deposit Example
**Request**

`POST /api/transactions/deposit`
```json
{
  "accountNumber": "MB8216501149615998897",
  "amount": 500.00
}
```
**Response**
```json
{
  "id": 1,
  "account_id": 2,
  "type": "DEPOSIT",
  "amount": 500.00,
  "note": "Salary Deposit",
  "timestamp": "2025-09-27T14:30:00"
}
```
---
## Future Improvements

- Add Unit & Integration Tests

- Build a frontend with React/Angular

- Add Admin Dashboard for user management

- Support for email/SMS notifications

## Author

- Ayman Ibrahim Siddek

- ayman.ibrahim.seddik@gmail.com

- [GitHub](https://github.com/Ayman2004iu)

- [LinkedIn]( https://www.linkedin.com/in/ayman-ibrahim-dev/)
