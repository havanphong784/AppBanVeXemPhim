-- Database: QuanLiVeXemPhim
CREATE DATABASE QuanLiVeXemPhim;
GO
USE QuanLiVeXemPhim;
GO

CREATE TABLE Movies (
    id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(200) NOT NULL,
    genre NVARCHAR(100),
    duration INT, -- phút
    poster_url NVARCHAR(500),
    description NVARCHAR(MAX)
);

CREATE TABLE Showtimes (
    id INT IDENTITY(1,1) PRIMARY KEY,
    movie_id INT FOREIGN KEY REFERENCES Movies(id),
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    room NVARCHAR(50) NOT NULL
);

CREATE TABLE Customers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(15),
    email NVARCHAR(100)
);

CREATE TABLE Seats (
    id INT IDENTITY(1,1) PRIMARY KEY,
    showtime_id INT FOREIGN KEY REFERENCES Showtimes(id),
    seat_row CHAR(1) NOT NULL, -- A, B, C...
    seat_col INT NOT NULL,
    status NVARCHAR(20) DEFAULT 'AVAILABLE' -- AVAILABLE, SELECTED, BOOKED
);

CREATE TABLE Tickets (
    id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT FOREIGN KEY REFERENCES Customers(id),
    seat_id INT FOREIGN KEY REFERENCES Seats(id),
    showtime_id INT FOREIGN KEY REFERENCES Showtimes(id),
    price DECIMAL(10,2) NOT NULL,
    booked_at DATETIME DEFAULT GETDATE()
);

-- Sample data
INSERT INTO Movies (title, genre, duration, description) VALUES
(N'Avengers: Endgame', N'Hành động', 181, N'Trận chiến cuối cùng của biệt đội siêu anh hùng'),
(N'Spirited Away', N'Hoạt hình', 125, N'Cuộc phiêu lưu kỳ diệu của Chihiro'),
(N'Parasite', N'Tâm lý', 132, N'Câu chuyện về hai gia đình khác biệt giai cấp');

INSERT INTO Showtimes (movie_id, show_date, show_time, room) VALUES
(1, '2026-05-20', '19:00', N'Phòng 1'),
(2, '2026-05-20', '20:30', N'Phòng 2'),
(3, '2026-05-21', '18:00', N'Phòng 1');

-- Tạo ghế cho showtime 1 (8 hàng x 10 cột)
DECLARE @row CHAR(1), @col INT, @showtime INT = 1;
DECLARE @rows TABLE (r CHAR(1));
INSERT INTO @rows VALUES ('A'),('B'),('C'),('D'),('E'),('F'),('G'),('H');

DECLARE row_cursor CURSOR FOR SELECT r FROM @rows;
OPEN row_cursor;
FETCH NEXT FROM row_cursor INTO @row;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @col = 1;
    WHILE @col <= 10
    BEGIN
        INSERT INTO Seats (showtime_id, seat_row, seat_col, status) VALUES (@showtime, @row, @col, 'AVAILABLE');
        SET @col = @col + 1;
    END
    FETCH NEXT FROM row_cursor INTO @row;
END
CLOSE row_cursor;
DEALLOCATE row_cursor;
GO
