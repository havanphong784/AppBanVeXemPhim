package com.cinema.view;

import com.cinema.database.DatabaseHelper;
import com.cinema.datastructure.MyList;
import com.cinema.datastructure.ShowtimeLinkedList;
import com.cinema.model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.StrokeType;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Random;

public class CustomerView extends BorderPane {
    private final DatabaseHelper dbHelper;
    private ShowtimeLinkedList showtimes;
    private MyList<Movie> movies;

    private VBox contentPane;
    private TextField searchField;
    
    // Selection state
    private Movie selectedMovie;
    private Showtime selectedShowtime;
    private MyList<Seat> selectedSeats;
    
    // Combo counts
    private int popcornComboCount = 0;
    private int drinkComboCount = 0;
    private final double POPCORN_PRICE = 50000;
    private final double DRINK_PRICE = 40000;
    private final double TICKET_PRICE = 75000;

    private final Runnable exitCallback;

    public CustomerView(DatabaseHelper dbHelper, Runnable exitCallback) {
        this.dbHelper = dbHelper;
        this.exitCallback = exitCallback;
        this.selectedSeats = new MyList<>();
        
        initHeader();
        initContentArea();
        loadData();
        showMovieList();
    }

    private void loadData() {
        this.movies = dbHelper.loadMovies();
        this.showtimes = dbHelper.loadShowtimes(movies);
    }

    private void initHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("nav-bar");
        header.setSpacing(20);

        Label title = new Label("CINECORE CLIENT");
        title.getStyleClass().add("label-title");
        title.setStyle("-fx-text-fill: #FF007F;");

        Button homeBtn = new Button("Trang Chủ");
        homeBtn.getStyleClass().add("btn-ghost");
        homeBtn.setOnAction(e -> {
            loadData();
            showMovieList();
        });

        Button historyBtn = new Button("Lịch Sử Đặt Vé");
        historyBtn.getStyleClass().add("btn-ghost");
        historyBtn.setOnAction(e -> showHistoryScreen());

        Button exitBtn = new Button("Đăng Xuất");
        exitBtn.getStyleClass().add("btn-ghost");
        exitBtn.setStyle("-fx-text-fill: #ff3366;");
        exitBtn.setOnAction(e -> {
            dbHelper.unlockAllSeatsForShowtime(selectedShowtime != null ? selectedShowtime.getId() : -1);
            exitCallback.run();
        });

        searchField = new TextField();
        searchField.setPromptText("Tìm kiếm phim, thể loại...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("text-input");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMovies(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, homeBtn, historyBtn, exitBtn, spacer, searchField);
        setTop(header);
    }

    private void initContentArea() {
        contentPane = new VBox();
        contentPane.setPadding(new Insets(25));
        contentPane.setSpacing(20);
        contentPane.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(contentPane);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        setCenter(scrollPane);
    }

    private void showMovieList() {
        contentPane.getChildren().clear();
        selectedMovie = null;
        selectedShowtime = null;
        selectedSeats.clear();
        popcornComboCount = 0;
        drinkComboCount = 0;

        Label sectionTitle = new Label("PHIM ĐANG CHIẾU");
        sectionTitle.getStyleClass().add("label-title");
        contentPane.getChildren().add(sectionTitle);

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(20);
        flowPane.setVgap(20);
        flowPane.setAlignment(Pos.TOP_LEFT);

        for (Movie movie : movies) {
            flowPane.getChildren().add(createMovieCard(movie));
        }

        contentPane.getChildren().add(flowPane);
    }

    private void filterMovies(String query) {
        if (selectedMovie != null) return; // Ignore if details page is open
        
        contentPane.getChildren().clear();
        
        Label sectionTitle = new Label("KẾT QUẢ TÌM KIẾM");
        sectionTitle.getStyleClass().add("label-title");
        contentPane.getChildren().add(sectionTitle);

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(20);
        flowPane.setVgap(20);
        flowPane.setAlignment(Pos.TOP_LEFT);

        String lowercaseQuery = query.toLowerCase();
        for (Movie movie : movies) {
            if (movie.getTitle().toLowerCase().contains(lowercaseQuery) || 
                movie.getGenre().toLowerCase().contains(lowercaseQuery)) {
                flowPane.getChildren().add(createMovieCard(movie));
            }
        }

        contentPane.getChildren().add(flowPane);
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox();
        card.setPrefSize(220, 320);
        card.getStyleClass().add("glass-card");
        card.setSpacing(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Poster Placeholder (Draw canvas representing a beautiful neon poster)
        Canvas poster = new Canvas(180, 180);
        GraphicsContext gc = poster.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRoundRect(0, 0, 180, 180, 10, 10);
        gc.setStroke(Color.web("#FF007F"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(0, 0, 180, 180, 10, 10);
        
        // Draw movie text/genre on canvas
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 14));
        gc.fillText(movie.getTitle().length() > 18 ? movie.getTitle().substring(0, 15) + "..." : movie.getTitle(), 10, 80);
        gc.setFill(Color.web("#00F0FF"));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", 12));
        gc.fillText(movie.getGenre(), 10, 110);

        Label title = new Label(movie.getTitle());
        title.getStyleClass().add("label-normal");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        title.setWrapText(true);
        title.setMaxWidth(180);

        Label duration = new Label("Thời lượng: " + movie.getDuration() + " phút");
        duration.getStyleClass().add("label-subtitle");

        card.getChildren().addAll(poster, title, duration);
        card.setOnMouseClicked(e -> showMovieDetails(movie));

        return card;
    }

    private void showMovieDetails(Movie movie) {
        selectedMovie = movie;
        contentPane.getChildren().clear();

        Button backBtn = new Button("← Quay Lại");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> showMovieList());

        HBox detailContainer = new HBox(30);
        detailContainer.setAlignment(Pos.TOP_LEFT);
        detailContainer.setPadding(new Insets(10));

        Canvas poster = new Canvas(200, 260);
        GraphicsContext gc = poster.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRoundRect(0, 0, 200, 260, 15, 15);
        gc.setStroke(Color.web("#00F0FF"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(0, 0, 200, 260, 15, 15);
        
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 18));
        gc.fillText(movie.getTitle().length() > 15 ? movie.getTitle().substring(0, 12) + "..." : movie.getTitle(), 15, 100);
        gc.setFill(Color.web("#FF007F"));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", 14));
        gc.fillText(movie.getGenre(), 15, 140);

        VBox textDetails = new VBox(15);
        textDetails.setAlignment(Pos.TOP_LEFT);

        Label title = new Label(movie.getTitle().toUpperCase());
        title.getStyleClass().add("label-title");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px;");

        Label genre = new Label("Thể loại: " + movie.getGenre());
        genre.getStyleClass().add("label-normal");
        genre.setStyle("-fx-font-size: 16px;");

        Label duration = new Label("Thời lượng: " + movie.getDuration() + " phút");
        duration.getStyleClass().add("label-normal");
        duration.setStyle("-fx-font-size: 16px;");

        Label descTitle = new Label("Tóm tắt nội dung:");
        descTitle.getStyleClass().add("label-normal");
        descTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label desc = new Label(movie.getDescription() != null ? movie.getDescription() : "Không có mô tả.");
        desc.getStyleClass().add("label-subtitle");
        desc.setStyle("-fx-font-size: 14px;");
        desc.setWrapText(true);
        desc.setMaxWidth(600);

        textDetails.getChildren().addAll(title, genre, duration, descTitle, desc);
        detailContainer.getChildren().addAll(poster, textDetails);

        // Showtime selection area
        VBox showtimesArea = new VBox(15);
        showtimesArea.setPadding(new Insets(20, 0, 0, 0));
        Label showtimesTitle = new Label("SUẤT CHIẾU KHẢ DỤNG");
        showtimesTitle.getStyleClass().add("label-title");
        showtimesTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #FF007F;");

        FlowPane showtimesPane = new FlowPane(15, 15);
        
        // Find showtimes for this movie
        MyList<Showtime> allShows = showtimes.getAllShowtimes();
        int showCount = 0;
        for (Showtime showtime : allShows) {
            if (showtime.getMovieId() == movie.getId()) {
                Button showBtn = new Button(showtime.getShowDate() + " @ " + showtime.getShowTime() + "\n(" + showtime.getRoom() + ")");
                showBtn.getStyleClass().add("btn-secondary");
                showBtn.setStyle("-fx-alignment: center; -fx-text-alignment: center;");
                showBtn.setOnAction(e -> startBookingFlow(showtime));
                showtimesPane.getChildren().add(showBtn);
                showCount++;
            }
        }

        if (showCount == 0) {
            Label noShows = new Label("Hiện không có suất chiếu nào được lên lịch cho bộ phim này.");
            noShows.getStyleClass().add("label-subtitle");
            showtimesPane.getChildren().add(noShows);
        }

        showtimesArea.getChildren().addAll(showtimesTitle, showtimesPane);
        contentPane.getChildren().addAll(backBtn, detailContainer, showtimesArea);
    }

    private void startBookingFlow(Showtime showtime) {
        this.selectedShowtime = showtime;
        this.selectedSeats.clear();
        
        // Make sure to clean up any temporary locks this user had
        dbHelper.unlockAllSeatsForShowtime(showtime.getId());

        contentPane.getChildren().clear();

        Button backBtn = new Button("← Quay Lại Phim");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> showMovieDetails(selectedMovie));

        Label stepTitle = new Label("BƯỚC 1: CHỌN GHẾ NGỒI");
        stepTitle.getStyleClass().add("label-title");
        stepTitle.setStyle("-fx-text-fill: #FF007F;");

        // IMAX Curved Screen styling
        VBox screenContainer = new VBox();
        screenContainer.setAlignment(Pos.CENTER);
        screenContainer.setPadding(new Insets(10, 0, 30, 0));
        
        // Use a path or arc to represent screen
        Arc screenArc = new Arc(0, 0, 300, 40, 0, 180);
        screenArc.setType(javafx.scene.shape.ArcType.OPEN);
        screenArc.setStroke(Color.web("#00F0FF"));
        screenArc.setStrokeWidth(5);
        screenArc.setStrokeType(StrokeType.CENTERED);
        screenArc.setFill(Color.TRANSPARENT);
        screenArc.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,240,255,0.7), 15, 0, 0, 0);");
        
        Label screenText = new Label("MÀN HÌNH");
        screenText.getStyleClass().add("label-subtitle");
        screenText.setStyle("-fx-font-weight: bold; -fx-text-fill: #00F0FF; -fx-font-size: 14px;");
        
        screenContainer.getChildren().addAll(screenArc, screenText);

        // Seat Grid
        VBox seatGrid = new VBox(8);
        seatGrid.setAlignment(Pos.CENTER);

        // Legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(0, 0, 20, 0));

        legend.getChildren().addAll(
                createLegendItem("AVAILABLE", Color.WHITE, "Thường"),
                createLegendItem("VIP", Color.GOLD, "VIP (Hàng C-E)"),
                createLegendItem("LOCKED", Color.ORANGE, "Đang khóa"),
                createLegendItem("BOOKED", Color.web("#444444"), "Đã đặt"),
                createLegendItem("SELECTED", Color.web("#FF007F"), "Đang chọn")
        );

        // Load seats from Database
        MyList<Seat> seats = dbHelper.loadSeats(showtime.getId());
        
        // Group seats by Row (Rows A to H, Cols 1 to 10)
        // Since we are prohibited from using Arrays/ArrayLists in our data logic,
        // we can dynamically build the grid using HBoxes based on letters.
        for (char rowChar = 'A'; rowChar <= 'H'; rowChar++) {
            HBox rowBox = new HBox(8);
            rowBox.setAlignment(Pos.CENTER);
            
            Label rowLabel = new Label(String.valueOf(rowChar));
            rowLabel.getStyleClass().add("label-subtitle");
            rowLabel.setStyle("-fx-font-weight: bold; -fx-pref-width: 20px;");
            rowBox.getChildren().add(rowLabel);

            for (int col = 1; col <= 10; col++) {
                // Find seat in list
                Seat matchSeat = null;
                for (Seat s : seats) {
                    if (s.getSeatRow().equals(String.valueOf(rowChar)) && s.getSeatCol() == col) {
                        matchSeat = s;
                        break;
                    }
                }

                if (matchSeat != null) {
                    Button seatBtn = createSeatButton(matchSeat);
                    rowBox.getChildren().add(seatBtn);
                }
            }
            seatGrid.getChildren().add(rowBox);
        }

        // Action buttons
        Button nextBtn = new Button("Chọn Combo Tiếp Theo →");
        nextBtn.getStyleClass().add("btn-primary");
        nextBtn.setOnAction(e -> {
            if (selectedSeats.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Chưa Chọn Ghế", "Vui lòng chọn ít nhất một ghế để tiếp tục.");
            } else {
                showComboScreen();
            }
        });

        contentPane.getChildren().addAll(backBtn, stepTitle, screenContainer, legend, seatGrid, nextBtn);
    }

    private HBox createLegendItem(String status, Color color, String labelText) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        Region colorBox = new Region();
        colorBox.setPrefSize(18, 18);
        colorBox.setStyle("-fx-background-color: " + toRGBHex(color) + "; -fx-background-radius: 4px; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 4px;");
        Label label = new Label(labelText);
        label.getStyleClass().add("label-subtitle");
        item.getChildren().addAll(colorBox, label);
        return item;
    }

    private Button createSeatButton(Seat seat) {
        Button btn = new Button(String.valueOf(seat.getSeatCol()));
        btn.setPrefSize(35, 35);
        btn.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        
        // Define initial visual style based on status
        boolean isVip = seat.getSeatRow().equals("C") || seat.getSeatRow().equals("D") || seat.getSeatRow().equals("E");
        setSeatButtonStyle(btn, seat.getStatus(), isVip, false);

        btn.setOnAction(e -> {
            if (seat.getStatus().equals("BOOKED")) {
                showAlert(Alert.AlertType.ERROR, "Ghế Đã Có Người Đặt", "Ghế này đã được thanh toán và đặt chỗ.");
                return;
            }
            if (seat.getStatus().equals("LOCKED") && !selectedSeats.contains(seat)) {
                showAlert(Alert.AlertType.WARNING, "Ghế Đang Bị Khóa", "Ghế này đang có người khác thực hiện giao dịch.");
                return;
            }

            if (selectedSeats.contains(seat)) {
                // Unlock seat
                if (dbHelper.unlockSeat(seat.getId())) {
                    selectedSeats.remove(seat);
                    seat.setStatus("AVAILABLE");
                    setSeatButtonStyle(btn, "AVAILABLE", isVip, false);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể giải phóng ghế ngồi lúc này.");
                }
            } else {
                // Try to lock seat
                if (dbHelper.lockSeat(seat.getId())) {
                    selectedSeats.add(seat);
                    seat.setStatus("LOCKED");
                    setSeatButtonStyle(btn, "LOCKED", isVip, true);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Không Thể Chọn Ghế", "Ghế vừa bị giữ chỗ bởi người dùng khác. Vui lòng chọn ghế khác.");
                    // Refresh seats list
                    startBookingFlow(selectedShowtime);
                }
            }
        });

        return btn;
    }

    private void setSeatButtonStyle(Button btn, String status, boolean isVip, boolean isSelectedByMe) {
        if (isSelectedByMe) {
            btn.setStyle("-fx-background-color: #FF007F; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
            return;
        }

        switch (status) {
            case "AVAILABLE":
                if (isVip) {
                    btn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-background-radius: 6px; -fx-cursor: hand;");
                } else {
                    btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 6px; -fx-cursor: hand;");
                }
                break;
            case "LOCKED":
                btn.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
                break;
            case "BOOKED":
                btn.setStyle("-fx-background-color: #444444; -fx-text-fill: #999999; -fx-background-radius: 6px;");
                break;
        }
    }

    private void showComboScreen() {
        contentPane.getChildren().clear();

        Button backBtn = new Button("← Quay Lại Chọn Ghế");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> startBookingFlow(selectedShowtime));

        Label stepTitle = new Label("BƯỚC 2: CHỌN COMBO BẮP NƯỚC");
        stepTitle.getStyleClass().add("label-title");
        stepTitle.setStyle("-fx-text-fill: #FF007F;");

        VBox comboContainer = new VBox(20);
        comboContainer.setMaxWidth(600);
        comboContainer.setAlignment(Pos.CENTER);

        // Popcorn Combo Card
        HBox popcornCard = createComboCard("Combo Bắp & Nước đơn", "1 Bắp lớn + 1 Nước ngọt lớn", POPCORN_PRICE, true);
        // Twin Combo Card
        HBox twinCard = createComboCard("Combo Đôi", "1 Bắp lớn + 2 Nước ngọt lớn", DRINK_PRICE, false);

        comboContainer.getChildren().addAll(popcornCard, twinCard);

        Button nextBtn = new Button("Chuyển Sang Thanh Toán →");
        nextBtn.getStyleClass().add("btn-primary");
        nextBtn.setOnAction(e -> showPaymentScreen());

        contentPane.getChildren().addAll(backBtn, stepTitle, comboContainer, nextBtn);
    }

    private HBox createComboCard(String title, String desc, double price, boolean isPopcorn) {
        HBox card = new HBox(20);
        card.getStyleClass().add("glass-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label comboTitle = new Label(title);
        comboTitle.getStyleClass().add("label-normal");
        comboTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label comboDesc = new Label(desc);
        comboDesc.getStyleClass().add("label-subtitle");
        Label comboPrice = new Label(String.format("%,.0f VNĐ", price));
        comboPrice.getStyleClass().add("label-mono");
        info.getChildren().addAll(comboTitle, comboDesc, comboPrice);

        HBox selector = new HBox(10);
        selector.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("btn-secondary");
        minusBtn.setPrefSize(30, 30);

        Label qtyLabel = new Label(String.valueOf(isPopcorn ? popcornComboCount : drinkComboCount));
        qtyLabel.getStyleClass().add("label-normal");
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-min-width: 25px; -fx-alignment: center;");

        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("btn-secondary");
        plusBtn.setPrefSize(30, 30);

        minusBtn.setOnAction(e -> {
            if (isPopcorn) {
                if (popcornComboCount > 0) popcornComboCount--;
                qtyLabel.setText(String.valueOf(popcornComboCount));
            } else {
                if (drinkComboCount > 0) drinkComboCount--;
                qtyLabel.setText(String.valueOf(drinkComboCount));
            }
        });

        plusBtn.setOnAction(e -> {
            if (isPopcorn) {
                popcornComboCount++;
                qtyLabel.setText(String.valueOf(popcornComboCount));
            } else {
                drinkComboCount++;
                qtyLabel.setText(String.valueOf(drinkComboCount));
            }
        });

        selector.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
        card.getChildren().addAll(info, selector);

        return card;
    }

    private void showPaymentScreen() {
        contentPane.getChildren().clear();

        Button backBtn = new Button("← Quay Lại Chọn Combo");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> showComboScreen());

        Label stepTitle = new Label("BƯỚC 3: XÁC NHẬN THÔNG TIN & THANH TOÁN");
        stepTitle.getStyleClass().add("label-title");
        stepTitle.setStyle("-fx-text-fill: #FF007F;");

        GridPane layoutGrid = new GridPane();
        layoutGrid.setHgap(30);
        layoutGrid.setVgap(20);
        layoutGrid.setAlignment(Pos.TOP_CENTER);
        layoutGrid.setPadding(new Insets(10));

        // Form details
        VBox form = new VBox(15);
        form.getStyleClass().add("glass-card");
        form.setPrefWidth(400);

        Label formTitle = new Label("THÔNG TIN KHÁCH HÀNG");
        formTitle.getStyleClass().add("label-normal");
        formTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #00F0FF;");

        TextField nameField = new TextField();
        nameField.setPromptText("Họ và Tên...");
        nameField.getStyleClass().add("text-input");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại...");
        phoneField.getStyleClass().add("text-input");

        TextField emailField = new TextField();
        emailField.setPromptText("Email (Tùy chọn)...");
        emailField.getStyleClass().add("text-input");

        form.getChildren().addAll(formTitle, new Label("Họ tên *"), nameField, new Label("Điện thoại *"), phoneField, new Label("Email"), emailField);

        // Bill Summary
        VBox bill = new VBox(15);
        bill.getStyleClass().add("glass-card");
        bill.setPrefWidth(350);

        Label billTitle = new Label("HÓA ĐƠN ĐẶT VÉ");
        billTitle.getStyleClass().add("label-normal");
        billTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF007F;");

        VBox billDetails = new VBox(8);
        billDetails.setAlignment(Pos.TOP_LEFT);

        Label movieTitle = new Label("Phim: " + selectedMovie.getTitle());
        movieTitle.getStyleClass().add("label-normal");
        movieTitle.setStyle("-fx-font-weight: bold;");

        Label showtimeText = new Label("Suất: " + selectedShowtime.getShowDate() + " " + selectedShowtime.getShowTime() + " (" + selectedShowtime.getRoom() + ")");
        showtimeText.getStyleClass().add("label-subtitle");

        StringBuilder seatLabels = new StringBuilder();
        for (Seat s : selectedSeats) {
            if (seatLabels.length() > 0) seatLabels.append(", ");
            seatLabels.append(s.getLabel());
        }
        Label seatsText = new Label("Ghế đã chọn: " + seatLabels);
        seatsText.getStyleClass().add("label-normal");

        double seatsCost = selectedSeats.size() * TICKET_PRICE;
        Label seatsCostLabel = new Label(String.format("Tiền vé: %d x %,.0f = %,.0f VNĐ", selectedSeats.size(), TICKET_PRICE, seatsCost));
        seatsCostLabel.getStyleClass().add("label-subtitle");

        double comboCost = (popcornComboCount * POPCORN_PRICE) + (drinkComboCount * DRINK_PRICE);
        Label comboCostLabel = new Label(String.format("Tiền combo: %,.0f VNĐ", comboCost));
        comboCostLabel.getStyleClass().add("label-subtitle");

        double totalCost = seatsCost + comboCost;
        Label totalLabel = new Label(String.format("TỔNG TIỀN: %,.0f VNĐ", totalCost));
        totalLabel.getStyleClass().add("label-title");
        totalLabel.setStyle("-fx-text-fill: #00F0FF; -fx-font-size: 20px;");

        billDetails.getChildren().addAll(movieTitle, showtimeText, seatsText, seatsCostLabel, comboCostLabel, new Separator(), totalLabel);
        
        Button payBtn = new Button("Thanh Toán & Nhận Vé (Simulate)");
        payBtn.getStyleClass().add("btn-primary");
        payBtn.setMaxWidth(Double.MAX_VALUE);
        payBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng nhập đầy đủ Họ tên và Số điện thoại.");
                return;
            }

            Customer c = new Customer();
            c.setFullName(name);
            c.setPhone(phone);
            c.setEmail(email);

            // 1. Process customer (save/get ID)
            int custId = dbHelper.getOrInsertCustomer(c);
            if (custId == -1) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Giao Dịch", "Không thể xử lý thông tin khách hàng.");
                return;
            }

            // 2. Book tickets one by one
            boolean success = true;
            MyList<Ticket> bookedTickets = new MyList<>();
            for (Seat seat : selectedSeats) {
                Ticket t = new Ticket();
                t.setCustomerId(custId);
                t.setSeatId(seat.getId());
                t.setShowtimeId(selectedShowtime.getId());
                t.setPrice(TICKET_PRICE);
                t.setBookedAt(new Timestamp(System.currentTimeMillis()));

                if (dbHelper.bookTicket(t)) {
                    t.setSeat(seat);
                    t.setShowtime(selectedShowtime);
                    t.setCustomer(c);
                    bookedTickets.add(t);
                } else {
                    success = false;
                    break;
                }
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Đặt Vé Thành Công", "Chúc mừng! Bạn đã đặt chỗ thành công.");
                showTicketScreen(bookedTickets, totalCost);
            } else {
                showAlert(Alert.AlertType.ERROR, "Đặt Vé Thất Bại", "Lỗi trong quá trình thanh toán hoặc ghế ngồi đã hết hạn khóa. Vui lòng thử lại.");
                startBookingFlow(selectedShowtime);
            }
        });

        bill.getChildren().addAll(billTitle, billDetails, payBtn);

        layoutGrid.add(form, 0, 0);
        layoutGrid.add(bill, 1, 0);

        contentPane.getChildren().addAll(backBtn, stepTitle, layoutGrid);
    }

    private void showTicketScreen(MyList<Ticket> tickets, double totalCost) {
        contentPane.getChildren().clear();

        Label ticketTitle = new Label("VÉ ĐIỆN TỬ CỦA BẠN");
        ticketTitle.getStyleClass().add("label-title");
        ticketTitle.setStyle("-fx-text-fill: #FF007F;");

        VBox card = new VBox(20);
        card.getStyleClass().add("glass-card");
        card.setMaxWidth(500);
        card.setAlignment(Pos.CENTER);

        Label successMsg = new Label("GIAO DỊCH THÀNH CÔNG");
        successMsg.getStyleClass().add("label-title");
        successMsg.setStyle("-fx-text-fill: #00F0FF; -fx-font-size: 20px;");

        VBox details = new VBox(8);
        details.setAlignment(Pos.TOP_LEFT);
        details.setPadding(new Insets(10));

        Ticket first = tickets.get(0);
        details.getChildren().add(new Label("Khách hàng: " + first.getCustomer().getFullName()));
        details.getChildren().add(new Label("Số điện thoại: " + first.getCustomer().getPhone()));
        details.getChildren().add(new Label("Phim: " + first.getShowtime().getMovie().getTitle()));
        details.getChildren().add(new Label("Rạp: " + first.getShowtime().getRoom()));
        details.getChildren().add(new Label("Suất chiếu: " + first.getShowtime().getShowDate() + " @ " + first.getShowtime().getShowTime()));
        
        StringBuilder seatsStr = new StringBuilder();
        for (Ticket t : tickets) {
            if (seatsStr.length() > 0) seatsStr.append(", ");
            seatsStr.append(t.getSeat().getLabel());
        }
        details.getChildren().add(new Label("Số Ghế: " + seatsStr));
        details.getChildren().add(new Label("Tổng tiền thanh toán: " + String.format("%,.0f VNĐ", totalCost)));

        // Programmatic QR Code Canvas generator (Draw a genuine-looking random 16x16 QR grid)
        Canvas qrCanvas = new Canvas(150, 150);
        GraphicsContext gc = qrCanvas.getGraphicsContext2D();
        drawSimulatedQRCode(gc);

        Label checkInInfo = new Label("Đưa mã QR này cho nhân viên soát vé tại rạp");
        checkInInfo.getStyleClass().add("label-subtitle");
        checkInInfo.setStyle("-fx-font-weight: bold;");

        card.getChildren().addAll(successMsg, details, qrCanvas, checkInInfo);

        Button doneBtn = new Button("Hoàn Tất & Về Trang Chủ");
        doneBtn.getStyleClass().add("btn-primary");
        doneBtn.setOnAction(e -> {
            loadData();
            showMovieList();
        });

        contentPane.getChildren().addAll(ticketTitle, card, doneBtn);
    }

    private void drawSimulatedQRCode(GraphicsContext gc) {
        int size = 150;
        int grid = 15;
        int cellSize = size / grid;
        
        // Background white
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, size, size);

        // Pattern corners (standard QR markers)
        gc.setFill(Color.BLACK);
        // Top-left
        gc.fillRect(0, 0, 7 * cellSize, 7 * cellSize);
        gc.setFill(Color.WHITE);
        gc.fillRect(cellSize, cellSize, 5 * cellSize, 5 * cellSize);
        gc.setFill(Color.BLACK);
        gc.fillRect(2 * cellSize, 2 * cellSize, 3 * cellSize, 3 * cellSize);

        // Top-right
        gc.fillRect((grid - 7) * cellSize, 0, 7 * cellSize, 7 * cellSize);
        gc.setFill(Color.WHITE);
        gc.fillRect((grid - 6) * cellSize, cellSize, 5 * cellSize, 5 * cellSize);
        gc.setFill(Color.BLACK);
        gc.fillRect((grid - 5) * cellSize, 2 * cellSize, 3 * cellSize, 3 * cellSize);

        // Bottom-left
        gc.fillRect(0, (grid - 7) * cellSize, 7 * cellSize, 7 * cellSize);
        gc.setFill(Color.WHITE);
        gc.fillRect(cellSize, (grid - 6) * cellSize, 5 * cellSize, 5 * cellSize);
        gc.setFill(Color.BLACK);
        gc.fillRect(2 * cellSize, (grid - 5) * cellSize, 3 * cellSize, 3 * cellSize);

        // Random bits in the rest of the grid
        Random rand = new Random();
        gc.setFill(Color.BLACK);
        for (int r = 0; r < grid; r++) {
            for (int c = 0; c < grid; c++) {
                // Skip the three corners
                if ((r < 7 && c < 7) || (r < 7 && c >= grid - 7) || (r >= grid - 7 && c < 7)) {
                    continue;
                }
                if (rand.nextBoolean()) {
                    gc.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                }
            }
        }
    }

    private void showHistoryScreen() {
        contentPane.getChildren().clear();

        Label histTitle = new Label("LỊCH SỬ ĐẶT VÉ CỦA KHÁCH HÀNG");
        histTitle.getStyleClass().add("label-title");
        histTitle.setStyle("-fx-text-fill: #FF007F;");

        HBox searchArea = new HBox(15);
        searchArea.setAlignment(Pos.CENTER);
        searchArea.setPadding(new Insets(10));

        TextField phoneSearch = new TextField();
        phoneSearch.setPromptText("Nhập số điện thoại để tra cứu...");
        phoneSearch.setPrefWidth(300);
        phoneSearch.getStyleClass().add("text-input");

        Button searchBtn = new Button("Tìm kiếm");
        searchBtn.getStyleClass().add("btn-primary");

        searchArea.getChildren().addAll(phoneSearch, searchBtn);

        VBox historyResults = new VBox(15);
        historyResults.setAlignment(Pos.TOP_CENTER);

        searchBtn.setOnAction(e -> {
            String phone = phoneSearch.getText().trim();
            if (phone.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu Số Điện Thoại", "Vui lòng nhập số điện thoại để tra cứu.");
                return;
            }

            historyResults.getChildren().clear();
            MyList<Ticket> historyList = dbHelper.loadTicketHistory(phone);

            if (historyList.isEmpty()) {
                Label noHistory = new Label("Không tìm thấy lịch sử giao dịch nào cho số điện thoại này.");
                noHistory.getStyleClass().add("label-subtitle");
                historyResults.getChildren().add(noHistory);
            } else {
                // Group tickets by transaction time/showtime to represent booking groups nicely
                for (Ticket ticket : historyList) {
                    VBox tCard = new VBox(10);
                    tCard.getStyleClass().add("glass-card");
                    tCard.setMaxWidth(600);

                    HBox line1 = new HBox(20);
                    Label movieLbl = new Label(ticket.getShowtime().getMovie().getTitle().toUpperCase());
                    movieLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #00F0FF; -fx-font-size: 15px;");
                    Label seatLbl = new Label("Ghế: " + ticket.getSeat().getLabel());
                    seatLbl.getStyleClass().add("label-normal");
                    line1.getChildren().addAll(movieLbl, seatLbl);

                    HBox line2 = new HBox(20);
                    Label roomLbl = new Label("Phòng: " + ticket.getShowtime().getRoom());
                    roomLbl.getStyleClass().add("label-subtitle");
                    Label timeLbl = new Label("Thời gian chiếu: " + ticket.getShowtime().getShowDate() + " " + ticket.getShowtime().getShowTime());
                    timeLbl.getStyleClass().add("label-subtitle");
                    line2.getChildren().addAll(roomLbl, timeLbl);

                    HBox line3 = new HBox(20);
                    Label priceLbl = new Label(String.format("Giá: %,.0f VNĐ", ticket.getPrice()));
                    priceLbl.getStyleClass().add("label-mono");
                    Label bookedAtLbl = new Label("Ngày đặt: " + ticket.getBookedAt());
                    bookedAtLbl.getStyleClass().add("label-subtitle");
                    line3.getChildren().addAll(priceLbl, bookedAtLbl);

                    tCard.getChildren().addAll(line1, line2, line3);
                    historyResults.getChildren().add(tCard);
                }
            }
        });

        contentPane.getChildren().addAll(histTitle, searchArea, historyResults);
    }

    private void showAlert(Alert.AlertType type, String title, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private String toRGBHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
