package com.cinema.controller;

import com.cinema.dao.CinemaDAO;
import com.cinema.model.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;
import java.util.concurrent.*;

public class SeatController {
    @FXML private GridPane seatGrid;
    @FXML private Label movieTitleLabel, showtimeLabel, selectedLabel;
    @FXML private Button bookBtn;

    private final CinemaDAO dao = new CinemaDAO();
    private Movie movie;
    private Showtime showtime;
    private final Map<Integer, Button> seatButtons = new HashMap<>();
    private final Set<Integer> selectedSeatIds = new HashSet<>();
    private ScheduledExecutorService scheduler;

    public void init(Movie movie, Showtime showtime) {
        this.movie = movie;
        this.showtime = showtime;
        movieTitleLabel.setText("🎬 " + movie.getTitle());
        showtimeLabel.setText(showtime.getShowDate() + " | " + showtime.getShowTime() + " | " + showtime.getRoom());
        loadSeats();
        startRealtimePolling();
    }

    private void loadSeats() {
        try {
            List<Seat> seats = dao.getSeatsByShowtime(showtime.getId());
            seatGrid.getChildren().clear();
            seatButtons.clear();
            int col = 0, row = 0;
            char currentRow = 0;

            for (int i = 0; i < seats.size(); i++) {
                Seat seat = seats.get(i);
                if (seat.getSeatRow() != currentRow) {
                    currentRow = seat.getSeatRow();
                    col = 0; row++;
                }
                Button btn = new Button(seat.getLabel());
                btn.getStyleClass().addAll("seat-btn", getSeatStyleClass(seat));
                btn.setDisable("BOOKED".equals(seat.getStatus()));
                btn.setOnAction(e -> toggleSeat(seat, btn));
                seatGrid.add(btn, col++, row);
                seatButtons.put(seat.getId(), btn);

                // Entrance animation
                btn.setOpacity(0); btn.setScaleX(0.5); btn.setScaleY(0.5);
                FadeTransition ft = new FadeTransition(Duration.millis(300), btn);
                ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(i * 20));
                ScaleTransition st = new ScaleTransition(Duration.millis(300), btn);
                st.setFromX(0.5); st.setFromY(0.5); st.setToX(1); st.setToY(1);
                st.setDelay(Duration.millis(i * 20));
                ft.play(); st.play();
            }
        } catch (Exception e) {
            showError("Lỗi tải ghế: " + e.getMessage());
        }
    }

    private void toggleSeat(Seat seat, Button btn) {
        if (selectedSeatIds.contains(seat.getId())) {
            selectedSeatIds.remove(seat.getId());
            btn.getStyleClass().removeAll("seat-selected");
            btn.getStyleClass().add("seat-available");
        } else {
            selectedSeatIds.add(seat.getId());
            btn.getStyleClass().removeAll("seat-available");
            btn.getStyleClass().add("seat-selected");
            // Bounce animation
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setFromX(1); st.setFromY(1); st.setToX(1.2); st.setToY(1.2);
            st.setAutoReverse(true); st.setCycleCount(2); st.play();
        }
        updateSelectedLabel();
    }

    private void updateSelectedLabel() {
        int count = selectedSeatIds.size();
        selectedLabel.setText(count == 0 ? "Chưa chọn ghế" : "Đã chọn: " + count + " ghế | " + (count * 75000) + " VNĐ");
        bookBtn.setDisable(count == 0);
    }

    // Realtime: poll database mỗi 3 giây để cập nhật ghế đã bị người khác đặt
    private void startRealtimePolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r); t.setDaemon(true); return t; });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Seat> seats = dao.getSeatsByShowtime(showtime.getId());
                Platform.runLater(() -> {
                    for (Seat seat : seats) {
                        Button btn = seatButtons.get(seat.getId());
                        if (btn != null && "BOOKED".equals(seat.getStatus()) && !btn.getStyleClass().contains("seat-booked")) {
                            btn.getStyleClass().removeAll("seat-available", "seat-selected");
                            btn.getStyleClass().add("seat-booked");
                            btn.setDisable(true);
                            selectedSeatIds.remove(seat.getId());
                            // Flash animation khi ghế bị người khác đặt
                            FadeTransition ft = new FadeTransition(Duration.millis(200), btn);
                            ft.setFromValue(0.3); ft.setToValue(1); ft.setCycleCount(4); ft.play();
                        }
                    }
                    updateSelectedLabel();
                });
            } catch (Exception ignored) {}
        }, 3, 3, TimeUnit.SECONDS);
    }

    @FXML
    private void onBook() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Thông tin khách hàng");
        dlg.setHeaderText("Nhập tên khách hàng:");
        Optional<String> name = dlg.showAndWait();
        if (name.isEmpty() || name.get().isBlank()) return;

        try {
            dao.bookSeats(new ArrayList<>(selectedSeatIds), name.get(), "", showtime.getId(), 75000);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đặt vé thành công! 🎉", ButtonType.OK);
            alert.showAndWait();
            selectedSeatIds.clear();
            loadSeats();
        } catch (Exception e) {
            showError("Đặt vé thất bại: " + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        stopPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            ((Stage) seatGrid.getScene().getWindow()).setScene(scene);
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void stopPolling() {
        if (scheduler != null) scheduler.shutdownNow();
    }

    private String getSeatStyleClass(Seat seat) {
        return switch (seat.getStatus()) {
            case "BOOKED" -> "seat-booked";
            case "SELECTED" -> "seat-selected";
            default -> "seat-available";
        };
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
