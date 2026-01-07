package server.repository;

import shared.dto.AdminUserDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminRepository {

    public long countDoctors() throws SQLException {
        return countSimple("SELECT COUNT(*) FROM doctor");
    }

    public long countPatients() throws SQLException {
        return countSimple("SELECT COUNT(*) FROM patient");
    }

    public long countUsers() throws SQLException {
        return countSimple("SELECT COUNT(*) FROM \"user\"");
    }

    public long countDoctorsBefore(LocalDate date) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM doctor d
                JOIN "user" u ON u.user_id = d.user_id
                WHERE u.created_at < ?
                """;
        return countBefore(sql, date);
    }

    public long countPatientsBefore(LocalDate date) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM patient p
                JOIN "user" u ON u.user_id = p.user_id
                WHERE u.created_at < ?
                """;
        return countBefore(sql, date);
    }

    public long countUsersBefore(LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE created_at < ?";
        return countBefore(sql, date);
    }

    public long countUsersOnDate(LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE created_at >= ? AND created_at < ?";
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public List<AdminUserDTO> findAdminUsers() throws SQLException {
        String sql = """
                SELECT u.user_id,
                       u.first_name,
                       u.last_name,
                       u.email,
                       u.created_at,
                       r.role_name,
                       d.doctor_id,
                       s.specialization_id,
                       s.name AS specialization_name,
                       p.patient_id,
                       p.national_id,
                       last_appt.last_date,
                       last_appt.last_time,
                       next_appt.next_date,
                       next_appt.next_time,
                       CASE
                           WHEN d.doctor_id IS NULL THEN FALSE
                           WHEN EXISTS (SELECT 1 FROM doctor_schedule ds WHERE ds.doctor_id = d.doctor_id) THEN TRUE
                           ELSE FALSE
                       END AS has_schedule
                FROM "user" u
                JOIN role r ON r.role_id = u.role_id
                LEFT JOIN doctor d ON d.user_id = u.user_id
                LEFT JOIN specialization s ON s.specialization_id = d.specialization_id
                LEFT JOIN patient p ON p.user_id = u.user_id
                LEFT JOIN LATERAL (
                    SELECT a.appointment_date AS last_date,
                           a.appointment_time AS last_time
                    FROM appointment a
                    WHERE (d.doctor_id IS NOT NULL AND a.doctor_id = d.doctor_id)
                       OR (p.patient_id IS NOT NULL AND a.patient_id = p.patient_id)
                    ORDER BY a.appointment_date DESC, a.appointment_time DESC
                    LIMIT 1
                ) last_appt ON true
                LEFT JOIN LATERAL (
                    SELECT a.appointment_date AS next_date,
                           a.appointment_time AS next_time
                    FROM appointment a
                    WHERE ((d.doctor_id IS NOT NULL AND a.doctor_id = d.doctor_id)
                       OR (p.patient_id IS NOT NULL AND a.patient_id = p.patient_id))
                      AND a.appointment_date >= CURRENT_DATE
                      AND a.status::text NOT IN ('CANCELED', 'CANCELLED')
                    ORDER BY a.appointment_date ASC, a.appointment_time ASC
                    LIMIT 1
                ) next_appt ON true
                ORDER BY u.user_id
                """;

        List<AdminUserDTO> result = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String role = rs.getString("role_name");
                OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
                LocalDate lastDate = rs.getObject("last_date", LocalDate.class);
                LocalTime lastTime = rs.getObject("last_time", LocalTime.class);
                LocalDate nextDate = rs.getObject("next_date", LocalDate.class);

                if (lastDate == null && createdAt != null) {
                    lastDate = createdAt.toLocalDate();
                    lastTime = createdAt.toLocalTime();
                }

                boolean hasSchedule = rs.getBoolean("has_schedule");
                String status = computeStatus(role, lastDate, nextDate, hasSchedule);

                String department = roleLabel(role);
                Long specializationId = rs.getObject("specialization_id", Long.class);
                String specializationName = rs.getString("specialization_name");
                if ("DOCTOR".equalsIgnoreCase(role) && specializationName != null) {
                    department = specializationName;
                } else if ("PATIENT".equalsIgnoreCase(role)) {
                    department = "Pacient";
                }

                result.add(new AdminUserDTO(
                        rs.getLong("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        role,
                        department,
                        specializationId,
                        lastDate,
                        lastTime,
                        status,
                        rs.getString("national_id")
                ));
            }
        }
        return result;
    }

    public boolean hasAppointmentsForDoctor(long doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointment WHERE doctor_id = ?";
        return countExists(sql, doctorId);
    }

    public boolean hasAppointmentsForPatient(long patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointment WHERE patient_id = ?";
        return countExists(sql, patientId);
    }

    private long countSimple(String sql) throws SQLException {
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    private long countBefore(String sql, LocalDate date) throws SQLException {
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    private boolean countExists(String sql, long id) throws SQLException {
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        }
        return false;
    }

    private String roleLabel(String role) {
        if (role == null) {
            return "Cont";
        }
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "Administrator";
            case "MANAGER" -> "Manager";
            case "DOCTOR" -> "Medic";
            case "PATIENT" -> "Pacient";
            default -> role;
        };
    }

    private String computeStatus(String role, LocalDate lastActivity, LocalDate nextActivity, boolean hasSchedule) {
        if (role == null) {
            return "ACTIVE";
        }
        String normalized = role.toUpperCase();
        if ("DOCTOR".equals(normalized)) {
            if (nextActivity != null) {
                return "ACTIVE";
            }
            if (!hasSchedule) {
                return "ON_LEAVE";
            }
            return "INACTIVE";
        }

        if ("PATIENT".equals(normalized)) {
            if (nextActivity != null) {
                return "ACTIVE";
            }
            if (lastActivity != null && lastActivity.isAfter(LocalDate.now().minusDays(90))) {
                return "ACTIVE";
            }
            return "INACTIVE";
        }

        return "ACTIVE";
    }
}
