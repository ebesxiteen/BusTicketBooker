USE ticketbooker;
-- =========================================
-- 1. USERS
-- =========================================
SET @BCRYPT_PASS = '$2a$10$0JT96MJWgK9/0SquImmnDuV7r0T8.y.RMP27OtbHeC9dn1M1bYCMG';

INSERT INTO Users (fullName, email, password, phone, address, gender, dateOfBirth, role, provider, userStatus, enabled) VALUES 
('Quản Trị Hệ Thống', 'admin@greenbus.vn', @BCRYPT_PASS, '0901234567', 'Quận 1, TP.HCM', 'MALE', '1990-01-01', 'ADMIN', 'LOCAL', 'ACTIVE', 1),
('Trần Nhân Viên', 'staff@greenbus.vn', @BCRYPT_PASS, '0909000222', 'Quận 3, TP.HCM', 'FEMALE', '1995-05-05', 'STAFF', 'LOCAL', 'ACTIVE', 1),
('Lê Khách Hàng', 'customer@gmail.com', @BCRYPT_PASS, '0909000333', 'TP. Đà Lạt', 'FEMALE', '2000-10-10', 'USER', 'LOCAL', 'ACTIVE', 1),
('Google User Test', 'googleuser@gmail.com', NULL, NULL, NULL, 'MALE', NULL, 'USER', 'GOOGLE', 'ACTIVE', 1);

-- =========================================
-- 2. ROUTES
-- =========================================
INSERT INTO Routes (departureLocation, arrivalLocation, estimatedTime, routeStatus) VALUES 
('TP. Hồ Chí Minh', 'TP. Đà Lạt', '06:00:00', 'ACTIVE'), -- ID 1
('TP. Hồ Chí Minh', 'TP. Nha Trang', '08:00:00', 'ACTIVE'), -- ID 2
('TP. Hồ Chí Minh', 'TP. Đà Nẵng', '12:00:00', 'ACTIVE'), -- ID 3
('TP. Nha Trang', 'TP. Hồ Chí Minh', '08:00:00', 'ACTIVE'), -- ID 4
('TP. Nha Trang', 'TP. Đà Lạt', '06:00:00', 'ACTIVE'), -- ID 5
('TP. Nha Trang', 'TP. Đà Nẵng', '04:00:00', 'ACTIVE'), -- ID 6
('TP. Đà Nẵng', 'TP. Hồ Chí Minh', '12:00:00', 'ACTIVE'), -- ID 7
('TP. Đà Nẵng', 'TP. Đà Lạt', '06:00:00', 'ACTIVE'), -- ID 8
('TP. Đà Nẵng', 'TP. Nha Trang', '04:00:00', 'ACTIVE'); -- ID 9

-- =========================================
-- 3. DRIVERS
-- =========================================
INSERT INTO Driver (name, licenseNumber, phone, address, driverStatus) VALUES 
('Nguyễn Văn Tài', 'B2-001234', '0988888111', 'Bình Thạnh, HCM', 'ACTIVE'), -- ID 1
('Lê Văn Xế', 'D-005678', '0988888222', 'Thủ Đức, HCM', 'ACTIVE'); -- ID 2

-- =========================================
-- 4. BUSES
-- =========================================
INSERT INTO Buses (routeId, licensePlate, busType, capacity, busStatus) VALUES 
-- Route 1: HCM -> Đà Lạt
(1, '51B-123.45', 'BEDSEAT', 36, 'ACTIVE'), -- busId 1
(1, '51B-333.44', 'BEDSEAT', 40, 'ACTIVE'), -- busId 2
-- Route 2: HCM -> Nha Trang
(2, '79B-111.22', 'BEDSEAT', 40, 'ACTIVE'), -- busId 3
(2, '51B-999.88', 'SEAT',    45, 'ACTIVE'), -- busId 4
-- Route 3: HCM -> Đà Nẵng
(3, '43B-001.23', 'BEDSEAT', 40, 'ACTIVE'), -- busId 5
(3, '51B-777.66', 'BEDSEAT', 34, 'ACTIVE'), -- busId 6
-- Route 4: Nha Trang -> HCM
(4, '79B-222.33', 'BEDSEAT', 40, 'ACTIVE'), -- busId 7
(4, '79B-444.55', 'SEAT',    45, 'ACTIVE'), -- busId 8
-- Route 5: Nha Trang -> Đà Lạt
(5, '79B-555.66', 'SEAT',    29, 'ACTIVE'), -- busId 9
(5, '49B-121.21', 'BEDSEAT', 36, 'ACTIVE'), -- busId 10
-- Route 6: Nha Trang -> Đà Nẵng
(6, '79B-888.99', 'BEDSEAT', 40, 'ACTIVE'), -- busId 11
(6, '43B-456.78', 'BEDSEAT', 40, 'ACTIVE'), -- busId 12
-- Route 7: Đà Nẵng -> HCM
(7, '43B-654.32', 'BEDSEAT', 34, 'ACTIVE'), -- busId 13
(7, '43B-112.23', 'BEDSEAT', 40, 'ACTIVE'), -- busId 14
-- Route 8: Đà Nẵng -> Đà Lạt
(8, '43B-987.65', 'BEDSEAT', 40, 'ACTIVE'), -- busId 15
(8, '49B-567.12', 'BEDSEAT', 36, 'ACTIVE'), -- busId 16
-- Route 9: Đà Nẵng -> Nha Trang
(9, '43B-333.22', 'SEAT',    45, 'ACTIVE'), -- busId 17
(9, '79B-777.11', 'BEDSEAT', 40, 'ACTIVE'); -- busId 18

-- =========================================
-- 5. TRIPS
--  - Trip COMPLETED: chỉ để demo lịch sử.
--  - Trip SCHEDULED: availableSeats = capacity (sẽ trừ sau vì ticket demo).
-- =========================================

INSERT INTO Trips (routeId, busId, driverId, departureStation, arrivalStation, departureTime, arrivalTime, price, availableSeats, tripStatus) VALUES 

-- Route 1: HCM -> Đà Lạt
-- Hôm qua (COMPLETED) - bus 1 (36 chỗ)
(1, 1, 1,
 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt',
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 7 HOUR),
 300000, 0, 'COMPLETED'),

-- Sáng nay 08:00 - bus 1 (36 chỗ)
(1, 1, 2,
 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt',
 CONCAT(CURDATE(), ' 08:00:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 7 HOUR),
 320000, 36, 'SCHEDULED'),

-- Tối nay 22:30 - bus 2 (40 chỗ)
(1, 2, 1,
 'Văn phòng Quận 1', 'Bến xe Liên Tỉnh Đà Lạt',
 CONCAT(CURDATE(), ' 22:30:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 22:30:00'), INTERVAL 6 HOUR),
 350000, 40, 'SCHEDULED'),

-- Ngày mai 10:00 - bus 1 (36 chỗ)
(1, 1, 2,
 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt',
 DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY), INTERVAL 7 HOUR),
 300000, 36, 'SCHEDULED'),

-- Route 2: HCM -> Nha Trang
-- Hôm qua (COMPLETED) - bus 3 (40 chỗ)
(2, 3, 2,
 'Bến xe Miền Đông Mới', 'Bến xe Phía Nam Nha Trang',
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 8 HOUR),
 280000, 0, 'COMPLETED'),

-- Tối nay 21:00 - bus 3 (40 chỗ)
(2, 3, 1,
 'Bến xe Miền Đông Mới', 'Bến xe Phía Nam Nha Trang',
 CONCAT(CURDATE(), ' 21:00:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 21:00:00'), INTERVAL 8 HOUR),
 300000, 40, 'SCHEDULED'),

-- Ngày mai 07:30 - bus 4 (45 chỗ)
(2, 4, 2,
 'Bến xe Miền Đông Mới', 'Bến xe Phía Nam Nha Trang',
 DATE_ADD(CONCAT(CURDATE(), ' 07:30:00'), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 07:30:00'), INTERVAL 1 DAY), INTERVAL 8 HOUR),
 200000, 45, 'SCHEDULED'),

-- Route 3: HCM -> Đà Nẵng
-- Hôm nay 13:00 - bus 5 (40 chỗ)
(3, 5, 1,
 'Bến xe Miền Đông', 'Bến xe Trung Tâm Đà Nẵng',
 CONCAT(CURDATE(), ' 13:00:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 13:00:00'), INTERVAL 20 HOUR),
 550000, 40, 'SCHEDULED'),

-- Ngày mai 16:00 - bus 6 (34 chỗ)
(3, 6, 2,
 'Bến xe Miền Đông', 'Bến xe Trung Tâm Đà Nẵng',
 DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 1 DAY), INTERVAL 19 HOUR),
 650000, 34, 'SCHEDULED'),

-- Route 4: Nha Trang -> HCM
-- Tối nay 20:00 - bus 7 (40 chỗ)
(4, 7, 1,
 'Bến xe Phía Nam Nha Trang', 'Bến xe Miền Đông',
 CONCAT(CURDATE(), ' 20:00:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 20:00:00'), INTERVAL 8 HOUR),
 280000, 40, 'SCHEDULED'),

-- Ngày mai 09:00 - bus 8 (45 chỗ)
(4, 8, 2,
 'Bến xe Phía Nam Nha Trang', 'Bến xe Miền Đông',
 DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY), INTERVAL 8 HOUR),
 200000, 45, 'SCHEDULED'),

-- Route 5: Nha Trang -> Đà Lạt
-- Sáng nay 07:00 - bus 9 (29 chỗ)
(5, 9, 1,
 'Bến xe Phía Bắc Nha Trang', 'Bến xe Đà Lạt',
 CONCAT(CURDATE(), ' 07:00:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 07:00:00'), INTERVAL 4 HOUR),
 150000, 29, 'SCHEDULED'),

-- Chiều nay 13:00 - bus 10 (36 chỗ)
(5, 10, 2,
 'Bến xe Phía Bắc Nha Trang', 'Bến xe Đà Lạt',
 CONCAT(CURDATE(), ' 13:00:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 13:00:00'), INTERVAL 4 HOUR),
 180000, 36, 'SCHEDULED'),

-- Route 6: Nha Trang -> Đà Nẵng
-- Tối mai 19:00 - bus 11 (40 chỗ)
(6, 11, 1,
 'Bến xe Phía Nam Nha Trang', 'Bến xe Đà Nẵng',
 DATE_ADD(CONCAT(CURDATE(), ' 19:00:00'), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 19:00:00'), INTERVAL 1 DAY), INTERVAL 10 HOUR),
 320000, 40, 'SCHEDULED'),

-- Ngày kia 08:00 - bus 12 (40 chỗ)
(6, 12, 2,
 'Bến xe Phía Nam Nha Trang', 'Bến xe Đà Nẵng',
 DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 2 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 2 DAY), INTERVAL 10 HOUR),
 320000, 40, 'SCHEDULED'),

-- Route 7: Đà Nẵng -> HCM
-- Sáng mai 05:00 - bus 13 (34 chỗ)
(7, 13, 2,
 'Bến xe Trung Tâm Đà Nẵng', 'Bến xe Miền Đông',
 DATE_ADD(CONCAT(CURDATE(), ' 05:00:00'), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 05:00:00'), INTERVAL 1 DAY), INTERVAL 20 HOUR),
 750000, 34, 'SCHEDULED'),

-- Chiều ngày kia 14:00 - bus 14 (40 chỗ)
(7, 14, 1,
 'Bến xe Trung Tâm Đà Nẵng', 'Bến xe Miền Đông',
 DATE_ADD(CONCAT(CURDATE(), ' 14:00:00'), INTERVAL 2 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 14:00:00'), INTERVAL 2 DAY), INTERVAL 20 HOUR),
 600000, 40, 'SCHEDULED'),

-- Route 8: Đà Nẵng -> Đà Lạt
-- Chiều ngày kia 16:00 - bus 15 (40 chỗ)
(8, 15, 1,
 'Bến xe Đà Nẵng', 'Bến xe Đà Lạt',
 DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 2 DAY),
 DATE_ADD(DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 2 DAY), INTERVAL 14 HOUR),
 450000, 40, 'SCHEDULED'),

-- Route 9: Đà Nẵng -> Nha Trang
-- Sáng nay 08:30 - bus 17 (45 chỗ)
(9, 17, 2,
 'Bến xe Đà Nẵng', 'Bến xe Nha Trang',
 CONCAT(CURDATE(), ' 08:30:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 08:30:00'), INTERVAL 10 HOUR),
 250000, 45, 'SCHEDULED'),

-- Tối nay 20:30 - bus 18 (40 chỗ)
(9, 18, 1,
 'Bến xe Đà Nẵng', 'Bến xe Nha Trang',
 CONCAT(CURDATE(), ' 20:30:00'),
 DATE_ADD(CONCAT(CURDATE(), ' 20:30:00'), INTERVAL 10 HOUR),
 350000, 40, 'SCHEDULED');

USE ticketbooker;

-- =========================================
-- 6. INVOICE DEMO
-- =========================================
-- BOOKED
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod)
SELECT 300000, 'PAID', NOW(), 'EWALLET'
FROM Trips t
WHERE t.tripStatus = 'SCHEDULED';

-- COMPLETED
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod)
SELECT 300000, 'PAID', NOW(), 'EWALLET'
FROM Trips t
WHERE t.tripStatus = 'COMPLETED';


-- =========================================
-- 7. SEATS cho TRIP SCHEDULED
--  - Mỗi trip SCHEDULED có hai ghế A01, A02 để demo
-- =========================================
INSERT INTO Seats (tripId, seatCode)
SELECT t.tripId, 'A01'
FROM Trips t WHERE t.tripStatus = 'SCHEDULED'
UNION ALL
SELECT t.tripId, 'A02'
FROM Trips t WHERE t.tripStatus = 'SCHEDULED';

-- =========================================
-- 8. TICKETS
--  (Lưu ý: bảng Tickets KHÔNG còn seatId)
-- =========================================

-- Vé BOOKED
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus)
SELECT t.tripId, 3, ROW_NUMBER() OVER (ORDER BY t.tripId),
       'Demo User', '0909000333', NULL, 'BOOKED'
FROM Trips t
WHERE t.tripStatus = 'SCHEDULED';

-- Vé COMPLETED
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus)
SELECT t.tripId, 3, ROW_NUMBER() OVER (ORDER BY t.tripId) + 
       (SELECT COUNT(*) FROM Trips WHERE tripStatus = 'SCHEDULED'),
       'Demo User', '0909000000', NULL, 'USED'
FROM Trips t
WHERE t.tripStatus = 'COMPLETED';

-- Vé CANCELLED (không gắn invoice)
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, qrCode, ticketStatus)
SELECT t.tripId, 3, NULL,
       'Demo User', '0909000000', NULL, 'CANCELLED'
FROM Trips t
WHERE t.tripStatus = 'SCHEDULED';


-- =========================================
-- 9. TICKETSEATS
--  - Gắn 2 ghế (A01, A02) cho mỗi vé BOOKED tương ứng theo tripId
--  - Mỗi seatId chỉ xuất hiện đúng 1 lần (1 ghế chỉ thuộc 1 vé)
-- =========================================

-- Giả sử bảng TicketSeats đã được tạo như sau:
-- CREATE TABLE TicketSeats (
--   ticketId INT NOT NULL,
--   seatId INT NOT NULL,
--   PRIMARY KEY (ticketId, seatId),
--   FOREIGN KEY (ticketId) REFERENCES Tickets(ticketId) ON DELETE CASCADE,
--   FOREIGN KEY (seatId) REFERENCES Seats(seatId) ON DELETE CASCADE,
--   CONSTRAINT uq_ticketseat_seat UNIQUE (seatId)
-- );

INSERT INTO ticket_seats (ticket_id, seat_id)
SELECT tk.ticketId, s.seatId
FROM Tickets tk
JOIN Trips t ON tk.tripId = t.tripId
JOIN Seats s ON s.tripId = t.tripId
WHERE t.tripStatus = 'SCHEDULED'
  AND tk.ticketStatus = 'BOOKED';

