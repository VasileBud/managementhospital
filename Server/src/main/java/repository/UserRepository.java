package repository;

import model.User;

import java.sql.*;
import java.time.OffsetDateTime;

public class UserRepository {

    /**
     * Finds a user by email (table "user").
     * Returns null if not found.
     */
    public User findByEmail(String email) throws SQLException {
        String sql = """
                SELECT user_id, first_name, last_name, email, password_hash, created_at, role_id
                FROM "user"
                WHERE email = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                long userId = rs.getLong("user_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String userEmail = rs.getString("email");
                String passwordHash = rs.getString("password_hash");
                OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
                long roleId = rs.getLong("role_id");

                return new User(userId, firstName, lastName, userEmail, passwordHash, createdAt, roleId);
            }
        }
    }

    /**
     * Creates a new user and returns the generated user_id.
     * roleName must exist in role.role_name (PATIENT/DOCTOR/MANAGER/ADMIN).
     */
    public long createUser(String firstName,
                           String lastName,
                           String email,
                           String passwordHash,
                           String roleName) throws SQLException {

        String sql = """
                INSERT INTO "user" (first_name, last_name, email, password_hash, role_id)
                VALUES (?, ?, ?, ?, (SELECT role_id FROM role WHERE role_name = ?))
                RETURNING user_id
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.setString(5, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("user_id");
                }
            }
        }

        throw new SQLException("Failed to create user (no user_id returned). Check role_name exists.");
    }

    /**
     * Returns the role_name for a given user_id (join role).
     */
    public String findRoleNameByUserId(long userId) throws SQLException {
        String sql = """
                SELECT r.role_name
                FROM "user" u
                JOIN role r ON r.role_id = u.role_id
                WHERE u.user_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role_name");
                }
            }
        }
        return null;
    }
}
