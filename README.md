# TicketBooker

TicketBooker là hệ thống quản lý đặt vé xe khách xây dựng bằng Spring Boot. Ứng dụng xử lý luồng tìm chuyến, chọn ghế, tạo hóa đơn, tạo vé, thanh toán và quản trị dữ liệu vận hành.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.4, Spring MVC |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Security | Spring Security, OAuth2 Client |
| View | Thymeleaf, Thymeleaf Layout Dialect, Thymeleaf Security |
| Integration | VNPay, ZaloPay, SMTP Mail, OpenAI API |
| Build/Test | Maven, JUnit, Spring Security Test, JaCoCo |
| Utilities | Lombok, Apache POI, Jackson, Apache HttpClient |

## Core Modules

| Module | Responsibility |
| --- | --- |
| `Controller` | MVC pages, admin screens, REST APIs, payment callbacks |
| `Service` | Business contracts for users, routes, buses, drivers, trips, seats, invoices, tickets |
| `Service/ServiceImp` | Business implementations and transaction orchestration |
| `Repository` | Spring Data JPA access layer |
| `Entity` | JPA mappings for relational schema |
| `DTO` | Request/response models for API and view binding |
| `Config` | Security, filters, external service configuration |
| `Util` | Mappers, enums, exception handling, scheduled cleanup, helpers |

## Main Features

- Search trips by route and departure date.
- Seat availability lookup per trip.
- Seat pre-booking before ticket creation.
- Invoice creation and payment status update.
- Multi-seat ticket booking through `ticket_seats`.
- Ticket cancellation and booking history lookup.
- Admin management for users, routes, buses, drivers, trips, tickets and invoices.
- Authentication with local account and OAuth2 providers.
- Payment integration with VNPay and ZaloPay.
- Email sending for account and booking workflows.
- Revenue, ticket and trip statistics.

## Database Architecture

The database uses a relational MySQL schema. Hibernate maps entities to existing tables; table generation is disabled with `spring.jpa.hibernate.ddl-auto=none`.

![ERD Diagram](docs/erd.png)

### Core Tables

| Table | Purpose | Key Relationships |
| --- | --- | --- |
| `Users` | User profile, login data, OAuth provider and role | One user can book many `Tickets` |
| `Routes` | Departure location, arrival location, estimated time and route status | One route has many `Buses` and `Trips` |
| `Driver` | Driver profile, license number and status | One driver can be assigned to many `Trips` |
| `Buses` | Bus license plate, bus type, capacity and status | Belongs to `Routes`; assigned to many `Trips` |
| `Trips` | Concrete trip schedule with route, bus, driver, price and seat count | Belongs to `Routes`, `Buses`, `Driver`; has many `Seats` and `Tickets` |
| `Seats` | Seat codes scoped by trip | Unique constraint on `(tripId, seatCode)` |
| `Invoices` | Total amount, payment method, payment time and payment status | Referenced by `Tickets` |
| `Tickets` | Passenger booking record, QR code and ticket status | Belongs to `Trips`, `Users`, `Invoices` |
| `ticket_seats` | Join table between tickets and seats | `UNIQUE(seatId)` prevents duplicated booking for the same seat |

### Data Write Flows

1. **Trip search**: client sends route/date filters to `/api/trips/search-trip`; service filters `Trips` by route, departure time and status.
2. **Seat lookup**: client calls `/api/seats/{tripId}/booked`; service reads `Tickets`, `Seats` and `ticket_seats` to calculate unavailable seats.
3. **Seat pre-booking**: client posts selected seats to `/api/seats/prebooking-seat`; service validates seat availability and stores selected seat IDs for the next booking step.
4. **Invoice creation**: client posts payment metadata to `/api/invoices/create`; service creates `Invoices` with initial payment status.
5. **Ticket creation**: client posts passenger, trip, invoice and seat data to `/api/tickets/create-ticket`; service creates `Tickets`, links seats through `ticket_seats` and updates available seats.
6. **Payment update**: VNPay/ZaloPay callbacks or status checks update `Invoices.paymentStatus`.
7. **Ticket cancellation**: client calls `/api/tickets/cancel-ticket`; service updates ticket status and related booking state.

## Project Structure

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

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Git

## Installation & Setup

Clone repository:

```bash
git clone <repository-url>
cd <repository-folder>
```

Install dependencies and run tests:

```bash
mvn clean test
```

Run application:

```bash
mvn spring-boot:run
```

Application URL:

```text
http://localhost:8000/greenbus
```

Admin pages are under:

```text
http://localhost:8000/admin
```

## Environment Configuration

Application configuration is loaded from `src/main/resources/application.properties`. Secrets are resolved from environment variables.

Create a local `.env` file from the sample:

```bash
cp .env.example .env
```

Required variables:

| Variable | Description |
| --- | --- |
| `DB_PASSWORD` | MySQL password for `spring.datasource.username` |
| `MAIL_PASSWORD` | Gmail SMTP app password |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `FACEBOOK_CLIENT_ID` | Facebook OAuth2 client ID |
| `FACEBOOK_CLIENT_SECRET` | Facebook OAuth2 client secret |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 client secret |
| `VNPAY_TMN_CODE` | VNPay terminal code |
| `VNPAY_HASH_SECRET` | VNPay hash secret |
| `ZALO_APP_ID` | ZaloPay app ID |
| `ZALO_KEY1` | ZaloPay signing key |
| `OPENAI_API_KEY` | OpenAI API key |

Default database configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ticketbooker
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
```

For local development without exporting environment variables globally, create `src/main/resources/application-local.properties` and run with:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Database Setup

Create schema:

```bash
mysql -u root -p < BusTicketManagement.sql
```

Seed sample data:

```bash
mysql -u root -p ticketbooker < insertdata.sql
```

Run additional changes if needed:

```bash
mysql -u root -p ticketbooker < alter.sql
```

Verify database:

```sql
USE ticketbooker;
SHOW TABLES;
```

## API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/trips/search-trip` | Search trips by route and departure date |
| `GET` | `/api/seats/{tripId}/booked` | Return booked seats of a trip |
| `POST` | `/api/seats/prebooking-seat` | Validate and hold selected seats before booking |
| `POST` | `/api/invoices/create` | Create invoice and return invoice data |
| `PUT` | `/api/invoices/{id}/status` | Update payment status |
| `POST` | `/api/tickets/create-ticket` | Create ticket and attach selected seats |
| `DELETE` | `/api/tickets/cancel-ticket` | Cancel a booked ticket |
| `POST` | `/payment/zalo-payment` | Create ZaloPay payment request |
| `GET` | `/vnpay/create-order` | Create VNPay payment URL |

## Admin Routes

| Route | Description |
| --- | --- |
| `/admin/users` | User management |
| `/admin/routes` | Route management |
| `/admin/buses` | Bus management |
| `/admin/drivers` | Driver management |
| `/admin/trips` | Trip management |
| `/admin/tickets` | Ticket management |
| `/admin/invoices` | Invoice management |
| `/admin/statistics` | Revenue, ticket and trip statistics |

## Testing

Run all tests:

```bash
mvn test
```

Generate JaCoCo report:

```bash
mvn clean test jacoco:report
```

Report output:

```text
target/site/jacoco/index.html
```

The Maven build includes a JaCoCo line coverage rule with minimum coverage `70%` for the configured bundle scope.

## Build Artifact

Create executable JAR:

```bash
mvn clean package
```

Run JAR:

```bash
java -jar target/ticketBooker-0.0.1-SNAPSHOT.jar
```

## Operational Notes

- Database schema is controlled by SQL scripts, not Hibernate auto DDL.
- Table and column names are preserved by `PhysicalNamingStrategyStandardImpl`.
- Payment configuration uses sandbox endpoints by default.
- ZIP artifacts under `src/main/resources/static/components/*.zip` are ignored and should not be committed.
- Do not commit local secrets, `.env`, `application-local.properties`, database dumps or generated build output.
