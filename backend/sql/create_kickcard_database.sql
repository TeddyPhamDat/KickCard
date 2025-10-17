-- Create KickCard Database with Trading System
-- Run this script to create a fresh database with all required tables and seed data

-- Create database
CREATE DATABASE KickCard;
GO

USE KickCard;
GO

-- Create Users table with wallet balance
CREATE TABLE dbo.Users (
    Id bigint IDENTITY(1,1) PRIMARY KEY,
    Username nvarchar(50) NOT NULL UNIQUE,
    Email nvarchar(100) NOT NULL UNIQUE,
    PasswordHash nvarchar(255) NOT NULL,
    Fullname nvarchar(100),
    Phone nvarchar(20),
    Address nvarchar(255),
    AvatarUrl nvarchar(500),
    Role nvarchar(20) NOT NULL DEFAULT 'USER',
    WalletBalance decimal(10,2) NOT NULL DEFAULT 0.00,
    CreatedAt datetime2 DEFAULT GETDATE(),
    UpdatedAt datetime2 DEFAULT GETDATE()
);

-- Create Cards table with price and trading status
CREATE TABLE dbo.Cards (
    Id bigint IDENTITY(1,1) PRIMARY KEY,
    Name nvarchar(100) NOT NULL,
    Description nvarchar(500),
    BaseImageUrl nvarchar(500),
    Rarity nvarchar(20) NOT NULL,
    Team nvarchar(50),
    OwnerId bigint NOT NULL,
    Status nvarchar(20) NOT NULL DEFAULT 'PENDING',
    Price decimal(10,2),
    RejectionReason nvarchar(500),
    CreatedAt datetime2 DEFAULT GETDATE(),
    UpdatedAt datetime2 DEFAULT GETDATE(),
    FOREIGN KEY (OwnerId) REFERENCES dbo.Users(Id) ON DELETE CASCADE
);

-- Create Listings table (for marketplace display)
CREATE TABLE dbo.Listings (
    Id bigint IDENTITY(1,1) PRIMARY KEY,
    Title nvarchar(100) NOT NULL,
    Description nvarchar(500),
    Price decimal(10,2) NOT NULL,
    Currency nvarchar(10) NOT NULL DEFAULT 'USD',
    Quantity int NOT NULL DEFAULT 1,
    Status nvarchar(20) NOT NULL DEFAULT 'ACTIVE',
    SellerId bigint NOT NULL,
    CardId bigint NOT NULL,
    CreatedAt datetime2 DEFAULT GETDATE(),
    UpdatedAt datetime2 DEFAULT GETDATE(),
    FOREIGN KEY (SellerId) REFERENCES dbo.Users(Id) ON DELETE CASCADE,
    FOREIGN KEY (CardId) REFERENCES dbo.Cards(Id) ON DELETE CASCADE
);

-- Create Transactions table for trading history
CREATE TABLE dbo.Transactions (
    Id bigint IDENTITY(1,1) PRIMARY KEY,
    BuyerId bigint NOT NULL,
    SellerId bigint NOT NULL,
    CardId bigint NOT NULL,
    Amount decimal(10,2) NOT NULL,
    Status nvarchar(20) NOT NULL DEFAULT 'PENDING',
    TransactionDate datetime2 DEFAULT GETDATE(),
    CompletedDate datetime2,
    Notes nvarchar(500),
    FOREIGN KEY (BuyerId) REFERENCES dbo.Users(Id),
    FOREIGN KEY (SellerId) REFERENCES dbo.Users(Id),
    FOREIGN KEY (CardId) REFERENCES dbo.Cards(Id)
);

-- Create indexes for better performance
CREATE INDEX IX_Users_Username ON dbo.Users(Username);
CREATE INDEX IX_Users_Email ON dbo.Users(Email);
CREATE INDEX IX_Cards_Status ON dbo.Cards(Status);
CREATE INDEX IX_Cards_OwnerId ON dbo.Cards(OwnerId);
CREATE INDEX IX_Listings_Status ON dbo.Listings(Status);
CREATE INDEX IX_Listings_SellerId ON dbo.Listings(SellerId);
CREATE INDEX IX_Transactions_BuyerId ON dbo.Transactions(BuyerId);
CREATE INDEX IX_Transactions_SellerId ON dbo.Transactions(SellerId);
CREATE INDEX IX_Transactions_Status ON dbo.Transactions(Status);

-- Insert seed data
-- Admin user
INSERT INTO dbo.Users (Username, Email, PasswordHash, Fullname, Phone, Address, Role, WalletBalance) VALUES
('admin', 'admin@kickcard.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System Administrator', '0123456789', '123 Admin Street', 'ADMIN', 10000.00);

-- Regular users with wallet balance
INSERT INTO dbo.Users (Username, Email, PasswordHash, Fullname, Phone, Address, Role, WalletBalance) VALUES
('user1', 'user1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John Doe', '0987654321', '456 User Lane', 'USER', 500.00),
('user2', 'user2@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Jane Smith', '0912345678', '789 Player Road', 'USER', 750.00),
('trader1', 'trader1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Mike Johnson', '0934567890', '321 Trader Blvd', 'USER', 1200.00),
('collector1', 'collector1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Sarah Wilson', '0945678901', '654 Collector Ave', 'USER', 2000.00);

-- Sample cards with prices
INSERT INTO dbo.Cards (Name, Description, BaseImageUrl, Rarity, Team, OwnerId, Status, Price) VALUES
-- Approved cards for sale
('Cristiano Ronaldo', 'Legendary Portuguese forward', 'https://example.com/ronaldo.jpg', 'LEGENDARY', 'Al Nassr', 2, 'APPROVED', 150.00),
('Lionel Messi', 'GOAT Argentine playmaker', 'https://example.com/messi.jpg', 'LEGENDARY', 'Inter Miami', 3, 'APPROVED', 200.00),
('Kylian Mbappé', 'Lightning-fast French striker', 'https://example.com/mbappe.jpg', 'EPIC', 'Real Madrid', 4, 'APPROVED', 120.00),
('Erling Haaland', 'Goal machine from Norway', 'https://example.com/haaland.jpg', 'EPIC', 'Manchester City', 5, 'APPROVED', 100.00),
('Kevin De Bruyne', 'Belgian midfield maestro', 'https://example.com/debruyne.jpg', 'RARE', 'Manchester City', 2, 'APPROVED', 80.00),
('Virgil van Dijk', 'Rock-solid Dutch defender', 'https://example.com/vandijk.jpg', 'RARE', 'Liverpool', 3, 'APPROVED', 70.00),

-- Pending cards (awaiting admin approval)
('Pedri González', 'Young Spanish talent', 'https://example.com/pedri.jpg', 'RARE', 'Barcelona', 4, 'PENDING', NULL),
('Jamal Musiala', 'Promising German midfielder', 'https://example.com/musiala.jpg', 'UNCOMMON', 'Bayern Munich', 5, 'PENDING', NULL),

-- User collection cards (not for sale)
('Neymar Jr', 'Brazilian superstar', 'https://example.com/neymar.jpg', 'EPIC', 'Al Hilal', 2, 'APPROVED', NULL),
('Mohamed Salah', 'Egyptian speedster', 'https://example.com/salah.jpg', 'RARE', 'Liverpool', 3, 'APPROVED', NULL);

-- Sample listings for marketplace
INSERT INTO dbo.Listings (Title, Description, Price, Currency, Quantity, Status, SellerId, CardId) VALUES
('Rare Cristiano Ronaldo Card', 'Perfect condition legendary card', 150.00, 'USD', 1, 'ACTIVE', 2, 1),
('Messi GOAT Edition', 'Limited edition Messi card', 200.00, 'USD', 1, 'ACTIVE', 3, 2),
('Mbappé Speed Demon', 'Epic striker card with great stats', 120.00, 'USD', 1, 'ACTIVE', 4, 3),
('Haaland Goal Machine', 'Norwegian powerhouse card', 100.00, 'USD', 1, 'ACTIVE', 5, 4),
('De Bruyne Playmaker', 'Master of assists and creativity', 80.00, 'USD', 1, 'ACTIVE', 2, 5),
('Van Dijk Defender', 'Unbeatable defensive card', 70.00, 'USD', 1, 'ACTIVE', 3, 6);

-- Sample completed transactions
INSERT INTO dbo.Transactions (BuyerId, SellerId, CardId, Amount, Status, TransactionDate, CompletedDate, Notes) VALUES
(4, 2, 1, 150.00, 'COMPLETED', DATEADD(day, -7, GETDATE()), DATEADD(day, -7, GETDATE()), 'Quick transaction, great card!'),
(5, 3, 2, 200.00, 'COMPLETED', DATEADD(day, -5, GETDATE()), DATEADD(day, -5, GETDATE()), 'Worth every penny for this legendary card'),
(2, 4, 3, 120.00, 'COMPLETED', DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()), 'Fast delivery, excellent condition');

-- Add some pending transactions
INSERT INTO dbo.Transactions (BuyerId, SellerId, CardId, Amount, Status, TransactionDate, Notes) VALUES
(3, 5, 4, 100.00, 'PENDING', GETDATE(), 'Waiting for payment confirmation'),
(4, 2, 5, 80.00, 'PENDING', GETDATE(), 'Processing transaction');

GO

PRINT 'KickCard database created successfully with trading system!';
PRINT 'Users created: admin (password: 123456), user1, user2, trader1, collector1 (all with password: 123456)';
PRINT 'Sample cards, listings, and transactions have been added.';
PRINT 'Database is ready for Spring Boot application with trading features.';