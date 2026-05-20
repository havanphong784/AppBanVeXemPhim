package com.cinema;

import com.cinema.database.DatabaseHelper;
import com.cinema.view.AdminView;
import com.cinema.view.CustomerView;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class App extends Application {
    private Stage primaryStage;
    private Scene mainScene;
    private DatabaseHelper dbHelper;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.dbHelper = new DatabaseHelper();

        // Check DB Connection
        try (Connection conn = DatabaseHelper.getConnection()) {
            System.out.println("Kết nối cơ sở dữ liệu thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            showErrorLayout("Không thể kết nối đến cơ sở dữ liệu SQL Server.\nVui lòng đảm bảo SQL Server đang chạy và database 'QuanLiVeXemPhim' đã được tạo.");
            return;
        }

        showLauncherScreen();
    }

    public void showLauncherScreen() {
        VBox root = new VBox(40);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root");

        // Styling the root background to have a subtle dark gradient
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #1e0912, #0d0407);");

        Label title = new Label("CINECORE CINEMA SYSTEM");
        title.getStyleClass().add("label-title");
        title.setStyle("-fx-font-size: 36px; -fx-text-fill: #FF007F; -fx-effect: dropshadow(three-pass-box, rgba(255, 0, 127, 0.6), 20, 0, 0, 0); -fx-font-weight: 900;");

        Label subtitle = new Label("Vui lòng lựa chọn giao diện truy cập hệ thống");
        subtitle.getStyleClass().add("label-subtitle");
        subtitle.setStyle("-fx-font-size: 16px;");

        HBox options = new HBox(30);
        options.setAlignment(Pos.CENTER);

        // Option 1: Customer Card
        VBox customerCard = new VBox(20);
        customerCard.getStyleClass().add("glass-card");
        customerCard.setPrefSize(280, 200);
        customerCard.setAlignment(Pos.CENTER);
        customerCard.setCursor(javafx.scene.Cursor.HAND);
        customerCard.setStyle("-fx-border-color: rgba(255, 0, 127, 0.3); -fx-border-width: 1.5px;");
        
        Label customerIcon = new Label("🎟");
        customerIcon.setStyle("-fx-font-size: 50px;");
        Label customerLabel = new Label("CỬA HÀNG KHÁCH HÀNG");
        customerLabel.getStyleClass().add("label-normal");
        customerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        Label customerDesc = new Label("Đặt vé, sơ đồ chọn ghế, combo & hóa đơn");
        customerDesc.getStyleClass().add("label-subtitle");
        customerDesc.setStyle("-fx-text-align: center;");
        customerCard.getChildren().addAll(customerIcon, customerLabel, customerDesc);

        // Hover animations
        customerCard.setOnMouseEntered(e -> customerCard.setStyle("-fx-border-color: #FF007F; -fx-background-color: rgba(255, 0, 127, 0.1); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        customerCard.setOnMouseExited(e -> customerCard.setStyle("-fx-border-color: rgba(255, 0, 127, 0.3); -fx-background-color: rgba(30, 30, 30, 0.7); -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        customerCard.setOnMouseClicked(e -> showCustomerView());

        // Option 2: Admin Card
        VBox adminCard = new VBox(20);
        adminCard.getStyleClass().add("glass-card");
        adminCard.setPrefSize(280, 200);
        adminCard.setAlignment(Pos.CENTER);
        adminCard.setCursor(javafx.scene.Cursor.HAND);
        adminCard.setStyle("-fx-border-color: rgba(0, 240, 255, 0.3); -fx-border-width: 1.5px;");

        Label adminIcon = new Label("⚙");
        adminIcon.setStyle("-fx-font-size: 50px;");
        Label adminLabel = new Label("HỆ THỐNG QUẢN TRỊ");
        adminLabel.getStyleClass().add("label-normal");
        adminLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        Label adminDesc = new Label("Quản lý phim, suất chiếu & báo cáo doanh thu");
        adminDesc.getStyleClass().add("label-subtitle");
        adminDesc.setStyle("-fx-text-align: center;");
        adminCard.getChildren().addAll(adminIcon, adminLabel, adminDesc);

        // Hover animations
        adminCard.setOnMouseEntered(e -> adminCard.setStyle("-fx-border-color: #00F0FF; -fx-background-color: rgba(0, 240, 255, 0.1); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        adminCard.setOnMouseExited(e -> adminCard.setStyle("-fx-border-color: rgba(0, 240, 255, 0.3); -fx-background-color: rgba(30, 30, 30, 0.7); -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        adminCard.setOnMouseClicked(e -> showAdminView());

        options.getChildren().addAll(customerCard, adminCard);

        root.getChildren().addAll(title, subtitle, options);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/com/cinema/styles.css").toExternalForm());
        primaryStage.setTitle("CineCore Cinema Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showCustomerView() {
        CustomerView view = new CustomerView(dbHelper, () -> showLauncherScreen());
        Scene scene = new Scene(view, 1024, 700);
        scene.getStylesheets().add(getClass().getResource("/com/cinema/styles.css").toExternalForm());
        primaryStage.setTitle("CineCore Client - Đặt Vé Xem Phim");
        primaryStage.setScene(scene);
    }

    private void showAdminView() {
        AdminView view = new AdminView(dbHelper, () -> showLauncherScreen());
        Scene scene = new Scene(view, 1024, 700);
        scene.getStylesheets().add(getClass().getResource("/com/cinema/styles.css").toExternalForm());
        primaryStage.setTitle("CineCore Dashboard - Quản Trị Hệ Thống");
        primaryStage.setScene(scene);
    }

    private void showErrorLayout(String message) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #121212;");

        Label errTitle = new Label("LỖI KẾT NỐI HỆ THỐNG");
        errTitle.setStyle("-fx-text-fill: #ff3366; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label errMsg = new Label(message);
        errMsg.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-text-alignment: center;");

        root.getChildren().addAll(errTitle, errMsg);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Lỗi Hệ Thống");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
