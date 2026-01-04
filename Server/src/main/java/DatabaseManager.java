import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://db.kpvooltkaebzdkinypzh.supabase.co:6543/postgres";
    private static final String USER = "postgres.db.kpvooltkaebzdkinypzh";
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