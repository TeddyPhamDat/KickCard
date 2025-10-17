# KickCard Database Setup Guide

## Hướng dẫn tạo database mới nhất cho hệ thống Trading (Đã đơn giản hóa)

### ✨ **Thay đổi quan trọng: Đã loại bỏ Listing tables**
- **Trước**: Card + Listing + ListingImage (phức tạp)
- **Bây giờ**: Chỉ cần Card (đơn giản, Card có sẵn price và baseImageUrl)

### Bước 1: Xóa database cũ (nếu có)
```sql
USE master;
DROP DATABASE IF EXISTS KickCard;
```

### Bước 2: Tạo database và users
1. Chạy script: `sql/setup_database.sql`
   - Tạo database KickCard
   - Tạo admin user và sample users với wallet balance
   - Password cho tất cả users: **123456**

### Bước 3: Khởi động Spring Boot
1. Chạy: `./gradlew bootRun`
2. Spring Boot sẽ tự động tạo 3 tables chính:
   - **Users** (với walletBalance)
   - **Cards** (với price, baseImageUrl)
   - **Transactions**
   - Indexes và foreign keys

### Bước 4: Thêm seed data (optional)
1. Chạy script: `sql/seed_data.sql`
   - Thêm 10 sample cards (6 có giá, 2 pending, 2 collection)
   - Thêm sample transactions
   - **Không cần Listing data nữa!**

## User Accounts Đã Tạo

| Username | Password | Role | Wallet Balance | Email |
|----------|----------|------|----------------|-------|
| admin | 123456 | ADMIN | $10,000.00 | admin@kickcard.com |
| user1 | 123456 | USER | $500.00 | user1@example.com |
| user2 | 123456 | USER | $750.00 | user2@example.com |
| trader1 | 123456 | USER | $1,200.00 | trader1@example.com |
| collector1 | 123456 | USER | $2,000.00 | collector1@example.com |

## Simplified Trading Features

### API Endpoints (Đã đơn giản hóa)
- `GET /api/home/listings` - Hiển thị cards có price (marketplace)
- `GET /api/home/cards` - Xem tất cả approved cards
- `GET /api/home/cards/{id}` - Chi tiết card
- `POST /api/trading/buy/{cardId}` - Mua card trực tiếp
- `GET /api/wallet/balance` - Xem số dư ví
- `POST /api/wallet/topup` - Nạp tiền vào ví
- `POST /api/wallet/withdraw` - Rút tiền từ ví

### Cards Làm Marketplace
- Cards với `price != null` → Có thể mua
- Cards với `price = null` → Collection only
- Cards với `status = "APPROVED"` → Hiển thị public
- Cards với `status = "PENDING"` → Chờ admin duyệt

### Swagger UI
- Access: http://localhost:8080/swagger-ui/index.html
- Test tất cả API endpoints
- Sử dụng JWT token để authenticate

## Database Schema (Đã đơn giản hóa)

### 3 Tables Chính (Không cần Listing tables)
```
Users: id, username, email, passwordHash, fullname, phone, address, avatarUrl, role, walletBalance
Cards: id, name, description, baseImageUrl, rarity, team, ownerId, status, price, rejectionReason, createdAt
Transactions: id, buyerId, sellerId, cardId, amount, status, transactionDate, completedDate, notes
```

## Benefits of Simplification
✅ **Ít tables hơn** - Dễ maintain
✅ **Logic đơn giản hơn** - Card tự handle marketplace
✅ **Performance tốt hơn** - Ít JOIN queries
✅ **Code sạch hơn** - Ít repository và model

## Ready for Testing!
Database KickCard đã được **đơn giản hóa** với chỉ 3 tables chính. Spring Boot sẽ tự động handle schema generation và relationships.