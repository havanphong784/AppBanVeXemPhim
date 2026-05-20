package com.cinema.controller;

import com.cinema.dao.CinemaDAO;
import com.cinema.model.Movie;
import com.cinema.model.Showtime;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

public class MainController {
    @FXML private FlowPane movieContainer;
    private final CinemaDAO dao = new CinemaDAO();

    @FXML
    public void initialize() {
        loadMovies();
    }

    private void loadMovies() {
        try {
            List<Movie> movies = dao.getAllMovies();
            for (int i = 0; i < movies.size(); i++) {
                VBox card = createMovieCard(movies.get(i));
                card.setOpacity(0);
                movieContainer.getChildren().add(card);
                // Fade in animation staggered
                FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                ft.setFromValue(0); ft.setToValue(1);
                ft.setDelay(Duration.millis(i * 150));
                TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
                tt.setFromY(30); tt.setToY(0);
                tt.setDelay(Duration.millis(i * 150));
                ft.play(); tt.play();
            }
        } catch (Exception e) {
            showError("Không thể kết nối database: " + e.getMessage());
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(8);
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(250);

        Label title = new Label(movie.getTitle());
        title.getStyleClass().add("movie-title");
        Label genre = new Label("🎭 " + movie.getGenre());
        genre.getStyleClass().add("subtitle-label");
        Label duration = new Label("⏱ " + movie.getDuration() + " phút");
        duration.getStyleClass().add("subtitle-label");

        // Combo chọn suất chiếu
        ComboBox<Showtime> cbShowtime = new ComboBox<>();
        cbShowtime.setPromptText("Chọn suất chiếu");
        cbShowtime.setPrefWidth(220);
        try {
            cbShowtime.getItems().addAll(dao.getShowtimesByMovie(movie.getId()));
            cbShowtime.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Showtime s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : s.getShowDate() + " " + s.getShowTime() + " - " + s.getRoom());
                }
            });
            cbShowtime.setButtonCell(cbShowtime.getCellFactory().call(null));
        } catch (Exception ignored) {}

        Button btn = new Button("Chọn ghế →");
        btn.getStyleClass().add("btn-primary");
        btn.setOnAction(e -> {
            Showtime selected = cbShowtime.getValue();
            if (selected == null) { showError("Vui lòng chọn suất chiếu!"); return; }
            openSeatView(movie, selected, btn);
        });

        // Hover scale animation
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.03); st.setToY(1.03); st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1); st.setToY(1); st.play();
        });

        card.getChildren().addAll(title, genre, duration, cbShowtime, btn);
        return card;
    }

    private void openSeatView(Movie movie, Showtime showtime, Button source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SeatView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            SeatController ctrl = loader.getController();
            ctrl.init(movie, showtime);
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
