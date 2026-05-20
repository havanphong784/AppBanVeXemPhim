package com.cinema.model;

import java.sql.Date;
import java.sql.Time;

public class Showtime implements Comparable<Showtime> {
    private int id;
    private int movieId;
    private Date showDate;
    private Time showTime;
    private String room;

    // Associated movie
    private Movie movie;

    // Cached statistics for the custom sorted linked list
    private int ticketsSold;
    private double revenue;

    public Showtime() {}

    public Showtime(int id, int movieId, Date showDate, Time showTime, String room) {
        this.id = id;
        this.movieId = movieId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.room = room;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public Date getShowDate() { return showDate; }
    public void setShowDate(Date showDate) { this.showDate = showDate; }

    public Time getShowTime() { return showTime; }
    public void setShowTime(Time showTime) { this.showTime = showTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public int getTicketsSold() { return ticketsSold; }
    public void setTicketsSold(int ticketsSold) { this.ticketsSold = ticketsSold; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    @Override
    public int compareTo(Showtime other) {
        int dateCompare = this.showDate.compareTo(other.showDate);
        if (dateCompare != 0) {
            return dateCompare;
        }
        return this.showTime.compareTo(other.showTime);
    }

    @Override
    public String toString() {
        String movieTitle = (movie != null) ? movie.getTitle() : "Movie ID: " + movieId;
        return movieTitle + " - " + showDate + " " + showTime + " (" + room + ")";
    }
}
