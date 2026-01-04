package repository;

import model.User;

import java.sql.*;
import java.time.OffsetDateTime;

public class UserRepository {

    public boolean save(User user) throws SQLException {
        // "user" is reserved in Postgres -> must be quoted
        String sql = """
                INSERT INTO "user" (first_name, last_name, email, password_hash, role_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setLong(5, user.getRoleId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public long saveAndReturnId(User user) throws SQLException {
        String sql = """
                INSERT INTO "user" (first_name, last_name, email, password_hash, role_id)
                VALUES (?, ?, ?, ?, ?)
                RETURNING user_id
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setLong(5, user.getRoleId());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("user_id");
                }
            }
        }
        return -1;
    }


    public User findByEmail(String email) throws SQLException {
        String sql = """
                SELECT user_id, first_name, last_name, email, password_hash, created_at, role_id
                FROM "user"
                WHERE email = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    OffsetDateTime createdAt = null;
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        // Timestamp doesn't carry offset; Postgres TIMESTAMPTZ is in UTC internally.
                        // This is fine for most apps; if you need exact offset handling, we can use getObject with OffsetDateTime.
                        createdAt = ts.toInstant().atOffset(OffsetDateTime.now().getOffset());
                    }

                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setCreatedAt(createdAt);
                    user.setRoleId(rs.getLong("role_id"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Finds a user by id.
     */
    public User findById(long userId) throws SQLException {
        String sql = """
                SELECT user_id, first_name, last_name, email, password_hash, created_at, role_id
                FROM "user"
                WHERE user_id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));

                    // Better mapping for TIMESTAMPTZ (Postgres JDBC supports this):
                    // If your driver supports it, prefer:
                    // OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
                    // For compatibility:
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        user.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
                    }

                    user.setRoleId(rs.getLong("role_id"));
                    return user;
                }
            }
        }
        return null;
    }
}
