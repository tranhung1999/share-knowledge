-- ============================================================
-- V1: Initial Schema — Expense & Reimbursement Management System
-- ============================================================

-- Roles
CREATE TABLE roles (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roles_name ON roles (name);

-- Departments
CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Users
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    department_id BIGINT REFERENCES departments (id) ON DELETE SET NULL,
    role_id       BIGINT NOT NULL REFERENCES roles (id),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email        ON users (email);
CREATE INDEX idx_users_department   ON users (department_id);
CREATE INDEX idx_users_role         ON users (role_id);

-- Expense Types (ENUM)
CREATE TYPE expense_type AS ENUM (
    'TRAVEL',
    'ACCOMMODATION',
    'MEALS',
    'OFFICE_SUPPLIES',
    'TRAINING',
    'ENTERTAINMENT',
    'MEDICAL',
    'OTHER'
);

-- Expense Status (ENUM)
CREATE TYPE expense_status AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'APPROVED',
    'REJECTED',
    'PAID'
);

-- Expenses
CREATE TABLE expenses (
    id           BIGSERIAL PRIMARY KEY,
    employee_id  BIGINT          NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    expense_type expense_type    NOT NULL,
    amount       DECIMAL(15, 2)  NOT NULL CHECK (amount > 0),
    expense_date DATE            NOT NULL,
    description  TEXT            NOT NULL,
    status       expense_status  NOT NULL DEFAULT 'DRAFT',
    created_at   TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_employee   ON expenses (employee_id);
CREATE INDEX idx_expenses_status     ON expenses (status);
CREATE INDEX idx_expenses_date       ON expenses (expense_date);
CREATE INDEX idx_expenses_type       ON expenses (expense_type);
CREATE INDEX idx_expenses_created_at ON expenses (created_at);

-- Expense Attachments
CREATE TABLE expense_attachments (
    id          BIGSERIAL PRIMARY KEY,
    expense_id  BIGINT       NOT NULL REFERENCES expenses (id) ON DELETE CASCADE,
    file_name   VARCHAR(255) NOT NULL,
    file_path   VARCHAR(512) NOT NULL,
    file_type   VARCHAR(50)  NOT NULL,
    file_size   BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_expense ON expense_attachments (expense_id);

-- Approval Actions (ENUM)
CREATE TYPE approval_action AS ENUM (
    'APPROVE',
    'REJECT',
    'PAY'
);

-- Approvals
CREATE TABLE approvals (
    id          BIGSERIAL PRIMARY KEY,
    expense_id  BIGINT          NOT NULL REFERENCES expenses (id) ON DELETE CASCADE,
    approver_id BIGINT          NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    action      approval_action NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_approvals_expense  ON approvals (expense_id);
CREATE INDEX idx_approvals_approver ON approvals (approver_id);
CREATE INDEX idx_approvals_action   ON approvals (action);

-- Notification Types (ENUM)
CREATE TYPE notification_type AS ENUM (
    'EXPENSE_SUBMITTED',
    'EXPENSE_APPROVED',
    'EXPENSE_REJECTED',
    'EXPENSE_PAID',
    'GENERAL'
);

-- Notifications
CREATE TABLE notifications (
    id         BIGSERIAL          PRIMARY KEY,
    user_id    BIGINT             NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expense_id BIGINT             REFERENCES expenses (id) ON DELETE CASCADE,
    title      VARCHAR(255)       NOT NULL,
    message    TEXT               NOT NULL,
    type       notification_type  NOT NULL DEFAULT 'GENERAL',
    is_read    BOOLEAN            NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP          NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user    ON notifications (user_id);
CREATE INDEX idx_notifications_is_read ON notifications (user_id, is_read);
CREATE INDEX idx_notifications_expense ON notifications (expense_id);

-- Audit Logs
CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   BIGINT,
    old_values  JSONB,
    new_values  JSONB,
    ip_address  VARCHAR(50),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user        ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity      ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at  ON audit_logs (created_at);

-- Trigger: auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_expenses_updated_at
    BEFORE UPDATE ON expenses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_departments_updated_at
    BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
