use TICKETBOOKER;
-- 1. Tạo Users (Admin, Staff, Customer)
INSERT INTO Users (fullName, phone, address, dateOfBirth, gender, userStatus) VALUES 
('Nguyễn Quản Trị', '0909000111', '123 Quận 1, TP.HCM', '1990-01-01', 'MALE', 'ACTIVE'),
('Trần Nhân Viên', '0909000222', '456 Quận 3, TP.HCM', '1995-05-05', 'FEMALE', 'ACTIVE'),
('Lê Khách Hàng', '0909000333', '789 Đà Lạt', '2000-10-10', 'MALE', 'ACTIVE');

-- 2. Tạo Accounts
INSERT INTO Account (userId, username, password, email, role, accountStatus) VALUES 
(1, 'admin', 'hash_password_123', 'admin@bus.com', 'MANAGER', 'ACTIVE'),
(2, 'staff01', 'hash_password_123', 'staff@bus.com', 'STAFF', 'ACTIVE'),
(3, 'customer01', 'hash_password_123', 'customer@bus.com', 'CUSTOMER', 'ACTIVE');

-- 3. Tạo Routes (Tuyến đường)
INSERT INTO Routes (departureLocation, arrivalLocation, estimatedTime, routeStatus) VALUES 
('TP. Hồ Chí Minh', 'Đà Lạt', '06:00:00', 'ACTIVE'),
('Đà Lạt', 'TP. Hồ Chí Minh', '06:00:00', 'ACTIVE'),
('TP. Hồ Chí Minh', 'Nha Trang', '08:00:00', 'ACTIVE');

-- 4. Tạo Buses (Xe)
-- Lưu ý: Trong schema của bạn, Bus gắn liền với RouteId
INSERT INTO Buses (routeId, licensePlate, busType, capacity, busStatus) VALUES 
(1, '51B-123.45', 'BEDSEAT', 40, 'ACTIVE'), -- Xe đi Đà Lạt
(1, '51B-678.90', 'BEDSEAT', 40, 'ACTIVE'), -- Xe đi Đà Lạt
(3, '79B-111.22', 'SEAT', 30, 'ACTIVE');    -- Xe đi Nha Trang

-- 5. Tạo Driver (Tài xế)
INSERT INTO Driver (name, licenseNumber, phone, driverStatus) VALUES 
('Phạm Bác Tài', 'D123456789', '0912345678', 'ACTIVE'),
('Võ Lái Xe', 'D987654321', '0987654321', 'ACTIVE');

-- 6. Tạo Trips (Chuyến đi cụ thể)
-- Tạo một chuyến đi Đà Lạt vào ngày mai
INSERT INTO Trips (routeId, busId, driverId, departureStation, arrivalStation, departureTime, arrivalTime, price, availableSeats, tripStatus) VALUES 
(1, 1, 1, 'Bến xe Miền Đông', 'Bến xe Liên Tỉnh Đà Lạt', '2023-12-25 22:00:00', '2023-12-26 04:00:00', 350000, 40, 'SCHEDULED');

-- 7. Tạo Seats (Ghế cho chuyến đi vừa tạo - TripId = 1)
-- Giả sử xe có 40 chỗ, ta tạo vài ghế ví dụ
INSERT INTO Seats (tripId, seatCode) VALUES 
(1, 'A01'), (1, 'A02'), (1, 'A03'), (1, 'A04'), (1, 'B01'), (1, 'B02');

-- 8. Tạo Invoices (Hóa đơn)
INSERT INTO Invoices (totalAmount, paymentStatus, paymentTime, paymentMethod) VALUES 
(350000, 'PAID', '2023-12-20 10:00:00', 'EWALLET');

-- 9. Tạo Tickets (Vé đã đặt)
-- Khách hàng (accountId=3) đặt ghế A01 cho chuyến đi TripId=1
INSERT INTO Tickets (tripId, bookerId, invoiceId, customerName, customerPhone, seatId, qrCode, ticketStatus) VALUES 
(1, 3, 1, 'Lê Khách Hàng', '0909000333', 1, 'QR_DATA_ENCRYPTED_123', 'BOOKED');

-- Cập nhật lại số ghế trống (Logic Backend sẽ làm việc này)
UPDATE Trips SET availableSeats = availableSeats - 1 WHERE tripId = 1;