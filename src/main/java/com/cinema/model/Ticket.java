package com.cinema.model;

import java.sql.Timestamp;

public class Ticket {
    private int id;
    private int customerId;
    private int seatId;
    private int showtimeId;
    private double price;
    private Timestamp bookedAt;

    // Associated objects (loaded dynamically)
    private Customer customer;
    private Showtime showtime;
    private Seat seat;

    public Ticket() {}

    public Ticket(int id, int customerId, int seatId, int showtimeId, double price, Timestamp bookedAt) {
        this.id = id;
        this.customerId = customerId;
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.price = price;
        this.bookedAt = bookedAt;
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

    public Timestamp getBookedAt() { return bookedAt; }
    public void setBookedAt(Timestamp bookedAt) { this.bookedAt = bookedAt; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Showtime getShowtime() { return showtime; }
    public void setShowtime(Showtime showtime) { this.showtime = showtime; }

    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    @Override
    public String toString() {
        return "Ticket #" + id + " - " + (seat != null ? seat.getLabel() : "Seat: " + seatId) + " - " + price + " VND";
    }
}
