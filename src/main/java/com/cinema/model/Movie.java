package com.cinema.model;

public class Movie {
    private int id;
    private String title, genre, posterUrl, description;
    private int duration;

    public Movie() {}
    public Movie(int id, String title, String genre, int duration, String posterUrl, String description) {
        this.id = id; this.title = title; this.genre = genre;
        this.duration = duration; this.posterUrl = posterUrl; this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
