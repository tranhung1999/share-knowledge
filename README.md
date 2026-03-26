# Expense & Reimbursement Management System

A full-stack web application for digitizing and streamlining employee expense claims and reimbursements.

## Tech Stack

| Layer      | Technology                                      |
|------------|-------------------------------------------------|
| Frontend   | React 18, TypeScript, Vite, Redux Toolkit, Tailwind CSS, Recharts |
| Backend    | Java 17, Spring Boot 3.2, Spring Security (JWT), JPA/Hibernate |
| Database   | PostgreSQL 15, Flyway migrations                |
| DevOps     | Docker (multi-stage), docker-compose            |
| CI/CD      | GitHub Actions                                  |

## Features

- **Expense CRUD** — create, edit, submit, delete expenses
- **Approval Workflow** — DRAFT → SUBMITTED → APPROVED → PAID / REJECTED
- **Role-Based Access** — EMPLOYEE, MANAGER, ACCOUNTANT, ADMIN
- **File Attachments** — upload JPG/PNG/PDF (max 10MB)
- **Notification System** — in-app notifications on status changes
- **Analytics Dashboard** — charts for trends, types, departments
- **Excel Export** — download reports as `.xlsx`
- **Audit Logs** — immutable log of all actions
- **Health Check** — `/health` endpoint

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local dev)
- Node.js 20+ (for local dev)

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/tranhung1999/share-knowledge.git
cd share-knowledge

# Copy environment template
cp .env.example .env
# Edit .env with your values (at minimum set a strong JWT_SECRET)

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend
```

The application will be available at:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Docs (Swagger)**: http://localhost:8080/swagger-ui.html

### Local Development

**Backend:**
```bash
cd backend
# Start PostgreSQL (or use Docker)
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=expense_db \
  -e POSTGRES_USER=expense_user \
  -e POSTGRES_PASSWORD=expense_pass \
  postgres:15-alpine

mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
# App available at http://localhost:5173
```

## Environment Variables

| Variable              | Default                  | Description                        |
|-----------------------|--------------------------|------------------------------------|
| `DB_URL`              | `jdbc:postgresql://...`  | PostgreSQL JDBC URL                |
| `DB_USERNAME`         | `expense_user`           | Database username                  |
| `DB_PASSWORD`         | `expense_pass`           | Database password                  |
| `JWT_SECRET`          | *(required)*             | JWT signing secret (min 32 chars)  |
| `JWT_ACCESS_EXPIRY`   | `900000` (15 min)        | Access token expiry in ms          |
| `UPLOAD_DIR`          | `./uploads`              | File storage directory             |
| `SERVER_PORT`         | `8080`                   | Backend server port                |
| `SPRING_PROFILES_ACTIVE` | `dev`               | Spring profile                     |

## Demo Accounts

| Role       | Email                    | Password    |
|------------|--------------------------|-------------|
| Admin      | admin@company.com        | Admin@123   |
| Manager    | manager@company.com      | password    |
| Accountant | accountant@company.com   | password    |
| Employee   | alice@company.com        | password    |
| Employee   | bob@company.com          | password    |

## Running Tests

```bash
cd backend
mvn test                    # run unit + integration tests
mvn verify                  # run tests + coverage check (≥60%)
mvn jacoco:report           # generate HTML coverage report
```

Coverage report: `backend/target/site/jacoco/index.html`

## API Documentation

Interactive Swagger UI available at: `http://localhost:8080/swagger-ui.html`

See `docs/api-docs.md` for full endpoint reference.

## Project Structure

```
share-knowledge/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/expense/management/
│   │   ├── config/             # Security, OpenAPI config
│   │   ├── controller/         # REST controllers
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── entity/             # JPA entities
│   │   ├── exception/          # Custom exceptions + global handler
│   │   ├── repository/         # JPA repositories
│   │   ├── security/           # JWT filter, UserPrincipal
│   │   ├── service/            # Business logic
│   │   └── util/               # Constants, utilities
│   ├── src/main/resources/
│   │   ├── db/migration/       # Flyway SQL scripts
│   │   └── application.yml
│   └── Dockerfile
├── frontend/                   # React SPA
│   ├── src/
│   │   ├── components/         # Reusable UI components
│   │   ├── pages/              # Route-level page components
│   │   ├── services/           # API client functions
│   │   ├── store/              # Redux slices
│   │   ├── types/              # TypeScript interfaces
│   │   └── utils/              # Formatters, helpers
│   ├── nginx.conf
│   └── Dockerfile
├── .github/workflows/          # GitHub Actions CI/CD
├── docker-compose.yml
└── docs/                       # Architecture & API documentation
```
