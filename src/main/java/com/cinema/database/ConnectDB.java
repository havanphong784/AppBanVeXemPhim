package com.cinema.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLiVeXemPhim;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASS = "123456";
    private static ConnectDB instance;
    private Connection connection;

    private ConnectDB() {}

    public static ConnectDB getInstance() {
        if (instance == null) instance = new ConnectDB();
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
