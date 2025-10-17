-- Quick setup script - Run this first to create empty KickCard database
-- Then let Spring Boot auto-generate the schema with proper JPA annotations
-- NOTE: No Listing tables needed - Cards handle everything directly

-- Create database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'KickCard')
BEGIN
    CREATE DATABASE KickCard;
    PRINT 'KickCard database created successfully!';
END
ELSE
BEGIN
    PRINT 'KickCard database already exists.';
END

GO

USE KickCard;
GO

-- Insert basic seed data (users with encrypted passwords)
-- Password for all users: 123456 (BCrypt encrypted)

-- Check if admin user exists
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = 'admin')
BEGIN
    INSERT INTO dbo.Users (Username, Email, PasswordHash, Fullname, Phone, Address, Role, WalletBalance) VALUES
    ('admin', 'admin@kickcard.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System Administrator', '0123456789', '123 Admin Street', 'ADMIN', 10000.00);
    PRINT 'Admin user created.';
END

-- Check if regular users exist
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = 'user1')
BEGIN
    INSERT INTO dbo.Users (Username, Email, PasswordHash, Fullname, Phone, Address, Role, WalletBalance) VALUES
    ('user1', 'user1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John Doe', '0987654321', '456 User Lane', 'USER', 500.00),
    ('user2', 'user2@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Jane Smith', '0912345678', '789 Player Road', 'USER', 750.00),
    ('trader1', 'trader1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Mike Johnson', '0934567890', '321 Trader Blvd', 'USER', 1200.00),
    ('collector1', 'collector1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Sarah Wilson', '0945678901', '654 Collector Ave', 'USER', 2000.00);
    PRINT 'Sample users created.';
END

GO

PRINT '=== KickCard Database Setup Complete ===';
PRINT 'Database: KickCard';
PRINT 'Tables: Only Users, Cards, Transactions (No Listing tables)';
PRINT 'Admin User: admin / 123456';
PRINT 'Sample Users: user1, user2, trader1, collector1 (all password: 123456)';
PRINT 'Now start Spring Boot application to auto-generate tables and relationships.';