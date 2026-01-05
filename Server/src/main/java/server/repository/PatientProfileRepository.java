package server.repository;

import shared.dto.PatientProfileDTO;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PatientProfileRepository {

    public PatientProfileDTO getProfile(long patientId) throws SQLException {
        String sql = """
                SELECT blood_type, weight_kg, height_cm
                FROM patient_profile
                WHERE patient_id = ?
                """;

        String bloodType = null;
        Double weightKg = null;
        Double heightCm = null;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bloodType = rs.getString("blood_type");
                    BigDecimal weight = rs.getBigDecimal("weight_kg");
                    BigDecimal height = rs.getBigDecimal("height_cm");
                    weightKg = weight == null ? null : weight.doubleValue();
                    heightCm = height == null ? null : height.doubleValue();
                }
            }
        }

        List<String> allergies = getAllergies(patientId);
        List<String> conditions = getConditions(patientId);

        return new PatientProfileDTO(patientId, bloodType, weightKg, heightCm, allergies, conditions);
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
