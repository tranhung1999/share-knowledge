-- ============================================================
-- V3: Fix password hashes for all seed users
-- Admin@123 for admin, 'password' for all others
-- ============================================================

UPDATE users SET password_hash = '$2b$10$ux8pWjeUkH1xI1.4d8vaIOZFWIIUvhwP6LZDeKNnUpfHTJ1tFWU6.'
WHERE email = 'admin@company.com';

UPDATE users SET password_hash = '$2b$10$swehxZT/DsZ6iLhjKho5NOWh5k3kWSjLT1edAnDn/T4VJPVPk0Dyu'
WHERE email = 'manager@company.com';

UPDATE users SET password_hash = '$2b$10$f44IcT26./512VKuM27XcOofqx6bPgJVU3sJGC8s7IXroNxdSZXM.'
WHERE email = 'accountant@company.com';

UPDATE users SET password_hash = '$2b$10$uT1hzxjRCCtfKHXx4Srnre7FletYVEGSNVjtMXMbpWakoNh5Kxgie'
WHERE email = 'alice@company.com';

UPDATE users SET password_hash = '$2b$10$RYCgrczLs08GuYCfYr9AxOe452Sg/1gqAym.CkTjcr66C5Y1XcG2.'
WHERE email = 'bob@company.com';
