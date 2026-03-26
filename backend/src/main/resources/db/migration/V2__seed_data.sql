-- ============================================================
-- V2: Seed Data
-- ============================================================

-- Roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN',      'System administrator with full access'),
    ('MANAGER',    'Department manager who approves expenses'),
    ('ACCOUNTANT', 'Accountant who marks expenses as paid'),
    ('EMPLOYEE',   'Regular employee who submits expenses');

-- Departments
INSERT INTO departments (name, description) VALUES
    ('Engineering',   'Software and hardware engineering team'),
    ('Marketing',     'Marketing and brand management team'),
    ('Finance',       'Finance and accounting team'),
    ('Human Resources', 'HR and recruitment team'),
    ('Operations',    'Business operations team');

-- Default Admin User (password: Admin@123)
-- BCrypt hash of "Admin@123"
INSERT INTO users (email, password_hash, full_name, department_id, role_id, is_active)
VALUES (
    'admin@company.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Administrator',
    (SELECT id FROM departments WHERE name = 'Finance'),
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    TRUE
);

-- Manager User (password: Manager@123)
INSERT INTO users (email, password_hash, full_name, department_id, role_id, is_active)
VALUES (
    'manager@company.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'John Manager',
    (SELECT id FROM departments WHERE name = 'Engineering'),
    (SELECT id FROM roles WHERE name = 'MANAGER'),
    TRUE
);

-- Accountant User (password: Accountant@123)
INSERT INTO users (email, password_hash, full_name, department_id, role_id, is_active)
VALUES (
    'accountant@company.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'Jane Accountant',
    (SELECT id FROM departments WHERE name = 'Finance'),
    (SELECT id FROM roles WHERE name = 'ACCOUNTANT'),
    TRUE
);

-- Employee Users (password: Employee@123)
INSERT INTO users (email, password_hash, full_name, department_id, role_id, is_active)
VALUES (
    'alice@company.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'Alice Johnson',
    (SELECT id FROM departments WHERE name = 'Engineering'),
    (SELECT id FROM roles WHERE name = 'EMPLOYEE'),
    TRUE
),
(
    'bob@company.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'Bob Smith',
    (SELECT id FROM departments WHERE name = 'Marketing'),
    (SELECT id FROM roles WHERE name = 'EMPLOYEE'),
    TRUE
);
