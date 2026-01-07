package server.repository;

import server.model.AppointmentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRepository {

    private static final String DB_STATUS_CANCELED = "CANCELLED";

    private String toDbStatus(AppointmentStatus status) {
        if (status == null) {
            return null;
        }
        if (status == AppointmentStatus.CANCELED) {
            return DB_STATUS_CANCELED;
        }
        return status.name();
    }

    public boolean isSlotAvailable(long doctorId, LocalDate date, LocalTime time) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM appointment
                WHERE doctor_id = ? 
                  AND appointment_date = ? 
                  AND appointment_time = ?
                  AND status::text NOT IN ('CANCELED', 'CANCELLED')
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
                VALUES (?, ?, ?, ?, ?, ?::appointment_status, CURRENT_TIMESTAMP)
                RETURNING appointment_id
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, patientId);
            ps.setLong(2, doctorId);
            if (serviceId > 0) ps.setLong(3, serviceId); else ps.setNull(3, Types.BIGINT);
            ps.setDate(4, Date.valueOf(date));
            ps.setTime(5, Time.valueOf(time));
            ps.setString(6, toDbStatus(status));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("appointment_id");
            }
        }
        throw new SQLException("Failed to create appointment");
    }

    public boolean updateAppointment(long appointmentId, long doctorId, Long serviceId,
                                     LocalDate date, LocalTime time) throws SQLException {
        String sql = """
            UPDATE appointment
            SET doctor_id = ?,
                service_id = ?,
                appointment_date = ?,
                appointment_time = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE appointment_id = ?
            """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);
            if (serviceId != null) ps.setLong(2, serviceId); else ps.setNull(2, Types.BIGINT);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            ps.setLong(5, appointmentId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(long appointmentId, AppointmentStatus status) throws SQLException {
        String sql = "UPDATE appointment SET status = ?::appointment_status, updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, toDbStatus(status));
            ps.setLong(2, appointmentId);

            return ps.executeUpdate() > 0;
        }
    }

    public java.util.List<LocalTime> findBookedTimes(long doctorId, LocalDate date) throws SQLException {
        String sql = """
                SELECT appointment_time
                FROM appointment
                WHERE doctor_id = ?
                  AND appointment_date = ?
                  AND status::text NOT IN ('CANCELED', 'CANCELLED')
                """;

        java.util.List<LocalTime> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time time = rs.getTime("appointment_time");
                    if (time != null) {
                        result.add(time.toLocalTime());
                    }
                }
            }
        }
        return result;
    }

    public shared.dto.AppointmentDTO findNextByPatientId(long patientId) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.appointment_date,
                       a.appointment_time,
                       a.status,
                       u.first_name || ' ' || u.last_name AS doctor_name,
                       COALESCE(s.name, sp.name) AS service_name
                FROM appointment a
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN "user" u ON u.user_id = d.user_id
                LEFT JOIN medical_service s ON s.service_id = a.service_id
                LEFT JOIN specialization sp ON sp.specialization_id = d.specialization_id
                WHERE a.patient_id = ?
                  AND a.appointment_date >= CURRENT_DATE
                  AND a.status::text NOT IN ('CANCELED', 'CANCELLED')
                ORDER BY a.appointment_date, a.appointment_time
                LIMIT 1
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new shared.dto.AppointmentDTO(
                            rs.getLong("appointment_id"),
                            rs.getLong("patient_id"),
                            null,
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getString("service_name"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getTime("appointment_time").toLocalTime(),
                            rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    public java.util.List<shared.dto.AppointmentDTO> findHistoryByPatientId(long patientId, int limit)
            throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.appointment_date,
                       a.appointment_time,
                       a.status,
                       u.first_name || ' ' || u.last_name AS doctor_name,
                       COALESCE(s.name, sp.name) AS service_name
                FROM appointment a
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN "user" u ON u.user_id = d.user_id
                LEFT JOIN medical_service s ON s.service_id = a.service_id
                LEFT JOIN specialization sp ON sp.specialization_id = d.specialization_id
                WHERE a.patient_id = ?
                  AND a.appointment_date < CURRENT_DATE
                ORDER BY a.appointment_date DESC, a.appointment_time DESC
                LIMIT ?
                """;

        java.util.List<shared.dto.AppointmentDTO> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new shared.dto.AppointmentDTO(
                            rs.getLong("appointment_id"),
                            rs.getLong("patient_id"),
                            null,
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getString("service_name"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getTime("appointment_time").toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        }
        return result;
    }

    public java.util.List<shared.dto.AppointmentDTO> findByDoctorId(long doctorId, LocalDate date)
            throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.appointment_date,
                       a.appointment_time,
                       a.status,
                       pu.first_name || ' ' || pu.last_name AS patient_name,
                       du.first_name || ' ' || du.last_name AS doctor_name,
                       COALESCE(s.name, sp.name) AS service_name
                FROM appointment a
                JOIN patient p ON p.patient_id = a.patient_id
                JOIN "user" pu ON pu.user_id = p.user_id
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN "user" du ON du.user_id = d.user_id
                LEFT JOIN medical_service s ON s.service_id = a.service_id
                LEFT JOIN specialization sp ON sp.specialization_id = d.specialization_id
                WHERE a.doctor_id = ?
                  AND a.appointment_date = ?
                ORDER BY a.appointment_time
                """;

        java.util.List<shared.dto.AppointmentDTO> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new shared.dto.AppointmentDTO(
                            rs.getLong("appointment_id"),
                            rs.getLong("patient_id"),
                            rs.getString("patient_name"),
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getString("service_name"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getTime("appointment_time").toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        }
        return result;
    }

    public java.util.List<shared.dto.AppointmentDTO> findByPatientId(long patientId) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.appointment_date,
                       a.appointment_time,
                       a.status,
                       pu.first_name || ' ' || pu.last_name AS patient_name,
                       du.first_name || ' ' || du.last_name AS doctor_name,
                       COALESCE(s.name, sp.name) AS service_name
                FROM appointment a
                JOIN patient p ON p.patient_id = a.patient_id
                JOIN "user" pu ON pu.user_id = p.user_id
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN "user" du ON du.user_id = d.user_id
                LEFT JOIN medical_service s ON s.service_id = a.service_id
                LEFT JOIN specialization sp ON sp.specialization_id = d.specialization_id
                WHERE a.patient_id = ?
                ORDER BY a.appointment_date, a.appointment_time
                """;

        java.util.List<shared.dto.AppointmentDTO> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new shared.dto.AppointmentDTO(
                            rs.getLong("appointment_id"),
                            rs.getLong("patient_id"),
                            rs.getString("patient_name"),
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getString("service_name"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getTime("appointment_time").toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        }
        return result;
    }

    public java.util.List<shared.dto.AppointmentDTO> findByDate(LocalDate date) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.appointment_date,
                       a.appointment_time,
                       a.status,
                       pu.first_name || ' ' || pu.last_name AS patient_name,
                       du.first_name || ' ' || du.last_name AS doctor_name,
                       COALESCE(s.name, sp.name) AS service_name
                FROM appointment a
                JOIN patient p ON p.patient_id = a.patient_id
                JOIN "user" pu ON pu.user_id = p.user_id
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN "user" du ON du.user_id = d.user_id
                LEFT JOIN medical_service s ON s.service_id = a.service_id
                LEFT JOIN specialization sp ON sp.specialization_id = d.specialization_id
                WHERE a.appointment_date = ?
                ORDER BY a.appointment_time
                """;

        java.util.List<shared.dto.AppointmentDTO> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new shared.dto.AppointmentDTO(
                            rs.getLong("appointment_id"),
                            rs.getLong("patient_id"),
                            rs.getString("patient_name"),
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getString("service_name"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getTime("appointment_time").toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        }
        return result;
    }

    public java.util.List<shared.dto.AppointmentDTO> findAll() throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.appointment_date,
                       a.appointment_time,
                       a.status,
                       pu.first_name || ' ' || pu.last_name AS patient_name,
                       du.first_name || ' ' || du.last_name AS doctor_name,
                       COALESCE(s.name, sp.name) AS service_name
                FROM appointment a
                JOIN patient p ON p.patient_id = a.patient_id
                JOIN "user" pu ON pu.user_id = p.user_id
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN "user" du ON du.user_id = d.user_id
                LEFT JOIN medical_service s ON s.service_id = a.service_id
                LEFT JOIN specialization sp ON sp.specialization_id = d.specialization_id
                ORDER BY a.appointment_date, a.appointment_time
                """;

        java.util.List<shared.dto.AppointmentDTO> result = new java.util.ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new shared.dto.AppointmentDTO(
                        rs.getLong("appointment_id"),
                        rs.getLong("patient_id"),
                        rs.getString("patient_name"),
                        rs.getLong("doctor_id"),
                        rs.getString("doctor_name"),
                        rs.getString("service_name"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("appointment_time").toLocalTime(),
                        rs.getString("status")
                ));
            }
        }
        return result;
    }
}
