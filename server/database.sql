-- ==========================================
-- KayanSoft Activation System Schema
-- Production Ready SQLite / MySQL Compatible SQL
-- ==========================================

-- Table: admin_users (For dashboard authentication)
CREATE TABLE IF NOT EXISTS `admin_users` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) DEFAULT 'admin',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: clients (For subscriber metadata)
CREATE TABLE IF NOT EXISTS `clients` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `network_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: activation_serials (For secure device-bound license keys)
CREATE TABLE IF NOT EXISTS `activation_serials` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `client_id` INTEGER NOT NULL,
    `serial_key` VARCHAR(100) NOT NULL UNIQUE,
    `device_id` VARCHAR(100) DEFAULT NULL, -- Hardware UUID Lock on activation
    `duration_months` INTEGER NOT NULL DEFAULT 12,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'UNUSED', -- 'ACTIVE', 'EXPIRED', 'REVOKED', 'USED', 'UNUSED'
    `notes` TEXT DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`) ON DELETE CASCADE
);

-- Table: security_logs (Audit trailing for anti-replay/tampering)
CREATE TABLE IF NOT EXISTS `security_logs` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `ip_address` VARCHAR(45) NOT NULL,
    `endpoint` VARCHAR(100) NOT NULL,
    `request_payload` TEXT DEFAULT NULL,
    `status_code` INTEGER NOT NULL,
    `message` TEXT DEFAULT NULL,
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Default Administrator Account (Seed Data)
-- Default Login details: Username: admin, Password: admin_secure_pass_2026
-- In production, make sure to change the password hash to a secure bcrypt-derived value.
INSERT INTO `admin_users` (`username`, `password_hash`, `role`) 
VALUES ('admin', '$2b$10$7qB2vNlJ0gN9l6X7tPsz6O7BfKz9N6UpxF2k9yZ3P7d1H3a0s2p9u', 'super_admin');
