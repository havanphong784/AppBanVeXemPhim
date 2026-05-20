package com.cinema.model;

import java.time.LocalDateTime;

public class Ticket {
    private int id, customerId, seatId, showtimeId;
    private double price;
    private LocalDateTime bookedAt;

    public Ticket() {}
    public Ticket(int id, int customerId, int seatId, int showtimeId, double price, LocalDateTime bookedAt) {
        this.id = id; this.customerId = customerId; this.seatId = seatId;
        this.showtimeId = showtimeId; this.price = price; this.bookedAt = bookedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
}
