-- PHẦN 1: CLEANUP VÀ KHỞI TẠO DATABASE
DROP DATABASE IF EXISTS ticketbooker;
CREATE DATABASE ticketbooker;
USE ticketbooker;

-- PHẦN 2: TẠO SCHEMA MỚI (ĐÃ GỘP BẢNG ACCOUNT VÀO USERS)

-- 1. Table: Users (Gộp Profile, Auth, và Security)
CREATE TABLE Users (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    fullName VARCHAR(255) NOT NULL,
    phone VARCHAR(15), 
    address VARCHAR(255),
    dateOfBirth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    profilePhoto BLOB,
    userStatus ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    
    -- CÁC TRƯỜNG TỪ BẢNG ACCOUNT CŨ
    email VARCHAR(100) UNIQUE,        
    password VARCHAR(100),            -- Lưu BCrypt Hash (có thể NULL cho tài khoản Google)
    role VARCHAR(20) DEFAULT 'USER',  -- ADMIN, STAFF, USER
    provider VARCHAR(50) DEFAULT 'LOCAL', -- LOCAL, GOOGLE, FACEBOOK
    enabled BIT(1) DEFAULT 1          
);

-- 2. Table: Routes
CREATE TABLE Routes (
    routeId INT AUTO_INCREMENT PRIMARY KEY,
    departureLocation VARCHAR(100) NOT NULL,
    arrivalLocation VARCHAR(100) NOT NULL,
    estimatedTime TIME,
    routeStatus ENUM('ACTIVE', 'INACTIVE') NOT NULL
);

-- 3. Table: Driver
CREATE TABLE Driver (
    driverId INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    licenseNumber VARCHAR(20) UNIQUE NOT NULL,
    phone VARCHAR(15) UNIQUE,
    address VARCHAR(255),
    driverStatus ENUM('ACTIVE', 'INACTIVE') NOT NULL
);

-- 4. Table: Buses
CREATE TABLE Buses (
    busId INT AUTO_INCREMENT PRIMARY KEY,
    routeId INT,
    licensePlate VARCHAR(20) NOT NULL UNIQUE,
    busType ENUM('BEDSEAT', 'SEAT') NOT NULL,
    capacity INT NOT NULL,
    busStatus ENUM('ACTIVE', 'INACTIVE') NOT NULL,
    FOREIGN KEY (routeId) REFERENCES Routes(routeId)
);

-- 5. Table: Invoices
CREATE TABLE Invoices (
    invoiceId INT AUTO_INCREMENT PRIMARY KEY,
    totalAmount INT,
    paymentStatus ENUM('PENDING', 'PAID') NOT NULL,
    paymentTime DATETIME,
    paymentMethod ENUM('CREDITCARD', 'EWALLET', 'CASH') NOT NULL
);

-- 6. Table: Trips
CREATE TABLE Trips (
    tripId INT AUTO_INCREMENT PRIMARY KEY,
    routeId INT NOT NULL,
    busId INT NOT NULL,
    driverId INT NOT NULL,
    departureStation VARCHAR(100) NOT NULL,
    arrivalStation VARCHAR(100) NOT NULL,
    departureTime DATETIME NOT NULL,
    arrivalTime DATETIME,
    price INT NOT NULL,
    availableSeats INT NOT NULL,
    tripStatus ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL,
    FOREIGN KEY (routeId) REFERENCES Routes(routeId),
    FOREIGN KEY (busId) REFERENCES Buses(busId),
    FOREIGN KEY (driverId) REFERENCES Driver(driverId)
);

-- 7. Table: Seats
CREATE TABLE Seats (
    seatId INT AUTO_INCREMENT PRIMARY KEY,
    tripId INT NOT NULL,
    seatCode VARCHAR(10) NOT NULL,
    FOREIGN KEY (tripId) REFERENCES Trips(tripId)
);

-- 8. Table: Tickets (bookerId trỏ về Users)
CREATE TABLE Tickets (
    ticketId INT AUTO_INCREMENT PRIMARY KEY,
    tripId INT NOT NULL,
    bookerId INT NOT NULL, 
    invoiceId INT,
    customerName VARCHAR(100) NOT NULL,
    customerPhone VARCHAR(15) NOT NULL,
    seatId INT NOT NULL,
    qrCode VARCHAR(255),
    ticketStatus ENUM('BOOKED', 'CANCELLED', 'USED') NOT NULL,
    FOREIGN KEY (tripId) REFERENCES Trips(tripId),
    FOREIGN KEY (bookerId) REFERENCES Users(userId), 
    FOREIGN KEY (invoiceId) REFERENCES Invoices(invoiceId),
    FOREIGN KEY (seatId) REFERENCES Seats(seatId)
);