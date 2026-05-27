# TicketBooker

TicketBooker là hệ thống quản lý đặt vé xe khách xây dựng bằng Spring Boot. Ứng dụng xử lý luồng tìm chuyến, chọn ghế, tạo hóa đơn, tạo vé, thanh toán và quản trị dữ liệu vận hành.

## Công nghệ sử dụng

| Lớp | Công nghệ |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.4, Spring MVC |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Security | Spring Security, OAuth2 Client |
| View | Thymeleaf, Thymeleaf Layout Dialect, Thymeleaf Security |
| Tích hợp ngoài | VNPay, ZaloPay, SMTP Mail, OpenAI API |
| Build/Test | Maven, JUnit, Spring Security Test, JaCoCo |
| Tiện ích | Lombok, Apache POI, Jackson, Apache HttpClient |

## Module chính

| Module | Trách nhiệm |
| --- | --- |
| `Controller` | Xử lý trang MVC, màn hình quản trị, REST API và callback thanh toán |
| `Service` | Định nghĩa nghiệp vụ cho người dùng, tuyến, xe, tài xế, chuyến, ghế, hóa đơn, vé |
| `Service/ServiceImp` | Hiện thực nghiệp vụ và điều phối các luồng giao dịch |
| `Repository` | Truy cập dữ liệu qua Spring Data JPA |
| `Entity` | Mapping JPA với schema quan hệ |
| `DTO` | Model request/response cho API và binding form |
| `Config` | Cấu hình security, filter và dịch vụ tích hợp ngoài |
| `Util` | Mapper, enum, exception handler, scheduler và helper |

## Tính năng chính

- Tìm chuyến theo tuyến và ngày khởi hành.
- Tra cứu trạng thái ghế theo từng chuyến.
- Giữ ghế tạm trước khi tạo vé.
- Tạo hóa đơn và cập nhật trạng thái thanh toán.
- Đặt vé nhiều ghế thông qua bảng trung gian `ticket_seats`.
- Hủy vé và tra cứu lịch sử đặt vé.
- Quản trị người dùng, tuyến, xe, tài xế, chuyến, vé và hóa đơn.
- Đăng nhập bằng tài khoản local và OAuth2.
- Tích hợp thanh toán VNPay và ZaloPay.
- Gửi email cho các luồng tài khoản và đặt vé.
- Thống kê doanh thu, vé và chuyến xe.

## Kiến trúc cơ sở dữ liệu

Cơ sở dữ liệu sử dụng schema quan hệ trên MySQL. Hibernate chỉ mapping entity với bảng có sẵn; dự án tắt cơ chế tự sinh bảng bằng `spring.jpa.hibernate.ddl-auto=none`.

![ERD Diagram](docs/erd.png)

### Bảng cốt lõi

| Bảng | Vai trò | Quan hệ chính |
| --- | --- | --- |
| `Users` | Hồ sơ người dùng, thông tin đăng nhập, OAuth provider và phân quyền | Một người dùng có thể đặt nhiều `Tickets` |
| `Routes` | Điểm đi, điểm đến, thời gian dự kiến và trạng thái tuyến | Một tuyến có nhiều `Buses` và `Trips` |
| `Driver` | Thông tin tài xế, số bằng lái và trạng thái hoạt động | Một tài xế có thể được gán vào nhiều `Trips` |
| `Buses` | Biển số, loại xe, sức chứa và trạng thái xe | Thuộc `Routes`; được gán vào nhiều `Trips` |
| `Trips` | Lịch chạy cụ thể theo tuyến, xe, tài xế, giá vé và số ghế còn trống | Thuộc `Routes`, `Buses`, `Driver`; có nhiều `Seats` và `Tickets` |
| `Seats` | Mã ghế theo từng chuyến | Ràng buộc duy nhất `(tripId, seatCode)` |
| `Invoices` | Tổng tiền, phương thức thanh toán, thời gian thanh toán và trạng thái thanh toán | Được tham chiếu bởi `Tickets` |
| `Tickets` | Bản ghi đặt vé, thông tin hành khách, QR code và trạng thái vé | Thuộc `Trips`, `Users`, `Invoices` |
| `ticket_seats` | Bảng trung gian giữa vé và ghế | `UNIQUE(seatId)` ngăn đặt trùng cùng một ghế |

### Luồng ghi dữ liệu chính

1. **Tìm chuyến**: client gửi bộ lọc tuyến/ngày đến `/api/trips/search-trip`; service lọc `Trips` theo tuyến, thời gian khởi hành và trạng thái.
2. **Tra cứu ghế**: client gọi `/api/seats/{tripId}/booked`; service đọc `Tickets`, `Seats` và `ticket_seats` để xác định ghế không còn khả dụng.
3. **Giữ ghế tạm**: client gửi danh sách ghế đến `/api/seats/prebooking-seat`; service kiểm tra tính khả dụng và lưu danh sách `seatId` cho bước đặt vé.
4. **Tạo hóa đơn**: client gửi thông tin thanh toán đến `/api/invoices/create`; service tạo `Invoices` với trạng thái thanh toán ban đầu.
5. **Tạo vé**: client gửi thông tin hành khách, chuyến, hóa đơn và ghế đến `/api/tickets/create-ticket`; service tạo `Tickets`, liên kết ghế qua `ticket_seats` và cập nhật số ghế còn trống.
6. **Cập nhật thanh toán**: callback hoặc truy vấn trạng thái từ VNPay/ZaloPay cập nhật `Invoices.paymentStatus`.
7. **Hủy vé**: client gọi `/api/tickets/cancel-ticket`; service cập nhật trạng thái vé và các dữ liệu đặt chỗ liên quan.

## Cấu trúc thư mục

```text
.
|-- BusTicketManagement.sql
|-- insertdata.sql
|-- alter.sql
|-- pom.xml
|-- README.md
|-- src
|   |-- main
|   |   |-- java/com/example/ticketbooker
|   |   |   |-- Config
|   |   |   |-- Controller
|   |   |   |-- DTO
|   |   |   |-- Entity
|   |   |   |-- Exception
|   |   |   |-- Repository
|   |   |   |-- Service
|   |   |   |-- Util
|   |   |   `-- TicketBookerApplication.java
|   |   `-- resources
|   |       |-- application.properties
|   |       |-- static
|   |       `-- templates
|   `-- test
`-- .env.example
```

## Yêu cầu hệ thống

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Git

## Cài đặt và chạy dự án

Clone repository:

```bash
git clone <repository-url>
cd <repository-folder>
```

Cài đặt dependency và chạy test:

```bash
mvn clean test
```

Chạy ứng dụng:

```bash
mvn spring-boot:run
```

URL ứng dụng:

```text
http://localhost:8000/greenbus
```

Trang quản trị:

```text
http://localhost:8000/admin
```

## Cấu hình môi trường

Cấu hình chính nằm trong `src/main/resources/application.properties`. Các giá trị nhạy cảm được đọc từ biến môi trường.

Tạo file `.env` từ file mẫu:

```bash
cp .env.example .env
```

Các biến bắt buộc:

| Biến | Mô tả |
| --- | --- |
| `DB_PASSWORD` | Mật khẩu MySQL cho `spring.datasource.username` |
| `MAIL_PASSWORD` | App password của Gmail SMTP |
| `GOOGLE_CLIENT_ID` | Client ID OAuth2 Google |
| `GOOGLE_CLIENT_SECRET` | Client secret OAuth2 Google |
| `FACEBOOK_CLIENT_ID` | Client ID OAuth2 Facebook |
| `FACEBOOK_CLIENT_SECRET` | Client secret OAuth2 Facebook |
| `GITHUB_CLIENT_ID` | Client ID OAuth2 GitHub |
| `GITHUB_CLIENT_SECRET` | Client secret OAuth2 GitHub |
| `VNPAY_TMN_CODE` | Mã terminal VNPay |
| `VNPAY_HASH_SECRET` | Secret ký request VNPay |
| `ZALO_APP_ID` | App ID ZaloPay |
| `ZALO_KEY1` | Khóa ký request ZaloPay |
| `OPENAI_API_KEY` | API key OpenAI |

Cấu hình database mặc định:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ticketbooker
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
```

Nếu không muốn export biến môi trường toàn cục, tạo `src/main/resources/application-local.properties` và chạy:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Khởi tạo cơ sở dữ liệu

Tạo schema:

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

## API endpoints

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| `POST` | `/api/trips/search-trip` | Tìm chuyến theo tuyến và ngày khởi hành |
| `GET` | `/api/seats/{tripId}/booked` | Trả về danh sách ghế đã được đặt của một chuyến |
| `POST` | `/api/seats/prebooking-seat` | Kiểm tra và giữ danh sách ghế trước khi đặt vé |
| `POST` | `/api/invoices/create` | Tạo hóa đơn và trả về dữ liệu hóa đơn |
| `PUT` | `/api/invoices/{id}/status` | Cập nhật trạng thái thanh toán |
| `POST` | `/api/tickets/create-ticket` | Tạo vé và gắn danh sách ghế đã chọn |
| `DELETE` | `/api/tickets/cancel-ticket` | Hủy vé đã đặt |
| `POST` | `/payment/zalo-payment` | Tạo request thanh toán ZaloPay |
| `GET` | `/vnpay/create-order` | Tạo URL thanh toán VNPay |

## Tuyến quản trị

| Route | Mô tả |
| --- | --- |
| `/admin/users` | Quản lý người dùng |
| `/admin/routes` | Quản lý tuyến xe |
| `/admin/buses` | Quản lý xe |
| `/admin/drivers` | Quản lý tài xế |
| `/admin/trips` | Quản lý chuyến xe |
| `/admin/tickets` | Quản lý vé |
| `/admin/invoices` | Quản lý hóa đơn |
| `/admin/statistics` | Thống kê doanh thu, vé và chuyến |

## Kiểm thử

Chạy toàn bộ test:

```bash
mvn test
```

Tạo báo cáo JaCoCo:

```bash
mvn clean test jacoco:report
```

Đường dẫn báo cáo:

```text
target/site/jacoco/index.html
```

Build Maven đang cấu hình ngưỡng line coverage tối thiểu `70%` cho phạm vi bundle đã khai báo.

## Build artifact

Tạo file JAR:

```bash
mvn clean package
```

Chạy file JAR:

```bash
java -jar target/ticketBooker-0.0.1-SNAPSHOT.jar
```

## Ghi chú vận hành

- Schema database được quản lý bằng SQL script, không dùng Hibernate auto DDL.
- Tên bảng và tên cột được giữ nguyên bằng `PhysicalNamingStrategyStandardImpl`.
- Cấu hình thanh toán đang dùng sandbox endpoint mặc định.
- File ZIP trong `src/main/resources/static/components/*.zip` được ignore và không nên commit.
- Không commit secret local, `.env`, `application-local.properties`, database dump hoặc build output.
