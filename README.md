# TicketBooker

TicketBooker là hệ thống quản lý đặt vé xe khách xây dựng bằng Spring Boot. Ứng dụng hỗ trợ tìm chuyến, chọn ghế, giữ ghế tạm, tạo hóa đơn, đặt vé, thanh toán, gửi email và quản trị dữ liệu vận hành.

## Công Nghệ

| Lớp | Công nghệ |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.4, Spring MVC |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Security | Spring Security, OAuth2 Client |
| View | Thymeleaf, Thymeleaf Layout Dialect, Thymeleaf Security |
| Tích hợp ngoài | VNPay, ZaloPay, SMTP Mail, OpenAI API |
| Build/Test | Maven, JUnit 5, Mockito, Spring Test, MockMvc, JaCoCo |
| Vận hành | Docker, Docker Compose, GitHub Actions |
| Tối ưu | Spring Cache, HTTP compression, static resource cache, read-only transaction |

## Tính Năng Chính

- Tìm chuyến theo tuyến và ngày khởi hành.
- Xem trạng thái ghế theo từng chuyến.
- Giữ ghế tạm trước khi đặt vé.
- Tạo hóa đơn và cập nhật trạng thái thanh toán.
- Đặt vé nhiều ghế qua bảng trung gian `ticket_seats`.
- Hủy vé và tra cứu lịch sử đặt vé.
- Quản trị người dùng, tuyến, xe, tài xế, chuyến, vé, hóa đơn và thống kê.
- Đăng nhập local và OAuth2 Google/Facebook/GitHub.
- Tích hợp thanh toán VNPay và ZaloPay.
- Gửi email cho các luồng tài khoản, đặt vé và hoàn tất chuyến.
- Scheduler tự cập nhật trạng thái vé/chuyến và dọn ghế tạm hết hạn.

## Cấu Trúc Chính

```text
.
|-- BusTicketManagement.sql
|-- insertdata.sql
|-- alter.sql
|-- docker-compose.yml
|-- Dockerfile
|-- pom.xml
|-- README.md
|-- src
|   |-- main
|   |   |-- java/com/example/ticketbooker
|   |   |   |-- Config
|   |   |   |-- Controller
|   |   |   |-- DTO
|   |   |   |-- Entity
|   |   |   |-- Repository
|   |   |   |-- Service
|   |   |   |-- Util
|   |   |   `-- TicketBookerApplication.java
|   |   `-- resources
|   |       |-- application.properties
|   |       |-- application-docker.properties
|   |       |-- application-prod.properties
|   |       |-- static
|   |       `-- templates
|   `-- test
|       |-- java
|       `-- resources/application.properties
`-- .env.example
```

## Yêu Cầu

- Java 21
- Maven 3.9+
- MySQL 8.0+ nếu chạy không dùng Docker
- Docker/Docker Compose nếu chạy bằng container

## Cấu Hình Môi Trường

Ứng dụng đọc secret từ biến môi trường. Tạo file `.env` từ mẫu:

```bash
cp .env.example .env
```

Các biến chính:

| Biến | Mô tả |
| --- | --- |
| `DB_PASSWORD` | Mật khẩu MySQL user `root` |
| `MYSQL_PORT` | Port expose MySQL khi chạy Docker, mặc định `3306` |
| `APP_PORT` | Port expose ứng dụng khi chạy Docker, mặc định `8000` |
| `MAIL_PASSWORD` | App password SMTP |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | OAuth2 Google |
| `FACEBOOK_CLIENT_ID`, `FACEBOOK_CLIENT_SECRET` | OAuth2 Facebook |
| `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` | OAuth2 GitHub |
| `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` | Cấu hình VNPay |
| `ZALO_APP_ID`, `ZALO_KEY1` | Cấu hình ZaloPay |
| `OPENAI_API_KEY` | API key OpenAI |
| `SPRING_AI_OPENAI_API_KEY` | API key Spring AI/OpenAI |
| `SPRING_AI_OPENAI_BASE_URL` | Base URL cho Spring AI |
| `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` | Model dùng cho Spring AI |

Không commit `.env` hoặc `src/main/resources/application-local.properties`.

## Chạy Bằng Docker

Docker Compose chạy cả ứng dụng và MySQL. Lần khởi động đầu tiên MySQL sẽ init schema/data từ:

- `BusTicketManagement.sql`
- `insertdata.sql`

Chạy:

```bash
docker compose up --build
```

Ứng dụng:

```text
http://localhost:8000
```

Dừng container:

```bash
docker compose down
```

Xóa cả volume database để init lại từ đầu:

```bash
docker compose down -v
```

## Chạy Local Không Dùng Docker

Tạo database:

```bash
mysql -u root -p < BusTicketManagement.sql
mysql -u root -p ticketbooker < insertdata.sql
```

Chạy ứng dụng:

```bash
mvn spring-boot:run
```

Nếu dùng cấu hình local riêng:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

URL:

```text
http://localhost:8000
```

Trang quản trị:

```text
http://localhost:8000/admin
```

## Profile

| Profile | Mục đích |
| --- | --- |
| mặc định | Chạy local với MySQL tại `localhost:3306` |
| `docker` | Chạy trong Docker Compose, datasource trỏ tới service `mysql` |
| `prod` | Tắt SQL debug, tắt Open Session in View, bật cache static |
| test resources | Test dùng H2 in-memory, tắt scheduler, dùng dummy secret |

Chạy production profile:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/ticketBooker-0.0.1-SNAPSHOT.jar
```

## Kiểm Thử

Chạy toàn bộ test:

```bash
mvn test
```

Chạy verify đầy đủ, bao gồm JaCoCo check:

```bash
mvn verify
```

Ngưỡng coverage hiện tại:

```text
Line coverage tối thiểu: 90%
```

Báo cáo JaCoCo:

```text
target/site/jacoco/index.html
```

Test hiện gồm unit test và integration test dùng Spring Test/MockMvc. Integration test không phụ thuộc MySQL thật vì dùng H2 in-memory.

## CI

Repository có GitHub Actions tại:

```text
.github/workflows/ci.yml
```

Pipeline chạy:

```bash
mvn -B verify
```

Nếu test fail hoặc coverage dưới 90%, CI sẽ fail.

## Build Artifact

Tạo JAR:

```bash
mvn clean package
```

Chạy JAR:

```bash
java -jar target/ticketBooker-0.0.1-SNAPSHOT.jar
```

## API Chính

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| `POST` | `/api/trips/search-trip` | Tìm chuyến theo tuyến/ngày |
| `GET` | `/api/seats/{tripId}/booked` | Lấy danh sách ghế đã đặt |
| `POST` | `/api/seats/prebooking-seat` | Giữ ghế tạm |
| `POST` | `/api/invoices/create` | Tạo hóa đơn |
| `PUT` | `/api/invoices/{id}/status` | Cập nhật trạng thái thanh toán |
| `POST` | `/api/tickets/create-ticket` | Tạo vé |
| `DELETE` | `/api/tickets/cancel-ticket` | Hủy vé |
| `POST` | `/payment/zalo-payment` | Tạo request thanh toán ZaloPay |
| `GET` | `/vnpay/create-order` | Tạo URL thanh toán VNPay |

## Route Quản Trị

| Route | Mô tả |
| --- | --- |
| `/admin/users` | Quản lý người dùng |
| `/admin/routes` | Quản lý tuyến |
| `/admin/buses` | Quản lý xe |
| `/admin/drivers` | Quản lý tài xế |
| `/admin/trips` | Quản lý chuyến |
| `/admin/tickets` | Quản lý vé |
| `/admin/invoices` | Quản lý hóa đơn |
| `/admin/statistics` | Thống kê |

## Tối Ưu Đã Áp Dụng

- Bật HTTP compression.
- Bật static resource cache cho `docker` và `prod`.
- Tách scheduling bằng `app.scheduling.enabled`.
- Test tắt scheduler để tránh tác vụ nền ảnh hưởng kết quả.
- Dùng H2 in-memory cho test, không cần MySQL local.
- Dùng `@Transactional(readOnly = true)` cho các service đọc dữ liệu.
- Thêm Spring Cache cho dữ liệu lookup form admin:
  - route options
  - bus options
  - driver options
- Tự clear cache khi thêm/sửa/xóa tuyến, xe, tài xế.
- Docker image chạy bằng user non-root và có JVM memory flags.
- Loại trùng dependency JSON trong test.
- Loại `commons-logging` conflict với `spring-jcl`.
- Dọn các log debug/`System.out` không cần thiết ở một số luồng chính.

## Ghi Chú Vận Hành

- Schema database được quản lý bằng SQL script, không dùng Hibernate auto DDL.
- Tên bảng/cột được giữ nguyên bằng `PhysicalNamingStrategyStandardImpl`.
- Các endpoint thanh toán đang dùng sandbox/default endpoint.
- `.env`, `application-local.properties`, database dump và build output không nên commit.
- Nếu thay đổi dữ liệu lookup ngoài service, cần chú ý cache invalidation tương ứng.
