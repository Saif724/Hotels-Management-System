
DROP DATABASE IF EXISTS hotel_db;
CREATE DATABASE hotel_db;
USE hotel_db;


CREATE TABLE login (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','EMPLOYEE','CUSTOMER') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(30),
    email VARCHAR(100) UNIQUE,
    username VARCHAR(50) UNIQUE,
    CONSTRAINT fk_customer_login FOREIGN KEY (username) REFERENCES login(username) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE employee (
    emp_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    salary DECIMAL(10,2) NOT NULL CHECK (salary > 0),
    paid_status ENUM('Paid','Unpaid') NOT NULL DEFAULT 'Unpaid',
    username VARCHAR(50) UNIQUE,
    CONSTRAINT fk_employee_login FOREIGN KEY (username) REFERENCES login(username) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE room (
    room_id INT PRIMARY KEY,
    room_type ENUM('Single','Double','Deluxe') NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    status ENUM('Available','Booked') NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB;

CREATE TABLE booking (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    room_id INT NOT NULL,
    checkin_date DATE NOT NULL,
    checkout_date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    status ENUM('Booked','CheckedIn','CheckedOut','Cancelled') NOT NULL DEFAULT 'Booked',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE,
    CHECK (checkin_date < checkout_date)
) ENGINE=InnoDB;

CREATE TABLE payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    method VARCHAR(50),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    comments TEXT,
    rating TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback_date DATE DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_feedback_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO room (room_id, room_type, price, status) VALUES
(101,'Single',2000.00,'Available'),
(102,'Single',2000.00,'Available'),
(103,'Single',2000.00,'Available'),
(104,'Single',2000.00,'Available'),
(105,'Single',2000.00,'Available'),
(106,'Single',2000.00,'Available'),
(107,'Single',2000.00,'Available'),
(108,'Single',2000.00,'Available'),
(109,'Single',2000.00,'Available'),
(110,'Single',2000.00,'Available'),

(201,'Double',3500.00,'Available'),
(202,'Double',3500.00,'Available'),
(203,'Double',3500.00,'Available'),
(204,'Double',3500.00,'Available'),
(205,'Double',3500.00,'Available'),

(301,'Deluxe',5000.00,'Available'),
(302,'Deluxe',5000.00,'Available'),
(303,'Deluxe',5000.00,'Available');

INSERT INTO login (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');

