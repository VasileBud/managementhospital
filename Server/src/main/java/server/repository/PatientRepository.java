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

    public shared.dto.PatientDetailsDTO findDetailsByPatientId(long patientId) throws SQLException {
        String sql = """
                SELECT p.patient_id,
                       p.national_id,
                       p.birth_date,
                       p.phone,
                       p.address,
                       u.first_name,
                       u.last_name,
                       pp.blood_type,
                       pp.weight_kg,
                       pp.height_cm
                FROM patient p
                JOIN "user" u ON u.user_id = p.user_id
                LEFT JOIN patient_profile pp ON pp.patient_id = p.patient_id
                WHERE p.patient_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                java.sql.Date birth = rs.getDate("birth_date");
                java.time.LocalDate birthDate = birth == null ? null : birth.toLocalDate();
                java.math.BigDecimal weight = rs.getBigDecimal("weight_kg");
                java.math.BigDecimal height = rs.getBigDecimal("height_cm");
                Double weightKg = weight == null ? null : weight.doubleValue();
                Double heightCm = height == null ? null : height.doubleValue();

                return new shared.dto.PatientDetailsDTO(
                        rs.getLong("patient_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("national_id"),
                        birthDate,
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("blood_type"),
                        weightKg,
                        heightCm,
                        getAllergies(patientId),
                        getConditions(patientId)
                );
            }
        }
    }

    private java.util.List<String> getAllergies(long patientId) throws SQLException {
        String sql = "SELECT name FROM patient_allergy WHERE patient_id = ? ORDER BY name";
        java.util.List<String> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("name"));
                }
            }
        }
        return result;
    }

    private java.util.List<String> getConditions(long patientId) throws SQLException {
        String sql = "SELECT name FROM patient_condition WHERE patient_id = ? ORDER BY name";
        java.util.List<String> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("name"));
                }
            }
        }
        return result;
    }
}
