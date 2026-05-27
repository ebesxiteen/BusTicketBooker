# TicketBooker

Hệ thống quản lý đặt vé xe khách xây dựng bằng Spring Boot. Dự án xử lý tìm chuyến, đặt vé, giữ ghế, lập hóa đơn, cập nhật trạng thái thanh toán và quản trị tuyến xe, chuyến xe, xe, tài xế, người dùng.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.4, Spring MVC |
| Persistence | Spring Data JPA, Hibernate, MySQL |
| Security | Spring Security, OAuth2 Client |
| View | Thymeleaf, Thymeleaf Layout Dialect |
| Integration | VNPay, ZaloPay, SMTP Mail, OpenAI API |
| Build/Test | Maven, JUnit, JaCoCo |
| Frontend Assets | HTML, CSS, JavaScript, Tailwind CSS |

## Database Architecture

Database sử dụng MySQL với schema quan hệ. Hibernate chỉ mapping entity, không tự sinh bảng do `spring.jpa.hibernate.ddl-auto=none`.


### Core Entities

| Entity/Table | Vai trò | Quan hệ chính |
| --- | --- | --- |
| `Users` | Hồ sơ người dùng, thông tin đăng nhập, OAuth provider, phân quyền | Một user có thể đặt nhiều `Tickets` |
| `Routes` | Tuyến đường: điểm đi, điểm đến, thời gian ước tính | Một route có nhiều `Buses` và `Trips` |
| `Driver` | Thông tin tài xế và trạng thái hoạt động | Một driver được gán vào nhiều `Trips` |
| `Buses` | Xe, biển số, loại xe, sức chứa, trạng thái | Thuộc một `Routes`, được gán vào nhiều `Trips` |
| `Trips` | Chuyến xe cụ thể theo tuyến, xe, tài xế và thời gian khởi hành | Thuộc `Routes`, `Buses`, `Driver`; có nhiều `Seats` và `Tickets` |
| `Seats` | Ghế theo từng chuyến xe | Thuộc một `Trips`; mỗi `seatCode` là duy nhất trong một trip |
| `Tickets` | Vé đặt bởi người dùng hoặc khách vãng lai | Thuộc `Trips`, `Users`, `Invoices`; liên kết nhiều `Seats` |
| `ticket_seats` | Bảng trung gian vé-ghế | Ràng buộc một ghế chỉ thuộc một vé |
| `Invoices` | Hóa đơn, số tiền, phương thức và trạng thái thanh toán | Được tham chiếu bởi `Tickets` |

### Data Flow

1. **Tra cứu chuyến**: client gửi điểm đi, điểm đến và ngày đi tới `/api/trips/search-trip`; service lọc `Trips` theo `Routes`, thời gian và trạng thái.
2. **Tải sơ đồ ghế**: client gọi `/api/seats/{tripId}/booked`; hệ thống đọc `Tickets`, `ticket_seats` và `Seats` để xác định ghế đã được giữ/đặt.
3. **Tạo hóa đơn**: client tạo `Invoices` qua `/api/invoices/create` với tổng tiền, phương thức thanh toán và trạng thái ban đầu.
4. **Tạo vé**: client gửi thông tin hành khách, trip, invoice và danh sách ghế tới `/api/tickets/create-ticket`; service ghi `Tickets`, liên kết `Seats` qua `ticket_seats` và giảm ghế trống của `Trips`.
5. **Thanh toán**: VNPay/ZaloPay xử lý giao dịch bên ngoài; callback hoặc polling cập nhật `Invoices.paymentStatus`.
6. **Hủy vé**: client gọi `/api/tickets/cancel-ticket`; service chuyển `Tickets.ticketStatus` sang `CANCELLED`, đồng bộ trạng thái liên quan và gửi email nếu có thông tin người đặt.

## Main Features

- Quản lý tuyến xe, xe, tài xế, chuyến xe, ghế, vé, hóa đơn và người dùng.
- Tìm chuyến theo tuyến và ngày khởi hành.
- Đặt vé nhiều ghế trên một chuyến thông qua bảng trung gian `ticket_seats`.
- Ràng buộc chống đặt trùng ghế bằng unique constraint trên `ticket_seats.seatId`.
- Theo dõi trạng thái vé: `BOOKED`, `CANCELLED`, `USED`.
- Theo dõi trạng thái chuyến: `SCHEDULED`, `COMPLETED`, `CANCELLED`.
- Theo dõi trạng thái thanh toán: `PENDING`, `PAID`, `CANCELLED`.
- Đăng nhập local và OAuth2 qua Google, Facebook, GitHub.
- Tích hợp VNPay, ZaloPay, SMTP Mail và OpenAI API.
- Trang quản trị bằng Thymeleaf cho users, routes, buses, drivers, trips, tickets, invoices và thống kê.

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Node.js 18+ nếu cần build Tailwind CSS

## Installation & Setup

```bash
git clone <repository-url>
cd TicketBooker
```

```bash
mvn clean install
```

```bash
mvn spring-boot:run
```

Ứng dụng chạy tại:

```text
http://localhost:8000/greenbus
```

Chạy test:

```bash
mvn test
```

## Environment Configuration

Dự án đọc cấu hình chính từ `src/main/resources/application.properties` và các secret từ biến môi trường.

Tạo file `.env` dựa trên `.env.example`:

```bash
cp .env.example .env
```

Các biến môi trường chính:

| Variable | Mục đích |
| --- | --- |
| `DB_PASSWORD` | Mật khẩu MySQL user đang cấu hình trong `application.properties` |
| `MAIL_PASSWORD` | App password cho SMTP Gmail |
| `GOOGLE_CLIENT_ID` | OAuth2 Google client ID |
| `GOOGLE_CLIENT_SECRET` | OAuth2 Google client secret |
| `FACEBOOK_CLIENT_ID` | OAuth2 Facebook client ID |
| `FACEBOOK_CLIENT_SECRET` | OAuth2 Facebook client secret |
| `GITHUB_CLIENT_ID` | OAuth2 GitHub client ID |
| `GITHUB_CLIENT_SECRET` | OAuth2 GitHub client secret |
| `VNPAY_TMN_CODE` | Mã terminal VNPay |
| `VNPAY_HASH_SECRET` | Secret ký request VNPay |
| `ZALO_APP_ID` | ZaloPay app ID |
| `ZALO_KEY1` | ZaloPay key dùng để ký request |
| `OPENAI_API_KEY` | API key cho OpenAI integration |

Cấu hình database mặc định:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ticketbooker
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
```

## Database Setup

Khởi tạo schema:

```bash
mysql -u root -p < BusTicketManagement.sql
```

Seed dữ liệu mẫu:

```bash
mysql -u root -p ticketbooker < insertdata.sql
```

Chạy script bổ sung nếu cần:

```bash
mysql -u root -p ticketbooker < alter.sql
```

Kiểm tra database:

```sql
USE ticketbooker;
SHOW TABLES;
```

## API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/invoices/create` | Tạo hóa đơn và trả về `invoiceId` |
| `PUT` | `/api/invoices/{id}/status` | Cập nhật trạng thái thanh toán của hóa đơn |
| `POST` | `/api/tickets/create-ticket` | Tạo vé, liên kết ghế với vé và lưu thông tin người đặt |
| `DELETE` | `/api/tickets/cancel-ticket` | Hủy vé và cập nhật trạng thái vé |
| `POST` | `/api/seats/prebooking-seat` | Giữ hoặc kiểm tra ghế trước khi đặt |
| `DELETE` | `/api/trips/delete` | Xóa chuyến xe ở luồng quản trị |

## Project Structure

```text
src/main/java/com/example/ticketbooker
├── Config/          # Security, filter, external service config
├── Controller/      # MVC controllers, REST APIs, payment callbacks
├── DTO/             # Request/response models
├── Entity/          # JPA entities
├── Repository/      # Spring Data JPA repositories
├── Service/         # Business interfaces
├── ServiceImp/      # Business implementations
└── Util/            # Mapper, enum, scheduler, helper classes

src/main/resources
├── templates/       # Thymeleaf views
├── static/          # CSS, JavaScript, images
└── application.properties
```

## Build Notes

- Database schema được quản lý bằng SQL script, không dùng Hibernate auto DDL.
- Entity naming dùng `PhysicalNamingStrategyStandardImpl` để giữ nguyên tên cột/bảng theo schema.
- JaCoCo được cấu hình trong Maven với ngưỡng line coverage tối thiểu `70%` cho bundle sau khi loại trừ controller, repository, service, entity, DTO, config và static resources.
