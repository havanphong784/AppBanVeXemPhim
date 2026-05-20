package com.cinema.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Showtime {
    private int id, movieId;
    private LocalDate showDate;
    private LocalTime showTime;
    private String room;

    public Showtime() {}
    public Showtime(int id, int movieId, LocalDate showDate, LocalTime showTime, String room) {
        this.id = id; this.movieId = movieId; this.showDate = showDate;
        this.showTime = showTime; this.room = room;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}
