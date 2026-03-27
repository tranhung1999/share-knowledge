-- ============================================================
-- V4: Convert PostgreSQL native enum types to varchar
-- Required for Hibernate @Enumerated(EnumType.STRING) compatibility
-- ============================================================

ALTER TABLE expenses
    ALTER COLUMN status       TYPE varchar(20) USING status::varchar,
    ALTER COLUMN expense_type TYPE varchar(30) USING expense_type::varchar;

ALTER TABLE approvals
    ALTER COLUMN action TYPE varchar(20) USING action::varchar;

ALTER TABLE notifications
    ALTER COLUMN type TYPE varchar(50) USING type::varchar;

