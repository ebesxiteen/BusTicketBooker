USE ticketbooker;

-- TẮT TẠM THỜI RÀNG BUỘC KHÓA NGOẠI KHI KHỞI TẠO DỮ LIỆU
SET FOREIGN_KEY_CHECKS = 0;

-- XÓA DỮ LIỆU CŨ TRƯỚC KHI CHÈN MỚI
TRUNCATE TABLE ticket_seats;
TRUNCATE TABLE Tickets;
TRUNCATE TABLE Invoices;
TRUNCATE TABLE Seats;
TRUNCATE TABLE Trips;
TRUNCATE TABLE Buses;
TRUNCATE TABLE Driver;
TRUNCATE TABLE Routes;
TRUNCATE TABLE Users;

-- BẬT LẠI RÀNG BUỘC SAU KHI DỌN DẸP
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 1. USERS
-- =========================================
SET @BCRYPT_PASS = '$2a$10$0JT96MJWgK9/0SquImmnDuV7r0T8.y.RMP27OtbHeC9dn1M1bYCMG';

INSERT INTO Users (
    fullName, email, password, phone, address, gender, dateOfBirth,
    role, provider, userStatus, enabled
) VALUES 
('Quản Trị Hệ Thống', 'admin@greenbus.vn', @BCRYPT_PASS, '0901234567', 'Quận 1, TP.HCM', 'MALE',   '1990-01-01', 'ADMIN', 'LOCAL', 'ACTIVE', 1),
('Trần Nhân Viên',   'staff@greenbus.vn', @BCRYPT_PASS, '0909000222', 'Quận 3, TP.HCM', 'FEMALE', '1995-05-05', 'STAFF', 'LOCAL', 'ACTIVE', 1),
('Lê Khách Hàng',    'customer.greenbus@gmail.com',@BCRYPT_PASS, '0909000333', 'TP. Đà Lạt',      'FEMALE', '2000-10-10', 'USER',  'LOCAL', 'ACTIVE', 1), -- ID 3 (BOOKER)
('Google User Test', 'googleuser@gmail.com', NULL, NULL, NULL,     'MALE',     NULL,    'USER',  'GOOGLE','ACTIVE', 1);

-- Thêm một số user mẫu khác
INSERT INTO Users (
    fullName, email, password, phone, address, gender, dateOfBirth,
    role, provider, userStatus, enabled
) VALUES
('Nguyễn Văn A', 'nguyenvana@example.com', @BCRYPT_PASS, '0909000444', 'Hà Nội', 'MALE', '1992-02-02', 'USER', 'LOCAL', 'ACTIVE', 1),
('Phạm Thị B',   'phamthib@example.com',   @BCRYPT_PASS, '0909000555', 'Quảng Ngãi', 'FEMALE', '1998-07-07', 'USER', 'LOCAL', 'ACTIVE', 1),
('Trần Khách C', 'khachc@example.com',     @BCRYPT_PASS, '0909000666', 'Đà Nẵng', 'MALE', '1988-11-11', 'USER', 'LOCAL', 'ACTIVE', 1),
('Facebook Test', 'fbuser@example.com', NULL, '0909000777', 'TP. HCM', 'FEMALE', NULL, 'USER', 'FACEBOOK', 'ACTIVE', 1);

-- =========================================
-- 2. ROUTES
-- =========================================
INSERT INTO Routes (departureLocation, arrivalLocation, estimatedTime, routeStatus) VALUES 
('TP. Hồ Chí Minh', 'TP. Đà Lạt',  '06:00:00', 'ACTIVE'), -- ID 1
('TP. Hồ Chí Minh', 'TP. Nha Trang','08:00:00', 'ACTIVE'), -- ID 2
('TP. Hồ Chí Minh', 'TP. Đà Nẵng', '12:00:00', 'ACTIVE'), -- ID 3
('TP. Nha Trang',   'TP. Hồ Chí Minh','08:00:00', 'ACTIVE'), -- ID 4
('TP. Nha Trang',   'TP. Đà Lạt', '06:00:00', 'ACTIVE'), -- ID 5
('TP. Nha Trang',   'TP. Đà Nẵng','04:00:00', 'ACTIVE'), -- ID 6
('TP. Đà Nẵng',     'TP. Hồ Chí Minh','12:00:00', 'ACTIVE'), -- ID 7
('TP. Đà Nẵng',     'TP. Đà Lạt','06:00:00', 'ACTIVE'), -- ID 8
('TP. Đà Nẵng',     'TP. Nha Trang','04:00:00', 'ACTIVE'); -- ID 9

-- =========================================
-- 3. DRIVERS
-- =========================================
INSERT INTO Driver (name, licenseNumber, phone, address, driverStatus) VALUES 
('Nguyễn Văn Tài', 'B2-001234', '0988888111', 'Bình Thạnh, HCM', 'ACTIVE'), -- ID 1
('Lê Văn Xế',      'D-005678',  '0988888222', 'Thủ Đức, HCM',     'ACTIVE'), -- ID 2
('Phạm Thị Thảo', 'B2-009876', '0901234789', 'Quận 7, HCM', 'ACTIVE'), -- ID 3
('Hoàng Văn Dũng', 'D-003456',  '0912345678', 'Biên Hòa, Đồng Nai', 'ACTIVE'), -- ID 4
('Trần Mai Hương', 'B2-001122', '0977666555', 'Nha Trang, Khánh Hòa', 'ACTIVE'); -- ID 5

-- =========================================
-- 4. BUSES
-- =========================================
INSERT INTO Buses (routeId, licensePlate, busType, capacity, busStatus) VALUES 
(1, '51B-123.45', 'BEDSEAT', 36, 'ACTIVE'), -- busId 1 (Capacity 36)
(1, '51B-333.44', 'BEDSEAT', 40, 'ACTIVE'), -- busId 2 (Capacity 40)
(2, '79B-111.22', 'BEDSEAT', 40, 'ACTIVE'), -- busId 3 (Capacity 40)
(2, '51B-999.88', 'SEAT',    45, 'ACTIVE'), -- busId 4 (Capacity 45)
(3, '43B-001.23', 'BEDSEAT', 40, 'ACTIVE'), -- busId 5 (Capacity 40)
(3, '51B-777.66', 'BEDSEAT', 34, 'ACTIVE'), -- busId 6 (Capacity 34)
(4, '79B-222.33', 'BEDSEAT', 40, 'ACTIVE'), -- busId 7 (Capacity 40)
(4, '79B-444.55', 'SEAT',    45, 'ACTIVE'), -- busId 8 (Capacity 45)
(5, '79B-555.66', 'SEAT',    29, 'ACTIVE'), -- busId 9 (Capacity 29)
(5, '49B-121.21', 'BEDSEAT', 36, 'ACTIVE'), -- busId 10 (Capacity 36)
(6, '79B-888.99', 'BEDSEAT', 40, 'ACTIVE'), -- busId 11 (Capacity 40)
(6, '43B-456.78', 'BEDSEAT', 40, 'ACTIVE'), -- busId 12 (Capacity 40)
(7, '43B-654.32', 'BEDSEAT', 34, 'ACTIVE'), -- busId 13 (Capacity 34)
(7, '43B-112.23', 'BEDSEAT', 40, 'ACTIVE'), -- busId 14 (Capacity 40)
(8, '43B-987.65', 'BEDSEAT', 40, 'ACTIVE'), -- busId 15 (Capacity 40)
(8, '49B-567.12', 'BEDSEAT', 36, 'ACTIVE'), -- busId 16 (Capacity 36)
(9, '43B-333.22', 'SEAT',    45, 'ACTIVE'), -- busId 17 (Capacity 45)
(9, '79B-777.11', 'BEDSEAT', 40, 'ACTIVE'); -- busId 18 (Capacity 40)

-- =========================================
-- 5. TRIPS 
-- =========================================
INSERT INTO Trips (
    routeId, busId, driverId,
    departureStation, arrivalStation,
    departureTime, arrivalTime,
    price, availableSeats, tripStatus
) VALUES 
(1, 1, 1, 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 7 HOUR), 300000, 0, 'COMPLETED'), -- Trip 1
(1, 1, 2, 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt', CONCAT(CURDATE(), ' 08:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 7 HOUR), 320000, 34, 'SCHEDULED'), -- Trip 2
(1, 2, 3, 'Văn phòng Quận 1', 'Bến xe Liên Tỉnh Đà Lạt', CONCAT(CURDATE(), ' 22:30:00'), DATE_ADD(CONCAT(CURDATE(), ' 22:30:00'), INTERVAL 6 HOUR), 350000, 38, 'SCHEDULED'), -- Trip 3
(1, 1, 4, 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt', DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY), INTERVAL 7 HOUR), 300000, 34, 'SCHEDULED'), -- Trip 4
(2, 3, 5, 'Bến xe Miền Đông Mới', 'Bến xe Phía Nam Nha Trang', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 8 HOUR), 280000, 0, 'COMPLETED'), -- Trip 5
(2, 3, 1, 'Bến xe Miền Đông Mới', 'Bến xe Phía Nam Nha Trang', CONCAT(CURDATE(), ' 21:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 21:00:00'), INTERVAL 8 HOUR), 300000, 38, 'SCHEDULED'), -- Trip 6
(2, 4, 2, 'Bến xe Miền Đông Mới', 'Bến xe Phía Nam Nha Trang', DATE_ADD(CONCAT(CURDATE(), ' 07:30:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 07:30:00'), INTERVAL 1 DAY), INTERVAL 8 HOUR), 200000, 43, 'SCHEDULED'), -- Trip 7
(3, 5, 3, 'Bến xe Miền Đông', 'Bến xe Trung Tâm Đà Nẵng', CONCAT(CURDATE(), ' 13:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 13:00:00'), INTERVAL 20 HOUR), 550000, 38, 'SCHEDULED'), -- Trip 8
(3, 6, 4, 'Bến xe Miền Đông', 'Bến xe Trung Tâm Đà Nẵng', DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 1 DAY), INTERVAL 19 HOUR), 650000, 32, 'SCHEDULED'), -- Trip 9
(4, 7, 5, 'Bến xe Phía Nam Nha Trang', 'Bến xe Miền Đông', CONCAT(CURDATE(), ' 20:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 20:00:00'), INTERVAL 8 HOUR), 280000, 38, 'SCHEDULED'), -- Trip 10
(4, 8, 1, 'Bến xe Phía Nam Nha Trang', 'Bến xe Miền Đông', DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY), INTERVAL 8 HOUR), 200000, 43, 'SCHEDULED'), -- Trip 11
(5, 9, 2, 'Bến xe Phía Bắc Nha Trang', 'Bến xe Đà Lạt', CONCAT(CURDATE(), ' 07:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 07:00:00'), INTERVAL 4 HOUR), 150000, 27, 'SCHEDULED'), -- Trip 12
(5, 10, 3, 'Bến xe Phía Bắc Nha Trang', 'Bến xe Đà Lạt', CONCAT(CURDATE(), ' 13:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 13:00:00'), INTERVAL 4 HOUR), 180000, 34, 'SCHEDULED'), -- Trip 13
(6, 11, 4, 'Bến xe Phía Nam Nha Trang', 'Bến xe Đà Nẵng', DATE_ADD(CONCAT(CURDATE(), ' 19:00:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 19:00:00'), INTERVAL 1 DAY), INTERVAL 10 HOUR), 320000, 38, 'SCHEDULED'), -- Trip 14
(6, 12, 5, 'Bến xe Phía Nam Nha Trang', 'Bến xe Đà Nẵng', DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 2 DAY), INTERVAL 10 HOUR), 320000, 38, 'SCHEDULED'), -- Trip 15
(7, 13, 1, 'Bến xe Trung Tâm Đà Nẵng', 'Bến xe Miền Đông', DATE_ADD(CONCAT(CURDATE(), ' 05:00:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 05:00:00'), INTERVAL 1 DAY), INTERVAL 20 HOUR), 750000, 32, 'SCHEDULED'), -- Trip 16
(7, 14, 2, 'Bến xe Trung Tâm Đà Nẵng', 'Bến xe Miền Đông', DATE_ADD(CONCAT(CURDATE(), ' 14:00:00'), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 14:00:00'), INTERVAL 2 DAY), INTERVAL 20 HOUR), 600000, 38, 'SCHEDULED'), -- Trip 17
(8, 15, 3, 'Bến xe Đà Nẵng', 'Bến xe Đà Lạt', DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 2 DAY), INTERVAL 14 HOUR), 450000, 38, 'SCHEDULED'), -- Trip 18
(9, 17, 4, 'Bến xe Đà Nẵng', 'Bến xe Nha Trang', CONCAT(CURDATE(), ' 08:30:00'), DATE_ADD(CONCAT(CURDATE(), ' 08:30:00'), INTERVAL 10 HOUR), 250000, 43, 'SCHEDULED'), -- Trip 19
(9, 18, 5, 'Bến xe Đà Nẵng', 'Bến xe Nha Trang', CONCAT(CURDATE(), ' 20:30:00'), DATE_ADD(CONCAT(CURDATE(), ' 20:30:00'), INTERVAL 10 HOUR), 350000, 38, 'SCHEDULED'), -- Trip 20
(9, 15, 1, 'Bến xe Đà Nẵng', 'Bến xe Đà Lạt', CONCAT(CURDATE(), ' 18:45:00'), DATE_ADD(CONCAT(CURDATE(), ' 18:45:00'), INTERVAL 10 HOUR), 450000, 38, 'SCHEDULED'); -- Trip 21

-- =========================================
-- 5.b CURRENT & FUTURE TRIPS
-- Thêm vài chuyến diễn ra ngay bây giờ và trong tương lai để có dữ liệu "hiện tại/tương lai"
-- =========================================
INSERT INTO Trips (
    routeId, busId, driverId,
    departureStation, arrivalStation,
    departureTime, arrivalTime,
    price, availableSeats, tripStatus
) VALUES
-- Chuyến khởi hành ngay lúc chạy script
(1, 2, 1, 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt', NOW(), DATE_ADD(NOW(), INTERVAL 7 HOUR), 330000, 36, 'SCHEDULED'), -- Trip 22 (now)
-- Chuyến trong vài giờ tới
(2, 4, 2, 'Bến xe Miền Đông Mới', 'Bến xe Phía Nam Nha Trang', DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 HOUR), INTERVAL 8 HOUR), 310000, 40, 'SCHEDULED'), -- Trip 23 (future hours)
-- Chuyến vài ngày sau
(3, 5, 3, 'Bến xe Miền Đông', 'Bến xe Trung Tâm Đà Nẵng', DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 20 HOUR), 560000, 38, 'SCHEDULED'), -- Trip 24 (future days)
-- Chuyến bắt đầu trước 30 phút (đang diễn ra) nhưng vẫn để trạng thái SCHEDULED để test "present"
(4, 7, 4, 'Bến xe Phía Nam Nha Trang', 'Bến xe Miền Đông', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 30 MINUTE), INTERVAL 8 HOUR), 270000, 39, 'SCHEDULED'); -- Trip 25 (present)

-- =========================================
-- 6. SEATS (Tạo 2 ghế A01, A0ticket_seats2 cho mỗi chuyến xe)
-- =========================================
-- Tạo ghế cho mỗi chuyến dựa trên `capacity` của `Buses` (A01, A02, ...)
-- Dùng bảng tạm để tương thích tốt hơn với các phiên bản MySQL.
DROP TEMPORARY TABLE IF EXISTS SeatNumbers;
CREATE TEMPORARY TABLE SeatNumbers (n INT PRIMARY KEY);
INSERT INTO SeatNumbers (n) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),
(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),
(51),(52),(53),(54),(55),(56),(57),(58),(59),(60);

INSERT INTO Seats (tripId, seatCode)
SELECT t.tripId, CONCAT('A', LPAD(sn.n, 2, '0')) AS seatCode
FROM Trips t
JOIN Buses b ON t.busId = b.busId
JOIN SeatNumbers sn ON sn.n <= b.capacity
ORDER BY t.tripId, sn.n;


-- =========================================
-- 7. TICKETS & INVOICES (ĐÃ SỬA LỖI 1175 & 1064)
-- =========================================
SET @BOOKER_ID = 3;

DROP TEMPORARY TABLE IF EXISTS Temp_Ticket_Data;
CREATE TEMPORARY TABLE Temp_Ticket_Data (
    tempId INT AUTO_INCREMENT PRIMARY KEY,
    tripId INT,
    price INT,
    ticketStatus VARCHAR(10),
    paymentStatus VARCHAR(10),
    customerName VARCHAR(255),
    customerPhone VARCHAR(20),
    invoiceId INT NULL,
    ticketId INT NULL
);

-- BƯỚC 1: ĐIỀN DỮ LIỆU CẦN TẠO VÉ VÀ INVOICE
-- Tạo 3 vé cho mỗi chuyến SCHEDULED (2 BOOKED, 1 CANCELLED)
INSERT INTO Temp_Ticket_Data (tripId, price, ticketStatus, paymentStatus, customerName, customerPhone)
SELECT t.tripId, t.price, 'BOOKED', 'PAID', 'Demo Khách Hàng', '0909000333' FROM Trips t WHERE t.tripStatus = 'SCHEDULED' -- Vé 1/2 BOOKED
UNION ALL
SELECT t.tripId, t.price, 'BOOKED', 'PAID', 'Demo Khách Hàng', '0909000333' FROM Trips t WHERE t.tripStatus = 'SCHEDULED' -- Vé 2/2 BOOKED
UNION ALL
SELECT t.tripId, t.price, 'CANCELLED', 'CANCELLED', 'Demo Khách Hàng', '0909000000' FROM Trips t WHERE t.tripStatus = 'SCHEDULED' -- Vé CANCELLED
-- Tạo 2 vé cho mỗi chuyến COMPLETED (2 USED)
UNION ALL
SELECT t.tripId, t.price, 'USED', 'PAID', 'Demo Khách Hàng', '0909000000' FROM Trips t WHERE t.tripStatus = 'COMPLETED'
UNION ALL
SELECT t.tripId, t.price, 'USED', 'PAID', 'Demo Khách Hàng', '0909000000' FROM Trips t WHERE t.tripStatus = 'COMPLETED';


-- BƯỚC 2: TẠO INVOICES VÀ CẬP NHẬT invoiceId
SET @START_INVOICE_ID = 0;

-- 2.1. TẠO INVOICES
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) 
SELECT price, paymentStatus, NOW(), 'EWALLET'
FROM Temp_Ticket_Data
ORDER BY tempId; 

-- 2.2. Lấy ID của Invoice đầu tiên trong batch vừa insert
SET @START_INVOICE_ID = LAST_INSERT_ID();

-- 2.3. CẬP NHẬT invoiceId VÀO BẢNG TẠM BẰNG CÁCH TÍNH TOÁN DỰA TRÊN THỨ TỰ (tempId)
SET SQL_SAFE_UPDATES = 0; 
UPDATE Temp_Ticket_Data
SET invoiceId = @START_INVOICE_ID + tempId - 1;
SET SQL_SAFE_UPDATES = 1;


-- BƯỚC 3: TẠO TICKETS TỪ BẢNG TẠM VÀ LƯU LẠI ticketId
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus)
SELECT tripId, @BOOKER_ID, invoiceId, customerName, customerPhone, NULL, ticketStatus
FROM Temp_Ticket_Data
ORDER BY tempId; 

-- 3.1. Lấy lại ticketId VÀO BẢNG TẠM
SET @START_TICKET_ID = LAST_INSERT_ID();
SET SQL_SAFE_UPDATES = 0;
UPDATE Temp_Ticket_Data 
SET ticketId = @START_TICKET_ID + tempId - 1;
SET SQL_SAFE_UPDATES = 1;


-- =========================================
-- 8. TICKETSEATS (SỬA LỖI 1064 - Thay thế Window Function bằng biến)
-- =========================================
-- Khởi tạo biến để đếm số thứ tự ghế trong mỗi nhóm tripId + ticketStatus
SET @rank = 0;
SET @current_tripId = 0;
SET @current_ticketStatus = '';

INSERT INTO ticket_seats (ticketId, seatId)
SELECT 
    t.ticketId,
    s.seatId
FROM (
    -- Subquery để gán Rank (thứ tự 1, 2) cho từng vé trong mỗi chuyến
    SELECT 
        ttd.ticketId, 
        ttd.tripId,
        ttd.ticketStatus,
        @rank := IF(@current_tripId = ttd.tripId AND @current_ticketStatus = ttd.ticketStatus, @rank + 1, 1) AS seat_rank,
        @current_tripId := ttd.tripId,
        @current_ticketStatus := ttd.ticketStatus
    FROM Temp_Ticket_Data ttd
    WHERE ttd.ticketStatus IN ('BOOKED', 'USED')
    ORDER BY ttd.tripId, ttd.ticketStatus, ttd.tempId -- Order quan trọng để Rank chạy đúng
) AS t
-- JOIN với Seats để lấy seatId tương ứng (rank 1 -> A01, rank 2 -> A02)
INNER JOIN (
    SELECT 
        s.tripId,
        s.seatId,
        -- Gán thứ tự cho ghế (A01 là 1, A02 là 2)
        ROW_NUMBER() OVER (PARTITION BY s.tripId ORDER BY s.seatCode) AS seat_order 
    FROM Seats s
) AS s ON t.tripId = s.tripId AND t.seat_rank = s.seat_order
WHERE t.seat_rank <= 2; -- Chỉ lấy tối đa 2 ghế (A01, A02) cho mỗi nhóm

-- Xóa bảng tạm
DROP TEMPORARY TABLE Temp_Ticket_Data;

-- =========================================
-- 9. EXTRA SAMPLE INVOICES / TICKETS / TICKET_SEATS
-- Thêm vài vé mẫu rõ ràng để đảm bảo mọi bảng có dữ liệu minh họa
-- =========================================

-- Vé mẫu 1 (Trip 2) - 1 ghế A03
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES (320000, 'PAID', NOW(), 'CREDITCARD');
SET @INV1 = LAST_INSERT_ID();
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus) VALUES (2, 3, @INV1, 'Khách Mẫu 1', '0909000888', NULL, 'BOOKED');
SET @T1 = LAST_INSERT_ID();
INSERT INTO ticket_seats (ticketId, seatId)
SELECT @T1, s.seatId FROM Seats s WHERE s.tripId = 2 AND s.seatCode = 'A03' LIMIT 1;

-- Vé mẫu 2 (Trip 3) - 2 ghế A03, A04
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES (700000, 'PAID', NOW(), 'EWALLET');
SET @INV2 = LAST_INSERT_ID();
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus) VALUES (3, 4, @INV2, 'Khách Mẫu 2', '0909000999', NULL, 'BOOKED');
SET @T2 = LAST_INSERT_ID();
INSERT INTO ticket_seats (ticketId, seatId)
SELECT @T2, s.seatId FROM Seats s WHERE s.tripId = 3 AND s.seatCode IN ('A03','A04') ORDER BY s.seatCode LIMIT 2;

-- Vé mẫu 3 (Trip 6) - CANCELLED invoice
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES (300000, 'CANCELLED', NOW(), 'CASH');
SET @INV3 = LAST_INSERT_ID();
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus) VALUES (6, 5, @INV3, 'Khách Hủy', '0909111222', NULL, 'CANCELLED');

-- Vé mẫu 4 (Trip 8) - USED (completed trip)
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES (550000, 'PAID', NOW(), 'EWALLET');
SET @INV4 = LAST_INSERT_ID();
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus) VALUES (8, 2, @INV4, 'Khách Đã Đi', '0909222333', NULL, 'USED');
SET @T4 = LAST_INSERT_ID();
INSERT INTO ticket_seats (ticketId, seatId)
SELECT @T4, s.seatId FROM Seats s WHERE s.tripId = 8 AND s.seatCode = 'A03' LIMIT 1;

-- =========================================
-- 10. EXTENDED DEMO DATA
-- Them du lieu mau bo sung cho demo tim chuyen, lich su ve va dashboard.
-- Block nay dung bien theo email/bien so/ma GPLX de khong phu thuoc ID co dinh.
-- =========================================

INSERT INTO Users (
    fullName, email, password, phone, address, gender, dateOfBirth,
    role, provider, userStatus, enabled
) VALUES
('Quản Lý Vận Hành', 'manager@greenbus.vn', @BCRYPT_PASS, '0911000001', 'Thủ Đức, TP.HCM', 'MALE', '1987-04-12', 'MANAGER', 'LOCAL', 'ACTIVE', 1),
('Đỗ Thị Minh Anh', 'minhanh.demo@example.com', @BCRYPT_PASS, '0911000002', 'Cần Thơ', 'FEMALE', '1996-09-21', 'USER', 'LOCAL', 'ACTIVE', 1),
('Bùi Quốc Huy', 'quochuy.demo@example.com', @BCRYPT_PASS, '0911000003', 'Vũng Tàu', 'MALE', '1991-12-03', 'USER', 'LOCAL', 'ACTIVE', 1),
('Đặng Hoàng Nam', 'hoangnam.demo@example.com', @BCRYPT_PASS, '0911000004', 'Hà Nội', 'MALE', '1994-06-18', 'USER', 'LOCAL', 'ACTIVE', 1),
('Khách Bị Khóa', 'inactive.demo@example.com', @BCRYPT_PASS, '0911000005', 'Đà Nẵng', 'OTHER', '1999-03-30', 'USER', 'LOCAL', 'INACTIVE', 1);

INSERT INTO Routes (departureLocation, arrivalLocation, estimatedTime, routeStatus) VALUES
('TP. Hồ Chí Minh', 'TP. Cần Thơ', '03:30:00', 'ACTIVE'),
('TP. Cần Thơ', 'TP. Hồ Chí Minh', '03:30:00', 'ACTIVE'),
('TP. Hồ Chí Minh', 'TP. Vũng Tàu', '02:30:00', 'ACTIVE'),
('TP. Vũng Tàu', 'TP. Hồ Chí Minh', '02:30:00', 'ACTIVE'),
('TP. Đà Lạt', 'TP. Hồ Chí Minh', '06:00:00', 'ACTIVE'),
('Hà Nội', 'TP. Đà Nẵng', '14:00:00', 'ACTIVE');

SET @ROUTE_HCM_CANTHO = (SELECT routeId FROM Routes WHERE departureLocation = 'TP. Hồ Chí Minh' AND arrivalLocation = 'TP. Cần Thơ' ORDER BY routeId DESC LIMIT 1);
SET @ROUTE_CANTHO_HCM = (SELECT routeId FROM Routes WHERE departureLocation = 'TP. Cần Thơ' AND arrivalLocation = 'TP. Hồ Chí Minh' ORDER BY routeId DESC LIMIT 1);
SET @ROUTE_HCM_VUNGTAU = (SELECT routeId FROM Routes WHERE departureLocation = 'TP. Hồ Chí Minh' AND arrivalLocation = 'TP. Vũng Tàu' ORDER BY routeId DESC LIMIT 1);
SET @ROUTE_VUNGTAU_HCM = (SELECT routeId FROM Routes WHERE departureLocation = 'TP. Vũng Tàu' AND arrivalLocation = 'TP. Hồ Chí Minh' ORDER BY routeId DESC LIMIT 1);
SET @ROUTE_DALAT_HCM = (SELECT routeId FROM Routes WHERE departureLocation = 'TP. Đà Lạt' AND arrivalLocation = 'TP. Hồ Chí Minh' ORDER BY routeId DESC LIMIT 1);
SET @ROUTE_HANOI_DANANG = (SELECT routeId FROM Routes WHERE departureLocation = 'Hà Nội' AND arrivalLocation = 'TP. Đà Nẵng' ORDER BY routeId DESC LIMIT 1);

INSERT INTO Driver (name, licenseNumber, phone, address, driverStatus) VALUES
('Vũ Minh Đức', 'D-101010', '0912000001', 'Cần Thơ', 'ACTIVE'),
('Trần Bảo Long', 'D-202020', '0912000002', 'Vũng Tàu', 'ACTIVE'),
('Lê Thanh Phong', 'D-303030', '0912000003', 'Đà Lạt', 'ACTIVE'),
('Phạm Gia Bảo', 'D-404040', '0912000004', 'Hà Nội', 'INACTIVE');

SET @DRIVER_DUC = (SELECT driverId FROM Driver WHERE licenseNumber = 'D-101010');
SET @DRIVER_LONG = (SELECT driverId FROM Driver WHERE licenseNumber = 'D-202020');
SET @DRIVER_PHONG = (SELECT driverId FROM Driver WHERE licenseNumber = 'D-303030');
SET @DRIVER_BAO = (SELECT driverId FROM Driver WHERE licenseNumber = 'D-404040');

INSERT INTO Buses (routeId, licensePlate, busType, capacity, busStatus) VALUES
(@ROUTE_HCM_CANTHO, '65B-100.11', 'SEAT', 29, 'ACTIVE'),
(@ROUTE_CANTHO_HCM, '65B-200.22', 'SEAT', 29, 'ACTIVE'),
(@ROUTE_HCM_VUNGTAU, '72B-300.33', 'SEAT', 29, 'ACTIVE'),
(@ROUTE_VUNGTAU_HCM, '72B-400.44', 'SEAT', 29, 'ACTIVE'),
(@ROUTE_DALAT_HCM, '49B-500.55', 'BEDSEAT', 36, 'ACTIVE'),
(@ROUTE_HANOI_DANANG, '29B-600.66', 'BEDSEAT', 40, 'INACTIVE');

SET @BUS_HCM_CANTHO = (SELECT busId FROM Buses WHERE licensePlate = '65B-100.11');
SET @BUS_CANTHO_HCM = (SELECT busId FROM Buses WHERE licensePlate = '65B-200.22');
SET @BUS_HCM_VUNGTAU = (SELECT busId FROM Buses WHERE licensePlate = '72B-300.33');
SET @BUS_VUNGTAU_HCM = (SELECT busId FROM Buses WHERE licensePlate = '72B-400.44');
SET @BUS_DALAT_HCM = (SELECT busId FROM Buses WHERE licensePlate = '49B-500.55');
SET @BUS_HANOI_DANANG = (SELECT busId FROM Buses WHERE licensePlate = '29B-600.66');

INSERT INTO Trips (
    routeId, busId, driverId,
    departureStation, arrivalStation,
    departureTime, arrivalTime,
    price, availableSeats, tripStatus
) VALUES
(@ROUTE_HCM_CANTHO, @BUS_HCM_CANTHO, @DRIVER_DUC, 'Bến xe Miền Tây', 'Bến xe Trung tâm Cần Thơ', DATE_ADD(CONCAT(CURDATE(), ' 06:00:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 06:00:00'), INTERVAL 1 DAY), INTERVAL 4 HOUR), 180000, 27, 'SCHEDULED'),
(@ROUTE_HCM_CANTHO, @BUS_HCM_CANTHO, @DRIVER_DUC, 'Bến xe Miền Tây', 'Bến xe Trung tâm Cần Thơ', DATE_ADD(CONCAT(CURDATE(), ' 15:30:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 15:30:00'), INTERVAL 1 DAY), INTERVAL 4 HOUR), 190000, 29, 'SCHEDULED'),
(@ROUTE_CANTHO_HCM, @BUS_CANTHO_HCM, @DRIVER_DUC, 'Bến xe Trung tâm Cần Thơ', 'Bến xe Miền Tây', DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 2 DAY), INTERVAL 4 HOUR), 180000, 28, 'SCHEDULED'),
(@ROUTE_HCM_VUNGTAU, @BUS_HCM_VUNGTAU, @DRIVER_LONG, 'Văn phòng Quận 1', 'Bến xe Vũng Tàu', DATE_ADD(NOW(), INTERVAL 90 MINUTE), DATE_ADD(DATE_ADD(NOW(), INTERVAL 90 MINUTE), INTERVAL 3 HOUR), 120000, 25, 'SCHEDULED'),
(@ROUTE_HCM_VUNGTAU, @BUS_HCM_VUNGTAU, @DRIVER_LONG, 'Văn phòng Quận 1', 'Bến xe Vũng Tàu', DATE_ADD(CONCAT(CURDATE(), ' 20:00:00'), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 20:00:00'), INTERVAL 1 DAY), INTERVAL 3 HOUR), 130000, 29, 'SCHEDULED'),
(@ROUTE_VUNGTAU_HCM, @BUS_VUNGTAU_HCM, @DRIVER_LONG, 'Bến xe Vũng Tàu', 'Văn phòng Quận 1', DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 3 DAY), INTERVAL 3 HOUR), 120000, 29, 'SCHEDULED'),
(@ROUTE_DALAT_HCM, @BUS_DALAT_HCM, @DRIVER_PHONG, 'Bến xe Liên tỉnh Đà Lạt', 'Bến xe Miền Đông', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 7 HOUR), 320000, 0, 'COMPLETED'),
(@ROUTE_DALAT_HCM, @BUS_DALAT_HCM, @DRIVER_PHONG, 'Bến xe Liên tỉnh Đà Lạt', 'Bến xe Miền Đông', DATE_ADD(CONCAT(CURDATE(), ' 22:00:00'), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 22:00:00'), INTERVAL 2 DAY), INTERVAL 7 HOUR), 340000, 34, 'SCHEDULED'),
(@ROUTE_HANOI_DANANG, @BUS_HANOI_DANANG, @DRIVER_BAO, 'Bến xe Nước Ngầm', 'Bến xe Trung tâm Đà Nẵng', DATE_ADD(CONCAT(CURDATE(), ' 18:00:00'), INTERVAL 4 DAY), DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 18:00:00'), INTERVAL 4 DAY), INTERVAL 14 HOUR), 620000, 40, 'CANCELLED');

SET @EXT_TRIP_START = LAST_INSERT_ID();

INSERT INTO Seats (tripId, seatCode)
SELECT t.tripId, CONCAT('A', LPAD(sn.n, 2, '0')) AS seatCode
FROM Trips t
JOIN Buses b ON t.busId = b.busId
JOIN SeatNumbers sn ON sn.n <= b.capacity
WHERE t.tripId >= @EXT_TRIP_START
ORDER BY t.tripId, sn.n;

SET @USER_MINHANH = (SELECT userId FROM Users WHERE email = 'minhanh.demo@example.com');
SET @USER_QUOCHUY = (SELECT userId FROM Users WHERE email = 'quochuy.demo@example.com');
SET @USER_HOANGNAM = (SELECT userId FROM Users WHERE email = 'hoangnam.demo@example.com');

SET @TRIP_HCM_CANTHO_1 = @EXT_TRIP_START;
SET @TRIP_HCM_VUNGTAU_NOW = @EXT_TRIP_START + 3;
SET @TRIP_DALAT_HCM_DONE = @EXT_TRIP_START + 6;

INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES (360000, 'PAID', NOW(), 'EWALLET');
SET @EXT_INV1 = LAST_INSERT_ID();
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus)
VALUES (@TRIP_HCM_CANTHO_1, @USER_MINHANH, @EXT_INV1, 'Đỗ Thị Minh Anh', '0911000002', NULL, 'BOOKED');
SET @EXT_TICKET1 = LAST_INSERT_ID();
INSERT INTO ticket_seats (ticketId, seatId)
SELECT @EXT_TICKET1, s.seatId FROM Seats s WHERE s.tripId = @TRIP_HCM_CANTHO_1 AND s.seatCode IN ('A01', 'A02') ORDER BY s.seatCode;

INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES (120000, 'PENDING', NULL, 'CASH');
SET @EXT_INV2 = LAST_INSERT_ID();
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus)
VALUES (@TRIP_HCM_VUNGTAU_NOW, @USER_QUOCHUY, @EXT_INV2, 'Bùi Quốc Huy', '0911000003', NULL, 'BOOKED');
SET @EXT_TICKET2 = LAST_INSERT_ID();
INSERT INTO ticket_seats (ticketId, seatId)
SELECT @EXT_TICKET2, s.seatId FROM Seats s WHERE s.tripId = @TRIP_HCM_VUNGTAU_NOW AND s.seatCode = 'A05' LIMIT 1;

INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES (320000, 'PAID', DATE_SUB(NOW(), INTERVAL 2 DAY), 'CREDITCARD');
SET @EXT_INV3 = LAST_INSERT_ID();
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus)
VALUES (@TRIP_DALAT_HCM_DONE, @USER_HOANGNAM, @EXT_INV3, 'Đặng Hoàng Nam', '0911000004', NULL, 'USED');
SET @EXT_TICKET3 = LAST_INSERT_ID();
INSERT INTO ticket_seats (ticketId, seatId)
SELECT @EXT_TICKET3, s.seatId FROM Seats s WHERE s.tripId = @TRIP_DALAT_HCM_DONE AND s.seatCode = 'A01' LIMIT 1;
