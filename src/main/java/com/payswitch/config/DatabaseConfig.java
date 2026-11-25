package com.payswitch.config;

import java.sql.*;

/** Reads connection settings from environment, with local development defaults. */
public final class DatabaseConfig {
    private DatabaseConfig(){}
    public static Connection getConnection() throws SQLException {
        String url=System.getenv().getOrDefault("PAYSWITCH_DB_URL","jdbc:mysql://localhost:3306/payswitch_db?useSSL=false&serverTimezone=UTC");
        String user=System.getenv().getOrDefault("PAYSWITCH_DB_USER","root");
        String password=System.getenv().getOrDefault("PAYSWITCH_DB_PASSWORD","");
        return DriverManager.getConnection(url,user,password);
    }
}
