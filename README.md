# KickCard

Hướng dẫn nhanh để cấu hình và chạy Backend (BE) của dự án KickCard.

## Yêu cầu trước

- Java JDK (phiên bản tương thích: JDK 17 được khuyến nghị; project hiện cấu hình `org.gradle.java.home` trỏ tới JDK22 trong `backend/gradle.properties`).
- Gradle wrapper (đã có sẵn: `gradlew` / `gradlew.bat`).
- SQL Server (hoặc container tương đương) để chạy cơ sở dữ liệu KickCard.

## Cấu hình cơ sở dữ liệu

Tệp cấu hình chính nằm ở `backend/src/main/resources/application.properties`.
Mặc định cấu hình hiện tại sử dụng SQL Server với các giá trị sau (để chạy local theo mặc định):

chạy BE để tự sinh ra các bảng của db

Sau khi bạn tạo database , trong thư mục `backend/sql/` có các script:

- `create_kickcard_database.sql` — tạo database dữ liệu để đăng nhập
- `seed.sql` / `seed_data.sql` — dữ liệu mẫu

Bạn có thể chạy các script này bằng SQL Server Management Studio (SSMS)




## Cấu hình JDK/Gradle

- Project chứa Gradle Wrapper; trên Windows dùng `.
\gradlew.bat` hoặc từ root repo: `.
gradlew.bat`.
- Trong `backend/gradle.properties` có dòng `org.gradle.java.home=C:/Program Files/Java/jdk-22` — nếu máy bạn không cài JDK22 hãy chỉnh đường dẫn này hoặc cài JDK tương ứng. Thực tế mã được biên dịch ở target Java 17 (tùy chọn `--release 17`).

## Chạy Backend (development)

**Cách chạy đúng từ PowerShell:**

1) Đặt JAVA_HOME và chạy từ thư mục backend:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-22'
cd D:\FPT\Semester8\PRM392\Assignment\KickCard\backend
.\gradlew.bat bootRun
```

2) Hoặc tất cả trong một dòng từ thư mục gốc:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-22'; cd backend; .\gradlew.bat bootRun
```

**Lưu ý quan trọng:**
- Phải set `$env:JAVA_HOME` trước khi chạy để Gradle tìm đúng JDK
- Chạy `.\gradlew.bat bootRun` từ trong thư mục `backend/` (không phải từ root)
- Đường dẫn JDK có thể khác tùy máy, kiểm tra `backend/gradle.properties`

Ứng dụng sẽ chạy dưới dạng Spring Boot thường trên cổng mặc định (8080) trừ khi cấu hình khác trong `application.properties`.

## Build và chạy file jar

1) Build (set JAVA_HOME trước):

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-22'
cd backend
.\gradlew.bat bootJar
```

2) Chạy jar (ví dụ):

```powershell
java -jar build\libs\backend-0.0.1-SNAPSHOT.jar
```

Lưu ý: tên file jar có thể khác nếu `group`/`version` thay đổi; kiểm tra thư mục `backend\build\libs`.

## Biến môi trường và bảo mật

- Hiện `application.properties` lưu JWT secret và thông tin DB ở dạng plaintext (để phát triển). Thay đổi `app.jwt.secret` bằng giá trị an toàn trong môi trường production hoặc sử dụng biến môi trường.
- Bạn có thể override cấu hình Spring bằng biến môi trường hoặc `application-{profile}.properties`.

**Ví dụ chạy với biến môi trường (PowerShell):**

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-22'
$env:SPRING_DATASOURCE_URL='jdbc:sqlserver://localhost:1433;databaseName=KickCard;encrypt=true;trustServerCertificate=true'
$env:SPRING_DATASOURCE_USERNAME='sa'
$env:SPRING_DATASOURCE_PASSWORD='12345'
cd backend
.\gradlew.bat bootRun
```


## Debug / Troubleshooting

- Nếu gặp lỗi về JDK/Gradle, kiểm tra `backend/gradle.properties` và đảm bảo `org.gradle.java.home` trỏ tới JDK hợp lệ.
- Nếu ứng dụng không kết nối được DB, kiểm tra TCP port (1433) và rằng SQL Server đang chạy; thử tắt `encrypt=true` hoặc bật `trustServerCertificate=true` theo nhu cầu.
- Kiểm tra logs; Spring Security debug bật sẵn trong `application.properties` (logging.level.org.springframework.security=DEBUG) để hỗ trợ gỡ lỗi authorization.

## Ghi chú thêm

- Thư mục backend sử dụng Spring Boot, Spring Data JPA, SQL Server JDBC, Spring Security và JWT.
- Nếu muốn chạy toàn bộ dự án (frontend + backend), xem README riêng của frontend (nếu có) và điều chỉnh CORS / endpoint tương ứng.

---



