package model.repository;

import model.Patient;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientRepository {

    public long createPatient(long userId,
                              String nationalId,
                              String address,
                              String phone,
                              java.time.LocalDate birthDate) throws SQLException {
        try (Connection conn = Repository.getConnection()) {
            return createPatient(conn, userId, nationalId, address, phone, birthDate);
        }
    }

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
            ps.setString(2, (nationalId == null) ? "" : nationalId);
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

    public boolean updateNationalIdByUserId(long userId, String nationalId) throws SQLException {
        String sql = "UPDATE patient SET national_id = ? WHERE user_id = ?";
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nationalId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public Patient findDetailsByPatientId(long patientId) throws SQLException {
        String sql = """
                SELECT p.patient_id,
                       p.user_id,
                       p.national_id,
                       p.address,
                       p.phone,
                       p.birth_date,
                       p.gender,
                       p.created_at,
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

                LocalDate birthDate = null;
                java.sql.Date birth = rs.getDate("birth_date");
                if (birth != null) {
                    birthDate = birth.toLocalDate();
                }

                BigDecimal weight = rs.getBigDecimal("weight_kg");
                BigDecimal height = rs.getBigDecimal("height_cm");
                Double weightKg = weight == null ? null : weight.doubleValue();
                Double heightCm = height == null ? null : height.doubleValue();

                OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);

                List<String> allergies = getAllergies(patientId);
                List<String> conditions = getConditions(patientId);

                return new Patient(
                        rs.getLong("patient_id"),
                        rs.getLong("user_id"),
                        rs.getString("national_id"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        birthDate,
                        rs.getString("gender"),
                        createdAt,
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("blood_type"),
                        weightKg,
                        heightCm,
                        allergies,
                        conditions
                );
            }
        }
    }

    private List<String> getAllergies(long patientId) throws SQLException {
        String sql = "SELECT name FROM patient_allergy WHERE patient_id = ? ORDER BY name";
        List<String> result = new ArrayList<>();
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

    private List<String> getConditions(long patientId) throws SQLException {
        String sql = "SELECT name FROM patient_condition WHERE patient_id = ? ORDER BY name";
        List<String> result = new ArrayList<>();
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
