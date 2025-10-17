-- Seed data for KickCard Trading System - Run after Spring Boot creates tables
-- This script adds sample cards and marketplace listings with trading features

USE KickCard;
GO

-- Sample cards with prices for trading
IF NOT EXISTS (SELECT 1 FROM dbo.Cards WHERE Name = 'Cristiano Ronaldo')
BEGIN
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
    
    PRINT 'Sample cards added successfully.';
END

-- Sample listings for marketplace
IF NOT EXISTS (SELECT 1 FROM dbo.Listings WHERE Title = 'Rare Cristiano Ronaldo Card')
BEGIN
    INSERT INTO dbo.Listings (Title, Description, Price, Currency, Quantity, Status, SellerId, CardId) VALUES
    ('Rare Cristiano Ronaldo Card', 'Perfect condition legendary card', 150.00, 'USD', 1, 'ACTIVE', 2, 1),
    ('Messi GOAT Edition', 'Limited edition Messi card', 200.00, 'USD', 1, 'ACTIVE', 3, 2),
    ('Mbappé Speed Demon', 'Epic striker card with great stats', 120.00, 'USD', 1, 'ACTIVE', 4, 3),
    ('Haaland Goal Machine', 'Norwegian powerhouse card', 100.00, 'USD', 1, 'ACTIVE', 5, 4),
    ('De Bruyne Playmaker', 'Master of assists and creativity', 80.00, 'USD', 1, 'ACTIVE', 2, 5),
    ('Van Dijk Defender', 'Unbeatable defensive card', 70.00, 'USD', 1, 'ACTIVE', 3, 6);
    
    PRINT 'Sample listings added successfully.';
END

-- Sample completed transactions
IF NOT EXISTS (SELECT 1 FROM dbo.Transactions)
BEGIN
    INSERT INTO dbo.Transactions (BuyerId, SellerId, CardId, Amount, Status, TransactionDate, CompletedDate, Notes) VALUES
    (4, 2, 1, 150.00, 'COMPLETED', DATEADD(day, -7, GETDATE()), DATEADD(day, -7, GETDATE()), 'Quick transaction, great card!'),
    (5, 3, 2, 200.00, 'COMPLETED', DATEADD(day, -5, GETDATE()), DATEADD(day, -5, GETDATE()), 'Worth every penny for this legendary card'),
    (2, 4, 3, 120.00, 'COMPLETED', DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()), 'Fast delivery, excellent condition');
    
    -- Add some pending transactions
    INSERT INTO dbo.Transactions (BuyerId, SellerId, CardId, Amount, Status, TransactionDate, Notes) VALUES
    (3, 5, 4, 100.00, 'PENDING', GETDATE(), 'Waiting for payment confirmation'),
    (4, 2, 5, 80.00, 'PENDING', GETDATE(), 'Processing transaction');
    
    PRINT 'Sample transactions added successfully.';
END

PRINT '=== Trading System Seed Data Complete ===';
PRINT 'Cards: 10 sample cards (6 approved with prices, 2 pending, 2 collection)';
PRINT 'Listings: 6 active marketplace listings';
PRINT 'Transactions: 3 completed + 2 pending transactions';
PRINT 'Ready for testing trading features!';

-- Insert regular users
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = 'alice')
BEGIN
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Role)
    VALUES ('alice', 'alice@example.com', 'seed-password', 'Alice Player', 'ROLE_USER');
END

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = 'bob')
BEGIN
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Role)
    VALUES ('bob', 'bob@example.com', 'seed-password', 'Bob Trader', 'ROLE_USER');
END

-- Insert sample cards
IF NOT EXISTS (SELECT 1 FROM dbo.Cards WHERE Name = 'Cristiano 99')
BEGIN
    INSERT INTO dbo.Cards (Name, Rarity, Team, Description, BaseImageUrl)
    VALUES ('Cristiano 99', 'Legendary', 'Al-Nassr', 'High-stat striker', 'https://example.com/img/cr7.png');
END

IF NOT EXISTS (SELECT 1 FROM dbo.Cards WHERE Name = 'Messi 98')
BEGIN
    INSERT INTO dbo.Cards (Name, Rarity, Team, Description, BaseImageUrl)
    VALUES ('Messi 98', 'Legendary', 'Inter Miami', 'Playmaking magician', 'https://example.com/img/messi.png');
END

-- Insert sample listings (use user ids and card ids by lookup)
IF NOT EXISTS (SELECT 1 FROM dbo.Listings WHERE Title = 'CR7 Rare Sell')
BEGIN
    DECLARE @sellerId INT = (SELECT TOP 1 Id FROM dbo.Users WHERE Username = 'alice');
    DECLARE @cardId INT = (SELECT TOP 1 Id FROM dbo.Cards WHERE Name = 'Cristiano 99');
    IF @sellerId IS NOT NULL AND @cardId IS NOT NULL
    BEGIN
        INSERT INTO dbo.Listings (SellerId, CardId, Title, Description, Price, Currency, Quantity, Status)
        VALUES (@sellerId, @cardId, 'CR7 Rare Sell', 'Selling a rare Cristiano card', 199.99, 'USD', 1, 'APPROVED');
    END
END

IF NOT EXISTS (SELECT 1 FROM dbo.Listings WHERE Title = 'Messi Trade Offer')
BEGIN
    DECLARE @sellerId2 INT = (SELECT TOP 1 Id FROM dbo.Users WHERE Username = 'bob');
    DECLARE @cardId2 INT = (SELECT TOP 1 Id FROM dbo.Cards WHERE Name = 'Messi 98');
    IF @sellerId2 IS NOT NULL AND @cardId2 IS NOT NULL
    BEGIN
        INSERT INTO dbo.Listings (SellerId, CardId, Title, Description, Price, Currency, Quantity, Status)
        VALUES (@sellerId2, @cardId2, 'Messi Trade Offer', 'Selling Messi card for trade', 149.50, 'USD', 1, 'PENDING');
    END
END

-- Insert listing images
IF NOT EXISTS (SELECT 1 FROM dbo.ListingImages WHERE ImageUrl LIKE '%cr7.png%')
BEGIN
    DECLARE @listingId INT = (SELECT TOP 1 Id FROM dbo.Listings WHERE Title = 'CR7 Rare Sell');
    IF @listingId IS NOT NULL
    BEGIN
        INSERT INTO dbo.ListingImages (ListingId, ImageUrl, Ordinal) VALUES (@listingId, 'https://example.com/img/cr7.png', 0);
    END
END

IF NOT EXISTS (SELECT 1 FROM dbo.ListingImages WHERE ImageUrl LIKE '%messi.png%')
BEGIN
    DECLARE @listingId2 INT = (SELECT TOP 1 Id FROM dbo.Listings WHERE Title = 'Messi Trade Offer');
    IF @listingId2 IS NOT NULL
    BEGIN
        INSERT INTO dbo.ListingImages (ListingId, ImageUrl, Ordinal) VALUES (@listingId2, 'https://example.com/img/messi.png', 0);
    END
END

SELECT 'Seed finished' AS Result;
