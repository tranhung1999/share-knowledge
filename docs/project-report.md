# Project Report — Expense & Reimbursement Management System

---

## Section 1 — Project Overview

### Problem Statement
Employees currently submit expense claims via email, causing delays, lost receipts, and lack of transparency. The manual process leads to inconsistent approvals and no visibility into spending trends.

### Solution
A full-stack web application that digitizes the entire expense lifecycle — from submission to reimbursement — with role-based access, automated notifications, and real-time analytics.

### Scope
- Digitize expense submission and approval
- Implement multi-role workflow (Employee → Manager → Accountant)
- Provide file attachment support for receipts
- Build analytics dashboard and exportable reports
- Deploy via Docker with automated CI/CD

---

## Section 2 — Functional Requirements Coverage

| Feature                        | Status | Notes                                      |
|-------------------------------|--------|--------------------------------------------|
| Expense CRUD (type, amount, date, description) | ✅ | Full CRUD with validation |
| Save as Draft / Submit        | ✅     | Two-stage creation flow                    |
| File attachment (JPG/PNG/PDF, 10MB max) | ✅ | Stored locally; path traversal protected |
| Manager Approve / Reject      | ✅     | With optional comment                      |
| Accountant Mark as Paid       | ✅     | Separate workflow step                     |
| Status flow: DRAFT→SUBMITTED→APPROVED→PAID/REJECTED | ✅ | Enforced in service layer |
| Employee expense history      | ✅     | Paginated, filterable                      |
| Reports by department         | ✅     | With date range filter                     |
| Reports by month              | ✅     | Year-based grouping                        |
| Reports by type               | ✅     | Pie + bar charts                           |
| Notification system (advanced)| ✅     | In-app, async, per-event                   |
| Analytics dashboard           | ✅     | Summary cards + charts                     |
| Excel export                  | ✅     | Apache POI, multi-sheet                    |

---

## Section 3 — Non-Functional Requirements Coverage

| Requirement          | Implementation                                      |
|---------------------|-----------------------------------------------------|
| Roles (4)            | EMPLOYEE, MANAGER, ACCOUNTANT, ADMIN via Spring Security RBAC |
| File validation      | MIME type + extension check + size limit            |
| No crashes on invalid input | GlobalExceptionHandler + Bean Validation    |
| Friendly error messages | Structured ApiResponse, no stack traces        |
| Response < 3 seconds | HikariCP pool, lazy loading, indexed queries       |
| UI on 1080p          | Tailwind responsive grid, tested at 1920×1080      |

---

## Section 4 — Architecture

See `docs/architecture.md` for the full diagram.

**Design Patterns Used:**
- **Clean Architecture** — Controller → Service → Repository separation
- **DTO pattern** — no entity exposure in API responses
- **Builder pattern** — for entity and DTO construction (Lombok)
- **Strategy pattern** — file storage (local vs MinIO)
- **Observer pattern** — async notifications via @Async + Spring Events

**Security:**
- Stateless JWT authentication (no session)
- BCrypt password hashing (strength 10)
- Path traversal prevention in file storage
- Role-based method security (`@PreAuthorize`)
- CORS configured for frontend origins only

---

## Section 5 — Database Design

See `V1__init_schema.sql` for complete DDL.

**Tables:** users, roles, departments, expenses, expense_attachments, approvals, notifications, audit_logs

**Key Design Decisions:**
- PostgreSQL ENUM types for status fields (type-safe, indexed)
- Cascade DELETE on attachments when expense deleted
- `updated_at` auto-managed by trigger (not application logic)
- JSONB for audit log old/new values (flexible schema)
- Indexes on all foreign keys and frequently-filtered columns

---

## Section 6 — Testing

### Unit Tests (3 test classes, 19 test cases)

| Class               | Tests | Coverage Focus                            |
|--------------------|-------|-------------------------------------------|
| ExpenseServiceTest  | 8     | CRUD, status transitions, authorization   |
| ApprovalServiceTest | 6     | Approval workflow, role checks            |
| ReportServiceTest   | 5     | Data aggregation, DTO mapping             |

### Integration Test
`ExpenseControllerIntegrationTest` — 6 end-to-end tests covering:
- Health endpoint (unauthenticated)
- Auth login (valid/invalid credentials, validation)
- Expense endpoints (unauthenticated access → 401)

### Coverage
Target: ≥ 60% instruction coverage (enforced by JaCoCo Maven plugin)

### Testing Principles
- Each test focuses on a single behavior
- Mockito for isolating service dependencies
- AssertJ for expressive assertions
- Spring Boot Test for integration context
- H2 in-memory database for integration tests (MODE=PostgreSQL)
