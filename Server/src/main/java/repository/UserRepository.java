package repository;

import model.Rol;
import model.Utilizator;
import java.sql.*;

public class UserRepository {

    public boolean save(Utilizator user) throws SQLException {
        // Nu inserăm id_utilizator (e SERIAL) și data_creare (are DEFAULT CURRENT_DATE)
        String sql = "INSERT INTO Utilizator (nume, prenume, email, parola, id_rol) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getNume());
            pstmt.setString(2, user.getPrenume());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getParola());
            pstmt.setInt(5, user.getRol().getIdRol()); // Asigură-te că id_rol există în tabela Rol

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public Utilizator findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Utilizator WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Utilizator(
                        rs.getInt("id_utilizator"),
                        rs.getString("nume"),
                        rs.getString("prenume"),
                        rs.getString("email"),
                        rs.getString("parola"),
                        rs.getDate("data_creare") != null ? rs.getDate("data_creare").toLocalDate() : null,
                        new Rol(rs.getInt("id_rol"))
                );
            }
        }
        return null;
    }
}