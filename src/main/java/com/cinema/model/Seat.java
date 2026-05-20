package com.cinema.model;

public class Seat {
    private int id;
    private int showtimeId;
    private String seatRow;
    private int seatCol;
    private String status; // AVAILABLE, LOCKED, BOOKED

    public Seat() {}

    public Seat(int id, int showtimeId, String seatRow, int seatCol, String status) {
        this.id = id;
        this.showtimeId = showtimeId;
        this.seatRow = seatRow;
        this.seatCol = seatCol;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    public String getSeatRow() { return seatRow; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; }

    public int getSeatCol() { return seatCol; }
    public void setSeatCol(int seatCol) { this.seatCol = seatCol; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLabel() {
        return seatRow + seatCol;
    }

    @Override
    public String toString() {
        return getLabel() + " (" + status + ")";
    }
}
