-- Seed data for KickCard Trading System - Run after Spring Boot creates tables
-- This script adds sample cards with trading features (No Listing tables needed)

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

PRINT '=== Simplified Trading System Seed Data Complete ===';
PRINT 'Cards: 10 sample cards (6 approved with prices, 2 pending, 2 collection)';
PRINT 'Transactions: 3 completed + 2 pending transactions';
PRINT 'No Listing tables - Cards handle marketplace directly!';
PRINT 'Ready for testing simplified trading features!';

GO