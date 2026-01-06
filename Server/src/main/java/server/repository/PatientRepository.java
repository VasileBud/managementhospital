package server.repository;

import java.sql.*;

public class PatientRepository {

    /**
     * Creates a patient profile using its own connection.
     * Returns the generated patient_id.
     */
    public long createPatient(long userId,
                              String nationalId,
                              String address,
                              String phone,
                              java.time.LocalDate birthDate) throws SQLException {
        try (Connection conn = Repository.getConnection()) {
            return createPatient(conn, userId, nationalId, address, phone, birthDate);
        }
    }

    /**
     * Creates a patient profile linked to an existing user_id.
     * Returns the generated patient_id.
     */
    public long createPatient(Connection conn,
                              long userId,
                              String nationalId,
                              String address,
                              String phone,
                              java.time.LocalDate birthDate) throws SQLException {

        String sql = """
            INSERT INTO patient (user_id, national_id, address, phone, birth_date)
            VALUES (?, ?, ?, ?, ?)
            RETURNING patient_id
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, (nationalId == null) ? "" : nationalId); //if nationalId is null, send empty string
            ps.setString(3, address);
            ps.setString(4, phone);

            if (birthDate != null) {
                ps.setDate(5, java.sql.Date.valueOf(birthDate));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("patient_id");
            }
        }

        throw new SQLException("Failed to create patient (no patient_id returned).");
    }

    /**
     * Finds patient_id by user_id.
     * Returns null if not found.
     */
    public Long findPatientIdByUserId(long userId) throws SQLException {
        String sql = """
                SELECT patient_id
                FROM patient
                WHERE user_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("patient_id");
                }
            }
        }

        return null;
    }
}
