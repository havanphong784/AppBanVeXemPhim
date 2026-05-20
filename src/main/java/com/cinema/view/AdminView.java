package com.cinema.view;

import com.cinema.database.DatabaseHelper;
import com.cinema.datastructure.MyList;
import com.cinema.datastructure.ShowtimeLinkedList;
import com.cinema.model.Movie;
import com.cinema.model.Showtime;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminView extends BorderPane {
    private final DatabaseHelper dbHelper;
    private ShowtimeLinkedList showtimes;
    private MyList<Movie> movies;

    private TabPane tabPane;
    
    // Dashboard widgets
    private Label totalRevLabel;
    private Label totalTicketsLabel;
    private Label activeMoviesLabel;
    
    // Movie CRUD widgets
    private ListView<Movie> movieListView;
    private TextField movieTitleField;
    private TextField movieGenreField;
    private TextField movieDurationField;
    private TextField moviePosterField;
    private TextArea movieDescArea;
    private Movie selectedMovie;

    // Showtime scheduling widgets
    private ComboBox<Movie> scheduleMovieCombo;
    private DatePicker scheduleDatePicker;
    private TextField scheduleTimeField;
    private ComboBox<String> scheduleRoomCombo;
    private TableView<Showtime> showtimeTableView;

    // Stats widgets
    private ComboBox<String> statsTypeCombo;
    private TextField statsValField;
    private Label statsSummaryLabel;
    private TableView<Showtime> statsTableView;

    private final Runnable exitCallback;

    public AdminView(DatabaseHelper dbHelper, Runnable exitCallback) {
        this.dbHelper = dbHelper;
        this.exitCallback = exitCallback;
        
        initTabs();
        loadData();
        refreshDashboard();
    }

    private void loadData() {
        this.movies = dbHelper.loadMovies();
        this.showtimes = dbHelper.loadShowtimes(movies);
        
        // Refresh CRUD controls lists
        if (movieListView != null) {
            movieListView.getItems().clear();
            for (Movie m : movies) {
                movieListView.getItems().add(m);
            }
        }
        if (scheduleMovieCombo != null) {
            scheduleMovieCombo.getItems().clear();
            for (Movie m : movies) {
                scheduleMovieCombo.getItems().add(m);
            }
        }
        
        refreshShowtimesTable();
    }

    private void initTabs() {
        tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        Tab dashboardTab = new Tab("Tổng Quan Dashboard", createDashboardLayout());
        dashboardTab.setClosable(false);

        Tab movieCrudTab = new Tab("Quản Lý Phim", createMovieCrudLayout());
        movieCrudTab.setClosable(false);

        Tab showtimeScheduleTab = new Tab("Lịch Suất Chiếu", createShowtimeScheduleLayout());
        showtimeScheduleTab.setClosable(false);

        Tab statsTab = new Tab("Thống Kê Báo Cáo", createStatsLayout());
        statsTab.setClosable(false);

        Tab exitTab = new Tab("Đăng Xuất");
        exitTab.setClosable(false);

        tabPane.getTabs().addAll(dashboardTab, movieCrudTab, showtimeScheduleTab, statsTab, exitTab);
        setCenter(tabPane);
        
        // Tab Selection Listener
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == exitTab) {
                exitCallback.run();
            } else {
                loadData();
                if (newTab == dashboardTab) {
                    refreshDashboard();
                }
            }
        });
    }

    // ----------------------------------------------------
    // TAB 1: DASHBOARD
    // ----------------------------------------------------
    private VBox createDashboardLayout() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("BẢNG ĐIỀU KHIỂN QUẢN TRỊ VIÊN");
        title.getStyleClass().add("label-title");

        HBox statsGrid = new HBox(20);
        statsGrid.setAlignment(Pos.TOP_LEFT);

        // Revenue Card
        VBox revCard = new VBox(10);
        revCard.getStyleClass().add("stat-card");
        revCard.setPrefWidth(220);
        Label revTitle = new Label("TỔNG DOANH THU");
        revTitle.getStyleClass().add("label-subtitle");
        totalRevLabel = new Label("0 VNĐ");
        totalRevLabel.getStyleClass().add("label-title");
        totalRevLabel.setStyle("-fx-text-fill: #00F0FF; -fx-font-size: 20px;");
        revCard.getChildren().addAll(revTitle, totalRevLabel);

        // Tickets Card
        VBox ticketsCard = new VBox(10);
        ticketsCard.getStyleClass().add("stat-card");
        ticketsCard.setPrefWidth(220);
        Label ticketsTitle = new Label("SỐ VÉ ĐÃ BÁN");
        ticketsTitle.getStyleClass().add("label-subtitle");
        totalTicketsLabel = new Label("0 vé");
        totalTicketsLabel.getStyleClass().add("label-title");
        totalTicketsLabel.setStyle("-fx-text-fill: #FF007F; -fx-font-size: 20px;");
        ticketsCard.getChildren().addAll(ticketsTitle, totalTicketsLabel);

        // Movies Card
        VBox moviesCard = new VBox(10);
        moviesCard.getStyleClass().add("stat-card");
        moviesCard.setPrefWidth(220);
        Label moviesTitle = new Label("PHIM ĐANG HOẠT ĐỘNG");
        moviesTitle.getStyleClass().add("label-subtitle");
        activeMoviesLabel = new Label("0 phim");
        activeMoviesLabel.getStyleClass().add("label-title");
        activeMoviesLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 20px;");
        moviesCard.getChildren().addAll(moviesTitle, activeMoviesLabel);

        statsGrid.getChildren().addAll(revCard, ticketsCard, moviesCard);

        // Visual Info Box
        VBox welcomeBox = new VBox(10);
        welcomeBox.getStyleClass().add("glass-card");
        welcomeBox.setStyle("-fx-background-color: rgba(255, 0, 127, 0.05);");
        
        Label welcomeTitle = new Label("Chào mừng đến với Cinema Management Console");
        welcomeTitle.getStyleClass().add("label-normal");
        welcomeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        
        Label welcomeDesc = new Label("Sử dụng các tab phía trên để thêm/chỉnh sửa phim, lên lịch chiếu cho phòng và tra cứu doanh thu thông qua cấu trúc cây dữ liệu sắp xếp.");
        welcomeDesc.getStyleClass().add("label-subtitle");
        welcomeBox.getChildren().addAll(welcomeTitle, welcomeDesc);

        layout.getChildren().addAll(title, statsGrid, welcomeBox);
        return layout;
    }

    private void refreshDashboard() {
        // Query Stats from tree
        MyList<Showtime> empty = new MyList<>();
        ShowtimeLinkedList.StatsResult stats = showtimes.getStatsAll(empty);

        totalRevLabel.setText(String.format("%,.0f VNĐ", stats.sumRevenue));
        
        int tickets = 0;
        for (Showtime s : showtimes.getAllShowtimes()) {
            tickets += s.getTicketsSold();
        }
        totalTicketsLabel.setText(tickets + " vé");
        activeMoviesLabel.setText(movies.size() + " phim");
    }

    // ----------------------------------------------------
    // TAB 2: MOVIE CRUD
    // ----------------------------------------------------
    private HBox createMovieCrudLayout() {
        HBox layout = new HBox(25);
        layout.setPadding(new Insets(25));

        // Left Pane: Movies List
        VBox leftPane = new VBox(10);
        leftPane.setPrefWidth(300);
        Label listTitle = new Label("DANH SÁCH PHIM");
        listTitle.getStyleClass().add("label-normal");
        listTitle.setStyle("-fx-font-weight: bold;");

        movieListView = new ListView<>();
        movieListView.getStyleClass().add("list-view");
        movieListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedMovie = newVal;
                movieTitleField.setText(newVal.getTitle());
                movieGenreField.setText(newVal.getGenre());
                movieDurationField.setText(String.valueOf(newVal.getDuration()));
                moviePosterField.setText(newVal.getPosterUrl());
                movieDescArea.setText(newVal.getDescription());
            }
        });
        VBox.setVgrow(movieListView, Priority.ALWAYS);
        leftPane.getChildren().addAll(listTitle, movieListView);

        // Right Pane: Input Form
        VBox rightPane = new VBox(15);
        rightPane.getStyleClass().add("glass-card");
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        Label formTitle = new Label("THÔNG TIN PHIM CHI TIẾT");
        formTitle.getStyleClass().add("label-normal");
        formTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF007F;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);

        movieTitleField = new TextField();
        movieTitleField.getStyleClass().add("text-input");
        movieTitleField.setPromptText("Tên phim...");

        movieGenreField = new TextField();
        movieGenreField.getStyleClass().add("text-input");
        movieGenreField.setPromptText("Thể loại (Hành động, Hài, ...)...");

        movieDurationField = new TextField();
        movieDurationField.getStyleClass().add("text-input");
        movieDurationField.setPromptText("Thời lượng (phút)...");

        moviePosterField = new TextField();
        moviePosterField.getStyleClass().add("text-input");
        moviePosterField.setPromptText("Poster URL (tùy chọn)...");

        movieDescArea = new TextArea();
        movieDescArea.getStyleClass().add("text-input");
        movieDescArea.setPromptText("Mô tả nội dung phim...");
        movieDescArea.setPrefRowCount(4);
        movieDescArea.setWrapText(true);

        formGrid.add(new Label("Tên Phim *"), 0, 0);
        formGrid.add(movieTitleField, 1, 0);
        formGrid.add(new Label("Thể Loại *"), 0, 1);
        formGrid.add(movieGenreField, 1, 1);
        formGrid.add(new Label("Thời Lượng *"), 0, 2);
        formGrid.add(movieDurationField, 1, 2);
        formGrid.add(new Label("Poster URL"), 0, 3);
        formGrid.add(moviePosterField, 1, 3);
        formGrid.add(new Label("Mô Tả Phim"), 0, 4);
        formGrid.add(movieDescArea, 1, 4);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("Thêm Mới");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            if (validateMovieInputs()) {
                Movie m = new Movie(
                        0,
                        movieTitleField.getText().trim(),
                        movieGenreField.getText().trim(),
                        Integer.parseInt(movieDurationField.getText().trim()),
                        moviePosterField.getText().trim(),
                        movieDescArea.getText().trim()
                );
                int id = dbHelper.insertMovie(m);
                if (id != -1) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã thêm phim mới thành công.");
                    clearMovieFields();
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất Bại", "Lỗi lưu phim vào cơ sở dữ liệu.");
                }
            }
        });

        Button updateBtn = new Button("Cập Nhật");
        updateBtn.getStyleClass().add("btn-secondary");
        updateBtn.setOnAction(e -> {
            if (selectedMovie == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa Chọn Phim", "Vui lòng chọn một bộ phim từ danh sách để cập nhật.");
                return;
            }
            if (validateMovieInputs()) {
                selectedMovie.setTitle(movieTitleField.getText().trim());
                selectedMovie.setGenre(movieGenreField.getText().trim());
                selectedMovie.setDuration(Integer.parseInt(movieDurationField.getText().trim()));
                selectedMovie.setPosterUrl(moviePosterField.getText().trim());
                selectedMovie.setDescription(movieDescArea.getText().trim());

                if (dbHelper.updateMovie(selectedMovie)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã cập nhật thông tin phim thành công.");
                    clearMovieFields();
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất Bại", "Lỗi cập nhật phim.");
                }
            }
        });

        Button deleteBtn = new Button("Xóa Phim");
        deleteBtn.getStyleClass().add("btn-ghost");
        deleteBtn.setStyle("-fx-text-fill: red;");
        deleteBtn.setOnAction(e -> {
            if (selectedMovie == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa Chọn Phim", "Vui lòng chọn phim để xóa.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hành động này sẽ xóa vĩnh viễn phim, toàn bộ suất chiếu và vé liên quan. Bạn có chắc chắn muốn xóa?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                if (dbHelper.deleteMovie(selectedMovie.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã xóa phim thành công.");
                    clearMovieFields();
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất Bại", "Lỗi xóa phim.");
                }
            }
        });

        buttonBox.getChildren().addAll(addBtn, updateBtn, deleteBtn);
        rightPane.getChildren().addAll(formTitle, formGrid, buttonBox);

        layout.getChildren().addAll(leftPane, rightPane);
        return layout;
    }

    private void clearMovieFields() {
        selectedMovie = null;
        movieTitleField.clear();
        movieGenreField.clear();
        movieDurationField.clear();
        moviePosterField.clear();
        movieDescArea.clear();
        movieListView.getSelectionModel().clearSelection();
    }

    private boolean validateMovieInputs() {
        if (movieTitleField.getText().trim().isEmpty() ||
            movieGenreField.getText().trim().isEmpty() ||
            movieDurationField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu Thông Tin", "Vui lòng điền đầy đủ các thông tin bắt buộc (*)");
            return false;
        }
        try {
            int duration = Integer.parseInt(movieDurationField.getText().trim());
            if (duration <= 0) {
                showAlert(Alert.AlertType.WARNING, "Thời Lượng Sai", "Thời lượng phim phải lớn hơn 0 phút.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Thời Lượng Sai", "Thời lượng phim phải là số nguyên.");
            return false;
        }
        return true;
    }

    // ----------------------------------------------------
    // TAB 3: SHOWTIME SCHEDULING
    // ----------------------------------------------------
    private HBox createShowtimeScheduleLayout() {
        HBox layout = new HBox(25);
        layout.setPadding(new Insets(25));

        // Left form
        VBox leftPane = new VBox(15);
        leftPane.getStyleClass().add("glass-card");
        leftPane.setPrefWidth(350);

        Label formTitle = new Label("LÊN LỊCH SUẤT CHIẾU");
        formTitle.getStyleClass().add("label-normal");
        formTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #00F0FF;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);

        scheduleMovieCombo = new ComboBox<>();
        scheduleMovieCombo.getStyleClass().add("combo-box");
        scheduleMovieCombo.setPromptText("Chọn phim...");

        scheduleDatePicker = new DatePicker(LocalDate.now());
        scheduleDatePicker.getStyleClass().add("date-picker");

        scheduleTimeField = new TextField();
        scheduleTimeField.getStyleClass().add("text-input");
        scheduleTimeField.setPromptText("HH:mm:ss (Ví dụ: 19:30:00)");

        scheduleRoomCombo = new ComboBox<>();
        scheduleRoomCombo.getStyleClass().add("combo-box");
        scheduleRoomCombo.getItems().addAll("Phòng chiếu IMAX", "Phòng chiếu Gold Class", "Phòng 3", "Phòng 4");
        scheduleRoomCombo.getSelectionModel().select(0);

        formGrid.add(new Label("Phim *"), 0, 0);
        formGrid.add(scheduleMovieCombo, 1, 0);
        formGrid.add(new Label("Ngày Chiếu *"), 0, 1);
        formGrid.add(scheduleDatePicker, 1, 1);
        formGrid.add(new Label("Giờ Chiếu *"), 0, 2);
        formGrid.add(scheduleTimeField, 1, 2);
        formGrid.add(new Label("Phòng Chiếu *"), 0, 3);
        formGrid.add(scheduleRoomCombo, 1, 3);

        Button scheduleBtn = new Button("Lên Lịch Chiếu");
        scheduleBtn.getStyleClass().add("btn-primary");
        scheduleBtn.setMaxWidth(Double.MAX_VALUE);
        scheduleBtn.setOnAction(e -> {
            Movie selected = scheduleMovieCombo.getValue();
            LocalDate localDate = scheduleDatePicker.getValue();
            String timeStr = scheduleTimeField.getText().trim();
            String room = scheduleRoomCombo.getValue();

            if (selected == null || localDate == null || timeStr.isEmpty() || room == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu Thông Tin", "Vui lòng nhập đầy đủ các thông tin để lên lịch.");
                return;
            }

            try {
                // Parse Time
                Time time = Time.valueOf(timeStr);
                Date date = Date.valueOf(localDate);

                // Validation rule: check overlap conflict (Room, date, time)
                // Duration rule: showTime -> showTime + movieDuration + 30m clean-up
                if (!dbHelper.isRoomAvailable(selected.getDuration(), date, time, room, -1)) {
                    showAlert(Alert.AlertType.ERROR, "Trùng Lịch Phòng", "Phòng chiếu này đang có suất chiếu khác hoạt động trong khoảng thời gian này. Vui lòng dời giờ chiếu.");
                    return;
                }

                Showtime s = new Showtime(0, selected.getId(), date, time, room);
                int id = dbHelper.insertShowtime(s);
                if (id != -1) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã tạo suất chiếu mới và tạo tự động 80 ghế thành công.");
                    scheduleTimeField.clear();
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi tạo suất chiếu.");
                }

            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Định Dạng Giờ", "Vui lòng nhập đúng định dạng giờ HH:mm:ss (Ví dụ: 19:30:00).");
            }
        });

        leftPane.getChildren().addAll(formTitle, formGrid, scheduleBtn);

        // Right TableView
        VBox rightPane = new VBox(10);
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        Label tableTitle = new Label("LỊCH TRÌNH SUẤT CHIẾU HIỆN TẠI (TẤT CẢ)");
        tableTitle.getStyleClass().add("label-normal");
        tableTitle.setStyle("-fx-font-weight: bold;");

        showtimeTableView = new TableView<>();
        showtimeTableView.getStyleClass().add("table-view");

        TableColumn<Showtime, String> movieCol = new TableColumn<>("Phim");
        movieCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMovie() != null ? data.getValue().getMovie().getTitle() : "ID: " + data.getValue().getMovieId()));

        TableColumn<Showtime, String> dateCol = new TableColumn<>("Ngày");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShowDate().toString()));

        TableColumn<Showtime, String> timeCol = new TableColumn<>("Giờ");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShowTime().toString()));

        TableColumn<Showtime, String> roomCol = new TableColumn<>("Phòng");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoom()));

        showtimeTableView.getColumns().addAll(movieCol, dateCol, timeCol, roomCol);

        // Add Delete Button for showtimes
        Button deleteShowtimeBtn = new Button("Xóa Suất Chiếu Đã Chọn");
        deleteShowtimeBtn.getStyleClass().add("btn-ghost");
        deleteShowtimeBtn.setStyle("-fx-text-fill: red;");
        deleteShowtimeBtn.setOnAction(e -> {
            Showtime selected = showtimeTableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa Chọn", "Vui lòng chọn suất chiếu từ bảng để xóa.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hành động này sẽ xóa suất chiếu, vé và sơ đồ ghế liên quan. Bạn chắc chắn?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                if (dbHelper.deleteShowtime(selected.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã xóa suất chiếu thành công.");
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xóa suất chiếu.");
                }
            }
        });

        rightPane.getChildren().addAll(tableTitle, showtimeTableView, deleteShowtimeBtn);

        layout.getChildren().addAll(leftPane, rightPane);
        return layout;
    }

    private void refreshShowtimesTable() {
        if (showtimeTableView != null) {
            showtimeTableView.getItems().clear();
            MyList<Showtime> all = showtimes.getAllShowtimes();
            for (Showtime s : all) {
                showtimeTableView.getItems().add(s);
            }
        }
    }

    // ----------------------------------------------------
    // TAB 4: STATISTICS REPORTING
    // ----------------------------------------------------
    private VBox createStatsLayout() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("BÁO CÁO THỐNG KÊ DOANH THU PHÂN CẤP");
        title.getStyleClass().add("label-title");
        title.setStyle("-fx-text-fill: #FF007F;");

        // Controls bar
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        statsTypeCombo = new ComboBox<>();
        statsTypeCombo.getStyleClass().add("combo-box");
        statsTypeCombo.getItems().addAll("Báo cáo theo Ngày", "Báo cáo theo Tháng", "Báo cáo theo Năm", "Tất cả các Suất");
        statsTypeCombo.getSelectionModel().select(3);

        statsValField = new TextField();
        statsValField.getStyleClass().add("text-input");
        statsValField.setPromptText("Nhập giá trị số (Ví dụ: Ngày 15, Tháng 5, Năm 2026)...");
        statsValField.setPrefWidth(300);
        statsValField.setDisable(true); // default all is selected

        statsTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals("Tất cả các Suất")) {
                statsValField.clear();
                statsValField.setDisable(true);
            } else {
                statsValField.setDisable(false);
            }
        });

        Button runReportBtn = new Button("Chạy Báo Cáo");
        runReportBtn.getStyleClass().add("btn-primary");
        runReportBtn.setOnAction(e -> runStatisticsReport());

        filterBar.getChildren().addAll(new Label("Loại thống kê:"), statsTypeCombo, statsValField, runReportBtn);

        // Stats summary label
        statsSummaryLabel = new Label("Chọn điều kiện lọc và bấm nút 'Chạy Báo Cáo'...");
        statsSummaryLabel.getStyleClass().add("label-normal");
        statsSummaryLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #00F0FF; -fx-font-weight: bold;");

        // TableView for stats showtimes
        statsTableView = new TableView<>();
        statsTableView.getStyleClass().add("table-view");

        TableColumn<Showtime, String> movieCol = new TableColumn<>("Phim");
        movieCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMovie() != null ? data.getValue().getMovie().getTitle() : "ID: " + data.getValue().getMovieId()));

        TableColumn<Showtime, String> dateCol = new TableColumn<>("Ngày chiếu");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShowDate().toString()));

        TableColumn<Showtime, String> timeCol = new TableColumn<>("Giờ chiếu");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShowTime().toString()));

        TableColumn<Showtime, String> roomCol = new TableColumn<>("Phòng");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoom()));

        TableColumn<Showtime, Integer> ticketsCol = new TableColumn<>("Vé đã bán");
        ticketsCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTicketsSold()).asObject());

        TableColumn<Showtime, String> revCol = new TableColumn<>("Doanh thu");
        revCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f VNĐ", data.getValue().getRevenue())));

        statsTableView.getColumns().addAll(movieCol, dateCol, timeCol, roomCol, ticketsCol, revCol);

        layout.getChildren().addAll(title, filterBar, statsSummaryLabel, statsTableView);
        return layout;
    }

    private void runStatisticsReport() {
        String reportType = statsTypeCombo.getValue();
        String valStr = statsValField.getText().trim();
        
        MyList<Showtime> matchedShowtimes = new MyList<>();
        ShowtimeLinkedList.StatsResult result = null;

        if (reportType.equals("Tất cả các Suất")) {
            result = showtimes.getStatsAll(matchedShowtimes);
        } else {
            if (valStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu Giá Trị", "Vui lòng nhập giá trị lọc (dạng số).");
                return;
            }
            try {
                int val = Integer.parseInt(valStr);
                switch (reportType) {
                    case "Báo cáo theo Ngày":
                        if (val < 1 || val > 31) {
                            showAlert(Alert.AlertType.WARNING, "Lỗi Số Ngày", "Ngày phải nằm trong khoảng 1-31.");
                            return;
                        }
                        result = showtimes.getStatsForDay(val, matchedShowtimes);
                        break;
                    case "Báo cáo theo Tháng":
                        if (val < 1 || val > 12) {
                            showAlert(Alert.AlertType.WARNING, "Lỗi Số Tháng", "Tháng phải nằm trong khoảng 1-12.");
                            return;
                        }
                        result = showtimes.getStatsForMonth(val, matchedShowtimes);
                        break;
                    case "Báo cáo theo Năm":
                        if (val < 2000 || val > 2100) {
                            showAlert(Alert.AlertType.WARNING, "Lỗi Số Năm", "Vui lòng nhập số năm hợp lệ.");
                            return;
                        }
                        result = showtimes.getStatsForYear(val, matchedShowtimes);
                        break;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Định Dạng", "Giá trị lọc phải là một số nguyên.");
                return;
            }
        }

        // Show result summary
        if (result != null) {
            statsSummaryLabel.setText(result.toString());
            
            // Populate Table
            statsTableView.getItems().clear();
            for (Showtime s : matchedShowtimes) {
                statsTableView.getItems().add(s);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}
