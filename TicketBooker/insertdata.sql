USE ticketbooker;

-- Mật khẩu: '123456' đã được mã hóa BCrypt
SET @BCRYPT_PASS = '$2a$10$0JT96MJWgK9/0SquImmnDuV7r0T8.y.RMP27OtbHeC9dn1M1bYCMG';

SET @FUTURE_DATE = DATE_ADD(CURDATE(), INTERVAL 2 DAY);

INSERT INTO Users (fullName, email, password, phone, address, gender, dateOfBirth, role, provider, userStatus, enabled) VALUES 
-- Admin (Local Account)
('Quản Trị Hệ Thống', 'admin@greenbus.vn', @BCRYPT_PASS, '0901234567', 'Quận 1, TP.HCM', 'MALE', '1990-01-01', 'ADMIN', 'LOCAL', 'ACTIVE', 1),

-- Staff (Local Account)
('Trần Nhân Viên', 'staff@greenbus.vn', @BCRYPT_PASS, '0909000222', 'Quận 3, TP.HCM', 'FEMALE', '1995-05-05', 'STAFF', 'LOCAL', 'ACTIVE', 1),

-- Customer (Local Account)
('Lê Khách Hàng', 'customer@gmail.com', @BCRYPT_PASS, '0909000333', 'TP. Đà Lạt', 'FEMALE', '2000-10-10', 'USER', 'LOCAL', 'ACTIVE', 1), 

-- Google User (No Password, for OAuth2 test)
('Google User Test', 'googleuser@gmail.com', NULL, NULL, NULL, 'MALE', NULL, 'USER', 'GOOGLE', 'ACTIVE', 1);

-- 2. INSERT ROUTES
INSERT INTO Routes (departureLocation, arrivalLocation, estimatedTime, routeStatus) VALUES 
('TP. Hồ Chí Minh', 'TP. Đà Lạt', '06:00:00', 'ACTIVE'), -- ID 1
('TP. Hồ Chí Minh', 'TP. Nha Trang', '08:00:00', 'ACTIVE'), -- ID 2
('TP. Hồ Chí Minh', 'TP. Cần Thơ', '04:00:00', 'ACTIVE'); -- ID 3


-- 3. INSERT DRIVERS
INSERT INTO Driver (name, licenseNumber, phone, address, driverStatus) VALUES 
('Nguyễn Văn Tài', 'B2-001234', '0988888111', 'Bình Thạnh, HCM', 'ACTIVE'), -- ID 1
('Lê Văn Xế', 'D-005678', '0988888222', 'Thủ Đức, HCM', 'ACTIVE'); -- ID 2


-- 4. INSERT BUSES
INSERT INTO Buses (routeId, licensePlate, busType, capacity, busStatus) VALUES 
(1, '51B-123.45', 'BEDSEAT', 36, 'ACTIVE'), -- ID 1 (SG-DL)
(2, '79A-111.22', 'SEAT', 45, 'ACTIVE');    -- ID 2 (SG-NT)


-- 5. INSERT TRIPS (Trips are scheduled for the next few days)
INSERT INTO Trips (routeId, busId, driverId, departureStation, arrivalStation, departureTime, arrivalTime, price, availableSeats, tripStatus) VALUES 
(1, 1, 1, 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 6 HOUR), 350000, 36, 'SCHEDULED'),
(2, 2, 2, 'Bến xe Miền Tây', 'Bến xe Phía Nam Nha Trang', DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 8 HOUR), 250000, 45, 'SCHEDULED');


-- 6. INSERT SEATS (For TripID 1: SG -> ĐL - 36 seats)
-- Logic tạo 36 ghế (A01-A18, B01-B18)
INSERT INTO Seats (tripId, seatCode)
SELECT 1, CONCAT('A', LPAD(n, 2, '0')) FROM (SELECT @n := 0) vars JOIN (SELECT @n := @n + 1 AS n FROM information_schema.columns LIMIT 18) numbers
UNION ALL
SELECT 1, CONCAT('B', LPAD(n, 2, '0')) FROM (SELECT @n := 0) vars JOIN (SELECT @n := @n + 1 AS n FROM information_schema.columns LIMIT 18) numbers;

-- 7. INSERT INVOICE & TICKET (Demo một vé đã đặt)
INSERT INTO Invoices (invoiceId, totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES 
(1, 350000, 'PAID', NOW(), 'EWALLET');

INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, seatId, ticketStatus) VALUES 
(1, 3, 1, 'Lê Khách Hàng', '0909000333', 
    (SELECT seatId FROM Seats WHERE tripId = 1 AND seatCode = 'A01'), 'BOOKED');

-- 8. CẬP NHẬT CHỖ TRỐNG SAU KHI BOOK
UPDATE Trips SET availableSeats = availableSeats - 1 WHERE tripId = 1;