package com.cinema.dao;

import com.cinema.database.ConnectDB;
import com.cinema.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CinemaDAO {
    private Connection getConn() throws SQLException {
        return ConnectDB.getInstance().getConnection();
    }

    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> list = new ArrayList<>();
        ResultSet rs = getConn().createStatement().executeQuery("SELECT * FROM Movies");
        while (rs.next()) {
            list.add(new Movie(rs.getInt("id"), rs.getString("title"), rs.getString("genre"),
                    rs.getInt("duration"), rs.getString("poster_url"), rs.getString("description")));
        }
        return list;
    }

    public List<Showtime> getShowtimesByMovie(int movieId) throws SQLException {
        PreparedStatement ps = getConn().prepareStatement("SELECT * FROM Showtimes WHERE movie_id = ?");
        ps.setInt(1, movieId);
        ResultSet rs = ps.executeQuery();
        List<Showtime> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Showtime(rs.getInt("id"), rs.getInt("movie_id"),
                    rs.getDate("show_date").toLocalDate(), rs.getTime("show_time").toLocalTime(), rs.getString("room")));
        }
        return list;
    }

    public List<Seat> getSeatsByShowtime(int showtimeId) throws SQLException {
        PreparedStatement ps = getConn().prepareStatement("SELECT * FROM Seats WHERE showtime_id = ? ORDER BY seat_row, seat_col");
        ps.setInt(1, showtimeId);
        ResultSet rs = ps.executeQuery();
        List<Seat> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Seat(rs.getInt("id"), rs.getInt("showtime_id"),
                    rs.getString("seat_row").charAt(0), rs.getInt("seat_col"), rs.getString("status")));
        }
        return list;
    }

    public void bookSeats(List<Integer> seatIds, String customerName, String phone, int showtimeId, double price) throws SQLException {
        Connection conn = getConn();
        conn.setAutoCommit(false);
        try {
            // Tạo customer
            PreparedStatement psC = conn.prepareStatement("INSERT INTO Customers (full_name, phone) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            psC.setString(1, customerName);
            psC.setString(2, phone);
            psC.executeUpdate();
            ResultSet keys = psC.getGeneratedKeys();
            keys.next();
            int customerId = keys.getInt(1);

            // Đặt vé và cập nhật ghế
            for (int seatId : seatIds) {
                PreparedStatement psS = conn.prepareStatement("UPDATE Seats SET status = 'BOOKED' WHERE id = ? AND status = 'AVAILABLE'");
                psS.setInt(1, seatId);
                int updated = psS.executeUpdate();
                if (updated == 0) throw new SQLException("Ghế đã được đặt bởi người khác!");

                PreparedStatement psT = conn.prepareStatement("INSERT INTO Tickets (customer_id, seat_id, showtime_id, price) VALUES (?,?,?,?)");
                psT.setInt(1, customerId);
                psT.setInt(2, seatId);
                psT.setInt(3, showtimeId);
                psT.setDouble(4, price);
                psT.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
