package com.project.artconnect.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DatabaseConfig {

    public static final String URL;
    public static final String USER;
    public static final String PASSWORD;
    public static final String DRIVER;

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/artconnect?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("[DatabaseConfig] database.properties not found on classpath; using defaults.");
            }
        } catch (IOException ex) {
            System.err.println("[DatabaseConfig] Could not load database.properties: " + ex.getMessage());
        }

        URL      = props.getProperty("db.url",      DEFAULT_URL);
        USER     = props.getProperty("db.user",     DEFAULT_USER);
        PASSWORD = props.getProperty("db.password", DEFAULT_PASSWORD);
        DRIVER   = props.getProperty("db.driver",   DEFAULT_DRIVER);
    }

    private DatabaseConfig() {
    }
}
