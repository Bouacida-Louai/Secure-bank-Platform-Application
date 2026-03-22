# 🏦 SecureBank API

A production-grade Banking & Loan Management REST API built with
Spring Boot 3, featuring enterprise-level security and compliance.

## 🚀 Tech Stack
- **Spring Boot 3** — Core framework
- **Spring Security 6** — Security layer
- **Keycloak 24** — Authentication & Authorization
- **OAuth2 + JWT** — Token-based auth
- **Custom RBAC Engine** — Amount-aware permissions
- **MySQL 8** — Database
- **Hibernate/JPA** — ORM with optimistic locking
- **Docker** — Containerization

## 🔐 Roles & Permissions

| Role | Permissions |
|------|------------|
| CUSTOMER | Apply for loans, transfer money |
| TELLER | Create accounts, deposit, withdraw |
| LOAN_OFFICER | Review & approve loans (under $50K) |
| RISK_ANALYST | Freeze accounts, view flagged transactions |
| AUDITOR | View all audit logs and reports |
| SUPER_ADMIN | Full system access |

## 🏗️ Architecture
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────▶│  Spring Boot │────▶│   MySQL     │
│  (Postman)  │     │     API     │     │  Database   │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
                    ┌──────▼──────┐
                    │  Keycloak   │
                    │   (Auth)    │
                    └─────────────┘
```

## ⚡ Quick Start

### Prerequisites
- Docker Desktop
- Java 21
- Maven

### Run with Docker
```bash
# Clone the repo
git clone https://github.com/louai/securebank-api.git

# Build the jar
./mvnw clean package -DskipTests

# Start everything
docker-compose up -d
```

### Access
- **API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Keycloak**: http://localhost:8181

### Get Token
```bash
POST http://localhost:8181/realms/securebank/protocol/openid-connect/token
  grant_type    = password
  client_id     = bank-api
  client_secret = bank-api-secret
  username      = teller_john
  password      = password123
```

## 📋 API Endpoints

### Accounts
| Method | Endpoint | Role |
|--------|----------|------|
| POST | /api/accounts | TELLER |
| GET | /api/accounts/my | CUSTOMER |
| GET | /api/accounts/{id} | Owner/TELLER |
| PATCH | /api/accounts/{id}/freeze | RISK_ANALYST |

### Transactions
| Method | Endpoint | Role |
|--------|----------|------|
| POST | /api/transactions/deposit | TELLER |
| POST | /api/transactions/withdraw | TELLER |
| POST | /api/transactions/transfer | CUSTOMER/TELLER |
| GET | /api/transactions/account/{id} | Owner/AUDITOR |

### Loans
| Method | Endpoint | Role |
|--------|----------|------|
| POST | /api/loans/apply | CUSTOMER |
| PATCH | /api/loans/{id}/review | LOAN_OFFICER |
| PATCH | /api/loans/{id}/approve | LOAN_OFFICER |
| PATCH | /api/loans/{id}/reject | LOAN_OFFICER |
| GET | /api/loans/{id}/schedule | Any |

### Audit & Risk
| Method | Endpoint | Role |
|--------|----------|------|
| GET | /api/audit/logs | AUDITOR |
| GET | /api/risk/flagged | RISK_ANALYST |

## 🔑 Key Features
- ✅ Amount-aware RBAC (loan approval limits)
- ✅ Optimistic + Pessimistic locking
- ✅ Immutable audit trail
- ✅ Automatic fraud detection
- ✅ Multi-level loan approval
- ✅ Keycloak realm auto-import