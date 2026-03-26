# System Architecture — Expense & Reimbursement Management System

## High-Level Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                │
│                                                                      │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │              React SPA (TypeScript + Vite)                   │   │
│   │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │   │
│   │  │ Expense  │ │Approval  │ │ Reports  │ │ Notifications │  │   │
│   │  │  Module  │ │  Module  │ │  Module  │ │    Module     │  │   │
│   │  └──────────┘ └──────────┘ └──────────┘ └───────────────┘  │   │
│   │         ↕ Axios HTTP Client + JWT Auth Header                │   │
│   └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │ HTTPS REST API
┌────────────────────────────────▼─────────────────────────────────────┐
│                         API GATEWAY LAYER                            │
│              Spring Security (JWT Filter + RBAC)                     │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │
┌────────────────────────────────▼─────────────────────────────────────┐
│                        APPLICATION LAYER                             │
│                    Spring Boot 3.x (Java 17)                         │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                     CONTROLLERS                              │    │
│  │  AuthController │ ExpenseController │ ApprovalController     │    │
│  │  ReportController │ FileController │ UserController          │    │
│  │  HealthController │ NotificationController                   │    │
│  └──────────────────────────┬──────────────────────────────────┘    │
│                             │                                        │
│  ┌──────────────────────────▼──────────────────────────────────┐    │
│  │                      SERVICES                                │    │
│  │  ExpenseService │ ApprovalService │ ReportService            │    │
│  │  FileStorageService │ NotificationService │ UserService      │    │
│  │  AuthService │ AuditLogService                               │    │
│  └──────────────────────────┬──────────────────────────────────┘    │
│                             │                                        │
│  ┌──────────────────────────▼──────────────────────────────────┐    │
│  │                    REPOSITORIES                              │    │
│  │  UserRepo │ ExpenseRepo │ ApprovalRepo │ AuditLogRepo        │    │
│  │  DepartmentRepo │ RoleRepo │ NotificationRepo                │    │
│  └──────────────────────────┬──────────────────────────────────┘    │
└────────────────────────────┬─┴─────────────────────────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
┌─────────▼──────┐  ┌────────▼───────┐  ┌──────▼───────────┐
│  PostgreSQL 15  │  │  File Storage  │  │   Audit Logs     │
│                │  │  (Local / S3)  │  │  (DB + Logback)  │
│  • users        │  │                │  │                  │
│  • roles        │  │  • receipts    │  │  • JSON logs     │
│  • departments  │  │  • PDFs        │  │  • audit_logs    │
│  • expenses     │  │  • images      │  │    table         │
│  • approvals    │  │                │  │                  │
│  • attachments  │  └────────────────┘  └──────────────────┘
│  • audit_logs   │
└─────────────────┘

```

## Component Descriptions

### Frontend (React SPA)
- **Tech**: React 18, TypeScript, Vite, Redux Toolkit, Axios, Tailwind CSS
- **Responsibilities**: UI rendering, client-side routing, JWT storage, file uploads
- **Key modules**: Expense CRUD, Approval workflow, Reports/Charts, Notifications

### Backend (Spring Boot)
- **Tech**: Java 17, Spring Boot 3.2, Spring Security, JPA/Hibernate
- **Architecture pattern**: Clean Architecture (Controller → Service → Repository)
- **Auth**: JWT stateless tokens (15min access, 7d refresh)
- **Validation**: Bean Validation (@Valid) + global exception handler

### Database (PostgreSQL 15)
- **Connection pooling**: HikariCP (max 20 connections)
- **Migrations**: Flyway versioned scripts
- **Indexes**: On foreign keys, status fields, date ranges

### File Storage
- **Default**: Local filesystem (`/uploads` directory)
- **Production option**: MinIO (S3-compatible)
- **Limits**: 10MB max, JPG/PNG/PDF only

## Entity Relationship Diagram (ERD)

```
┌─────────────┐     ┌─────────────────┐     ┌──────────────┐
│  departments │     │      users       │     │    roles     │
├─────────────┤     ├─────────────────┤     ├──────────────┤
│ id (PK)     │◄────│ id (PK)         │────►│ id (PK)      │
│ name        │     │ email           │     │ name         │
│ description │     │ password_hash   │     │ description  │
│ created_at  │     │ full_name       │     │ created_at   │
└─────────────┘     │ department_id FK│     └──────────────┘
                    │ role_id FK      │
                    │ is_active       │
                    │ created_at      │
                    └────────┬────────┘
                             │
              ┌──────────────▼──────────────────┐
              │            expenses              │
              ├─────────────────────────────────┤
              │ id (PK)                         │
              │ employee_id FK → users.id       │
              │ expense_type (ENUM)             │
              │ amount (DECIMAL 15,2)           │
              │ expense_date                    │
              │ description                     │
              │ status (DRAFT/SUBMITTED/        │
              │         APPROVED/PAID/REJECTED) │
              │ created_at                      │
              │ updated_at                      │
              └───────┬──────────────┬──────────┘
                      │              │
       ┌──────────────▼──┐    ┌─────▼─────────────────┐
       │ expense_attachments│  │       approvals        │
       ├─────────────────┤   ├────────────────────────┤
       │ id (PK)         │   │ id (PK)                │
       │ expense_id FK   │   │ expense_id FK          │
       │ file_name       │   │ approver_id FK         │
       │ file_path       │   │ action (APPROVE/REJECT/│
       │ file_type       │   │         PAY)           │
       │ file_size       │   │ comment                │
       │ created_at      │   │ created_at             │
       └─────────────────┘   └────────────────────────┘
                                        │
              ┌─────────────────────────▼──────┐
              │           audit_logs            │
              ├────────────────────────────────┤
              │ id (PK)                        │
              │ user_id FK                     │
              │ action                         │
              │ entity_type                    │
              │ entity_id                      │
              │ old_values (JSONB)             │
              │ new_values (JSONB)             │
              │ ip_address                     │
              │ created_at                     │
              └────────────────────────────────┘

              ┌────────────────────────────────┐
              │         notifications           │
              ├────────────────────────────────┤
              │ id (PK)                        │
              │ user_id FK                     │
              │ title                          │
              │ message                        │
              │ type (EXPENSE_SUBMITTED, etc.) │
              │ expense_id FK (nullable)       │
              │ is_read                        │
              │ created_at                     │
              └────────────────────────────────┘
```

## Status Flow Diagram

```
            ┌──────────┐
            │  DRAFT   │◄─── Employee creates expense
            └────┬─────┘
                 │ Employee submits
                 ▼
         ┌──────────────┐
         │  SUBMITTED   │◄─── Notification sent to Manager
         └──────┬───────┘
                │
       ┌────────┴────────┐
       │ Manager reviews  │
       ▼                  ▼
┌──────────────┐   ┌──────────────┐
│   APPROVED   │   │   REJECTED   │◄─── Notification sent to Employee
└──────┬───────┘   └──────────────┘
       │ Accountant marks paid
       ▼
┌──────────────┐
│     PAID     │◄─── Notification sent to Employee
└──────────────┘
```

## Potential Bottlenecks & Scaling Strategy

### Bottlenecks
1. **File uploads**: Large files blocking threads → use async upload with streaming
2. **Report generation**: Heavy DB aggregation queries → add materialized views / caching
3. **JWT validation**: Crypto on every request → use stateless validation (no DB hit)
4. **Connection pool exhaustion**: Many concurrent users → tune HikariCP, add read replicas

### Scaling Strategy
1. **Horizontal scaling**: Stateless JWT → multiple app instances behind load balancer
2. **Database**: Read replicas for reports, connection pooling via PgBouncer
3. **File storage**: Migrate local → MinIO cluster → AWS S3 with CDN
4. **Caching**: Redis for report cache, notification counts
5. **Async processing**: Spring @Async for notifications, email sending
6. **Containerization**: Kubernetes Deployment with HPA for auto-scaling
