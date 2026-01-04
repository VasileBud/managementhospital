import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbConnectionTest {

    public static void main(String[] args) {
        System.out.println("Pornesc testul de conexiune la baza de date...");
        try (Connection connection = DatabaseManager.getConnection()) {
            System.out.println("Conexiune reusita: autoCommit=" + connection.getAutoCommit());
            try (PreparedStatement ps = connection.prepareStatement("SELECT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Interogare test reusita, rezultat=" + rs.getInt(1));
                } else {
                    System.out.println("Interogarea test nu a intors randuri.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Conexiunea a esuat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
