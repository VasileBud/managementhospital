package model.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FeedbackRepository {

    public void createFeedback(long appointmentId, int rating, String comment) throws SQLException {
        String sql = """
                INSERT INTO feedback (appointment_id, rating, comment, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, appointmentId);
            ps.setInt(2, rating);
            ps.setString(3, comment);

            ps.executeUpdate();
        }
    }
}