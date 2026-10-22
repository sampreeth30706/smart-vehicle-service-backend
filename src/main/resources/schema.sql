-- ============================================================================
-- Smart Vehicle Service & Management System - MySQL Relational Database Schema
-- ============================================================================

CREATE DATABASE IF NOT EXISTS smart_vehicle_db;
USE smart_vehicle_db;

-- 1. System Users (Customer / Owner, Mechanic, Administrator)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'MECHANIC', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    specialization VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Vehicles (Profiles, mileage, history, Insurance & PUC expiry dates)
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    registration_number VARCHAR(30) NOT NULL UNIQUE,
    make VARCHAR(60) NOT NULL,
    model VARCHAR(60) NOT NULL,
    manufacturing_year INT NOT NULL,
    fuel_type ENUM('PETROL', 'DIESEL', 'ELECTRIC', 'HYBRID', 'CNG') NOT NULL,
    vin VARCHAR(50),
    current_mileage INT NOT NULL DEFAULT 0,
    last_service_mileage INT NOT NULL DEFAULT 0,
    last_service_date DATE,
    insurance_policy_no VARCHAR(60),
    insurance_expiry DATE NOT NULL,
    puc_certificate_no VARCHAR(60),
    puc_expiry DATE NOT NULL,
    CONSTRAINT fk_vehicle_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Spare Parts Inventory
CREATE TABLE IF NOT EXISTS inventory_parts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(40) NOT NULL UNIQUE,
    part_name VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 5,
    unit_price DECIMAL(10, 2) NOT NULL,
    supplier VARCHAR(100)
);

-- 4. End-to-End Service Requests (9-Stage Lifecycle)
CREATE TABLE IF NOT EXISTS service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    vehicle_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    mechanic_id BIGINT,
    service_type VARCHAR(100) NOT NULL,
    customer_symptoms TEXT,
    ai_diagnosis_summary TEXT,
    preferred_date DATE NOT NULL,
    time_slot VARCHAR(40) NOT NULL,
    lifecycle_stage ENUM(
        'BOOKING',
        'VEHICLE_RECEIVED',
        'INSPECTION',
        'QUOTATION',
        'APPROVAL',
        'SERVICE_REPAIR',
        'QUALITY_CHECK',
        'PAYMENT',
        'DELIVERY'
    ) NOT NULL DEFAULT 'BOOKING',
    quotation_approved BOOLEAN DEFAULT FALSE,
    inspection_notes TEXT,
    service_report TEXT,
    estimated_cost DECIMAL(10, 2) DEFAULT 0.00,
    labour_hours DECIMAL(5, 2) DEFAULT 0.00,
    labour_rate DECIMAL(10, 2) DEFAULT 450.00,
    customer_rating INT,
    customer_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sr_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_sr_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_sr_mechanic FOREIGN KEY (mechanic_id) REFERENCES users(id)
);

-- 5. Parts Used in Service Requests
CREATE TABLE IF NOT EXISTS service_used_parts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    part_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_sup_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_sup_part FOREIGN KEY (part_id) REFERENCES inventory_parts(id)
);

-- 6. Digital Invoices & Billing
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    service_request_id BIGINT NOT NULL UNIQUE,
    parts_subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    labour_subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    CONSTRAINT fk_invoice_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id)
);

-- 7. Notification Scheduler Logs (Insurance, PUC, Low Stock, Service Updates)
CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT,
    channel ENUM('EMAIL', 'SMS', 'PUSH', 'SYSTEM') NOT NULL DEFAULT 'SYSTEM',
    alert_type ENUM('INSURANCE_EXPIRY', 'PUC_EXPIRY', 'LOW_STOCK', 'SERVICE_STATUS', 'MAINTENANCE_DUE') NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
