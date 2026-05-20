package com.project.artconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.project.artconnect.config.DatabaseConfig;

public final class ConnectionManager {

    static {
        try {
            Class.forName(DatabaseConfig.DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError(
                    "JDBC driver not on classpath: " + DatabaseConfig.DRIVER);
        }
    }

    private ConnectionManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD);
    }
}
