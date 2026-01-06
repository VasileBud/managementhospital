package server.repository;

import shared.dto.MedicalRecordEntryDTO;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordRepository {

    public List<MedicalRecordEntryDTO> findByPatientId(Long patientId) throws SQLException {
        String sql = """
                SELECT e.entry_id, e.diagnosis, e.treatment, e.notes, e.entry_date, 
                       a.appointment_id, 
                       u.first_name || ' ' || u.last_name AS doctor_name
                FROM medical_record_entry e
                JOIN patient_medical_file f ON f.file_id = e.file_id
                LEFT JOIN appointment a ON a.appointment_id = e.appointment_id
                LEFT JOIN doctor d ON d.doctor_id = a.doctor_id
                LEFT JOIN "user" u ON u.user_id = d.user_id
                WHERE f.patient_id = ?
                ORDER BY e.entry_date DESC
                """;

        List<MedicalRecordEntryDTO> result = new ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new MedicalRecordEntryDTO(
                            rs.getLong("entry_id"),
                            patientId,
                            rs.getObject("appointment_id", Long.class), // ca sa gestionam NULL
                            rs.getString("doctor_name"),
                            rs.getString("diagnosis"),
                            rs.getString("treatment"),
                            rs.getString("notes"),
                            rs.getObject("entry_date", OffsetDateTime.class)
                    ));
                }
            }
        }
        return result;
    }

    public void addEntry(Long patientId, Long appointmentId, String diagnosis, String treatment, String notes) throws SQLException {
        // gasim dosarul pacientului (sau creem)
        long fileId = getOrCreateFileId(patientId);

        // insert
        String sql = """
                INSERT INTO medical_record_entry (file_id, appointment_id, diagnosis, treatment, notes, entry_date)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, fileId);
            if (appointmentId != null) ps.setLong(2, appointmentId); else ps.setNull(2, Types.BIGINT);
            ps.setString(3, diagnosis);
            ps.setString(4, treatment);
            ps.setString(5, notes);

            ps.executeUpdate();
        }
    }

    private long getOrCreateFileId(long patientId) throws SQLException {
        // verificam daca exista
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT file_id FROM patient_medical_file WHERE patient_id = ?")) {
            ps.setLong(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("file_id");
        }

        // daca nu, creem
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO patient_medical_file (patient_id) VALUES (?) RETURNING file_id")) {
            ps.setLong(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("file_id");
        }
        throw new SQLException("Could not access medical file");
    }
}
