package repository;

import model.AppointmentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRepository {

    public boolean isSlotAvailable(long doctorId, LocalDate date, LocalTime time) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM appointment
                WHERE doctor_id = ? 
                  AND appointment_date = ? 
                  AND appointment_time = ?
                  AND status != 'CANCELED'
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(time));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    public long createAppointment(long patientId, long doctorId, long serviceId,
                                  LocalDate date, LocalTime time, AppointmentStatus status) throws SQLException {
        String sql = """
                INSERT INTO appointment (patient_id, doctor_id, service_id, appointment_date, appointment_time, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                RETURNING appointment_id
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, patientId);
            ps.setLong(2, doctorId);
            if (serviceId > 0) ps.setLong(3, serviceId); else ps.setNull(3, Types.BIGINT);
            ps.setDate(4, Date.valueOf(date));
            ps.setTime(5, Time.valueOf(time));
            ps.setString(6, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("appointment_id");
            }
        }
        throw new SQLException("Failed to create appointment");
    }

    public boolean updateStatus(long appointmentId, AppointmentStatus status) throws SQLException {
        String sql = "UPDATE appointment SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setLong(2, appointmentId);

            return ps.executeUpdate() > 0;
        }
    }
}