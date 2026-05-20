package com.cinema.database;

import com.cinema.datastructure.MyList;
import com.cinema.datastructure.ShowtimeLinkedList;
import com.cinema.model.*;

import java.sql.*;

public class DatabaseHelper {
    private static final String URL_SQL_AUTH = "jdbc:sqlserver://localhost;databaseName=QuanLiVeXemPhim;user=sa;password=123456;trustServerCertificate=true;";
    private static final String URL_INTEGRATED = "jdbc:sqlserver://localhost;databaseName=QuanLiVeXemPhim;integratedSecurity=true;trustServerCertificate=true;";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            // First try SQL Auth (common local credential found on this machine: sa / 123456)
            return DriverManager.getConnection(URL_SQL_AUTH);
        } catch (SQLException e) {
            try {
                // Fallback to integrated security if configured
                return DriverManager.getConnection(URL_INTEGRATED);
            } catch (SQLException ex) {
                throw new SQLException("Cannot connect to SQL Server database. Checked both SQL authentication (sa/123456) and Integrated Security. Error: " + ex.getMessage(), e);
            }
        }
    }

    // ----------------------------------------------------
    // MOVIE CRUD
    // ----------------------------------------------------

    public MyList<Movie> loadMovies() {
        MyList<Movie> movies = new MyList<>();
        String sql = "SELECT * FROM Movies ORDER BY title";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getString("poster_url"),
                        rs.getString("description")
                );
                movies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public int insertMovie(Movie movie) {
        String sql = "INSERT INTO Movies (title, genre, duration, poster_url, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getDuration());
            pstmt.setString(4, movie.getPosterUrl());
            pstmt.setString(5, movie.getDescription());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    movie.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateMovie(Movie movie) {
        String sql = "UPDATE Movies SET title = ?, genre = ?, duration = ?, poster_url = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getDuration());
            pstmt.setString(4, movie.getPosterUrl());
            pstmt.setString(5, movie.getDescription());
            pstmt.setInt(6, movie.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMovie(int movieId) {
        // Cascade delete showtimes associated with movie
        String selectShowtimes = "SELECT id FROM Showtimes WHERE movie_id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete associated tickets and seats first
                try (PreparedStatement pstmt = conn.prepareStatement(selectShowtimes)) {
                    pstmt.setInt(1, movieId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int showtimeId = rs.getInt("id");
                            deleteShowtimeInternal(conn, showtimeId);
                        }
                    }
                }
                // Delete showtimes
                String deleteShows = "DELETE FROM Showtimes WHERE movie_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteShows)) {
                    pstmt.setInt(1, movieId);
                    pstmt.executeUpdate();
                }
                // Delete movie
                String deleteMov = "DELETE FROM Movies WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteMov)) {
                    pstmt.setInt(1, movieId);
                    pstmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------------------------------
    // SHOWTIME CRUD
    // ----------------------------------------------------

    public ShowtimeLinkedList loadShowtimes(MyList<Movie> movies) {
        ShowtimeLinkedList list = new ShowtimeLinkedList();
        String sql = "SELECT s.*, " +
                     "       (SELECT COALESCE(COUNT(t.id), 0) FROM Tickets t WHERE t.showtime_id = s.id) as tickets_sold, " +
                     "       (SELECT COALESCE(SUM(t.price), 0.0) FROM Tickets t WHERE t.showtime_id = s.id) as revenue " +
                     "FROM Showtimes s ORDER BY s.show_date, s.show_time";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Showtime showtime = new Showtime(
                        rs.getInt("id"),
                        rs.getInt("movie_id"),
                        rs.getDate("show_date"),
                        rs.getTime("show_time"),
                        rs.getString("room")
                );
                showtime.setTicketsSold(rs.getInt("tickets_sold"));
                showtime.setRevenue(rs.getDouble("revenue"));

                // Attach Movie object
                for (Movie m : movies) {
                    if (m.getId() == showtime.getMovieId()) {
                        showtime.setMovie(m);
                        break;
                    }
                }
                list.insert(showtime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int insertShowtime(Showtime showtime) {
        String sql = "INSERT INTO Showtimes (movie_id, show_date, show_time, room) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, showtime.getMovieId());
                pstmt.setDate(2, showtime.getShowDate());
                pstmt.setTime(3, showtime.getShowTime());
                pstmt.setString(4, showtime.getRoom());
                pstmt.executeUpdate();

                int showtimeId = -1;
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        showtimeId = rs.getInt(1);
                        showtime.setId(showtimeId);
                    }
                }

                if (showtimeId != -1) {
                    // Generate 80 seats (Rows A to H, Cols 1 to 10)
                    String seatSql = "INSERT INTO Seats (showtime_id, seat_row, seat_col, status) VALUES (?, ?, ?, 'AVAILABLE')";
                    try (PreparedStatement seatPstmt = conn.prepareStatement(seatSql)) {
                        for (char r = 'A'; r <= 'H'; r++) {
                            for (int c = 1; c <= 10; c++) {
                                seatPstmt.setInt(1, showtimeId);
                                seatPstmt.setString(2, String.valueOf(r));
                                seatPstmt.setInt(3, c);
                                seatPstmt.addBatch();
                            }
                        }
                        seatPstmt.executeBatch();
                    }
                }

                conn.commit();
                return showtimeId;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean deleteShowtime(int showtimeId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                deleteShowtimeInternal(conn, showtimeId);
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteShowtimeInternal(Connection conn, int showtimeId) throws SQLException {
        // Delete tickets
        String deleteTickets = "DELETE FROM Tickets WHERE showtime_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteTickets)) {
            pstmt.setInt(1, showtimeId);
            pstmt.executeUpdate();
        }
        // Delete seats
        String deleteSeats = "DELETE FROM Seats WHERE showtime_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSeats)) {
            pstmt.setInt(1, showtimeId);
            pstmt.executeUpdate();
        }
        // Delete showtime
        String deleteShow = "DELETE FROM Showtimes WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteShow)) {
            pstmt.setInt(1, showtimeId);
            pstmt.executeUpdate();
        }
    }

    // Check if room is available for scheduling (no overlapping showtimes)
    // Overlap rule: start_time to start_time + duration + 30m cleanup
    public boolean isRoomAvailable(int movieDuration, Date date, Time time, String room, int excludeShowtimeId) {
        String sql = "SELECT s.show_time, m.duration " +
                     "FROM Showtimes s " +
                     "JOIN Movies m ON s.movie_id = m.id " +
                     "WHERE s.show_date = ? AND s.room = ? AND s.id != ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, date);
            pstmt.setString(2, room);
            pstmt.setInt(3, excludeShowtimeId);

            Time newStart = time;
            long newStartMs = newStart.getTime();
            long newEndMs = newStartMs + (movieDuration * 60L * 1000L) + (30L * 60L * 1000L); // duration + 30m clean

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Time extStart = rs.getTime("show_time");
                    int extDuration = rs.getInt("duration");
                    long extStartMs = extStart.getTime();
                    long extEndMs = extStartMs + (extDuration * 60L * 1000L) + (30L * 60L * 1000L);

                    // Check overlap
                    if (newStartMs < extEndMs && newEndMs > extStartMs) {
                        return false; // overlap found
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // ----------------------------------------------------
    // SEAT MANAGEMENT (CONCURRENCY LOCKING)
    // ----------------------------------------------------

    public MyList<Seat> loadSeats(int showtimeId) {
        MyList<Seat> seats = new MyList<>();
        String sql = "SELECT * FROM Seats WHERE showtime_id = ? ORDER BY seat_row, seat_col";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, showtimeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat(
                            rs.getInt("id"),
                            rs.getInt("showtime_id"),
                            rs.getString("seat_row"),
                            rs.getInt("seat_col"),
                            rs.getString("status")
                    );
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    // Try lock seat: Only allow lock if current status is AVAILABLE
    public boolean lockSeat(int seatId) {
        String sql = "UPDATE Seats SET status = 'LOCKED' WHERE id = ? AND status = 'AVAILABLE'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlockSeat(int seatId) {
        String sql = "UPDATE Seats SET status = 'AVAILABLE' WHERE id = ? AND status = 'LOCKED'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Clear all stale locks (e.g. locks created more than 5 minutes ago)
    // Since we don't have a lock_time in Seats table, we can just unlock them when requested or when they cancel.
    // To make it super robust, we can clear all LOCKED seats back to AVAILABLE when closing booking screens.
    public void unlockAllSeatsForShowtime(int showtimeId) {
        String sql = "UPDATE Seats SET status = 'AVAILABLE' WHERE showtime_id = ? AND status = 'LOCKED'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, showtimeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------
    // CUSTOMER CRUD
    // ----------------------------------------------------

    public int getOrInsertCustomer(Customer customer) {
        String querySql = "SELECT id, full_name, email FROM Customers WHERE phone = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(querySql)) {
            pstmt.setString(1, customer.getPhone());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    customer.setId(id);
                    // Update name or email if they are empty
                    if (rs.getString("full_name") == null || rs.getString("full_name").trim().isEmpty()) {
                        updateCustomerDetails(id, customer.getFullName(), customer.getEmail());
                    }
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert new
        String insertSql = "INSERT INTO Customers (full_name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    customer.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void updateCustomerDetails(int customerId, String name, String email) {
        String sql = "UPDATE Customers SET full_name = ?, email = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setInt(3, customerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------
    // TICKET / BOOKING FLOW
    // ----------------------------------------------------

    public boolean bookTicket(Ticket ticket) {
        String insertTicketSql = "INSERT INTO Tickets (customer_id, seat_id, showtime_id, price, booked_at) VALUES (?, ?, ?, ?, ?)";
        String updateSeatSql = "UPDATE Seats SET status = 'BOOKED' WHERE id = ? AND status = 'LOCKED'";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update Seat Status to BOOKED
                try (PreparedStatement pstmt = conn.prepareStatement(updateSeatSql)) {
                    pstmt.setInt(1, ticket.getSeatId());
                    int updated = pstmt.executeUpdate();
                    if (updated == 0) {
                        // Seat was not locked by us or already booked
                        conn.rollback();
                        return false;
                    }
                }

                // 2. Insert Ticket
                try (PreparedStatement pstmt = conn.prepareStatement(insertTicketSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, ticket.getCustomerId());
                    pstmt.setInt(2, ticket.getSeatId());
                    pstmt.setInt(3, ticket.getShowtimeId());
                    pstmt.setDouble(4, ticket.getPrice());
                    pstmt.setTimestamp(5, ticket.getBookedAt());
                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            ticket.setId(rs.getInt(1));
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MyList<Ticket> loadTicketHistory(String phone) {
        MyList<Ticket> history = new MyList<>();
        String sql = "SELECT t.*, c.full_name, c.phone, c.email, " +
                     "       s.show_date, s.show_time, s.room, s.movie_id, " +
                     "       se.seat_row, se.seat_col, m.title, m.genre, m.duration " +
                     "FROM Tickets t " +
                     "JOIN Customers c ON t.customer_id = c.id " +
                     "JOIN Showtimes s ON t.showtime_id = s.id " +
                     "JOIN Seats se ON t.seat_id = se.id " +
                     "JOIN Movies m ON s.movie_id = m.id " +
                     "WHERE c.phone = ? " +
                     "ORDER BY t.booked_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("full_name"),
                            rs.getString("phone"),
                            rs.getString("email")
                    );
                    Movie movie = new Movie(
                            rs.getInt("movie_id"),
                            rs.getString("title"),
                            rs.getString("genre"),
                            rs.getInt("duration"),
                            null,
                            null
                    );
                    Showtime showtime = new Showtime(
                            rs.getInt("showtime_id"),
                            rs.getInt("movie_id"),
                            rs.getDate("show_date"),
                            rs.getTime("show_time"),
                            rs.getString("room")
                    );
                    showtime.setMovie(movie);

                    Seat seat = new Seat(
                            rs.getInt("seat_id"),
                            rs.getInt("showtime_id"),
                            rs.getString("seat_row"),
                            rs.getInt("seat_col"),
                            "BOOKED"
                    );

                    Ticket ticket = new Ticket(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getInt("seat_id"),
                            rs.getInt("showtime_id"),
                            rs.getDouble("price"),
                            rs.getTimestamp("booked_at")
                    );
                    ticket.setCustomer(customer);
                    ticket.setShowtime(showtime);
                    ticket.setSeat(seat);

                    history.add(ticket);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
