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
-- 6. SEATS (Tạo 2 ghế A01, A0ticket_seats2 cho mỗi chuyến xe)
-- =========================================
INSERT INTO Seats (tripId, seatCode)
SELECT t.tripId, 'A01'
FROM Trips t 
UNION ALL
SELECT t.tripId, 'A02'
FROM Trips t;


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