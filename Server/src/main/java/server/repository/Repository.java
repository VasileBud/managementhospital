package server.repository;

import java.sql.*;

public class Repository {
    private static final String URL = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:6543/postgres";
    private static final String USER = "postgres.kpvooltkaebzdkinypzh";
    private static final String PASSWORD = "RazvanSiVasi";

    public static Connection getConnection() throws SQLException {
        try {
            //load driver
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver-ul PostgreSQL nu a fost găsit!", e);
        }
    }
}