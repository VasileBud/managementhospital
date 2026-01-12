package model.repository;

import model.Appointment;
import model.AppointmentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

    public long createAppointment(long patientId, long doctorId, long serviceId, LocalDate date, LocalTime time, AppointmentStatus status) throws SQLException {
        String sql = """
                INSERT INTO appointment (patient_id, doctor_id, service_id, appointment_date, appointment_time, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?::appointment_status, CURRENT_TIMESTAMP)
                RETURNING appointment_id
                """;

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, patientId);
            ps.setLong(2, doctorId);
            if (serviceId > 0) ps.setLong(3, serviceId);
            else ps.setNull(3, Types.BIGINT);
            ps.setDate(4, Date.valueOf(date));
            ps.setTime(5, Time.valueOf(time));
            ps.setString(6, toDbStatus(status));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("appointment_id");
            }
        }
        throw new SQLException("Failed to create appointment");
    }

    public boolean updateAppointment(long appointmentId, long doctorId, Long serviceId, LocalDate date, LocalTime time) throws SQLException {
        String sql = """
                UPDATE appointment
                SET doctor_id = ?,
                    service_id = ?,
                    appointment_date = ?,
                    appointment_time = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE appointment_id = ?
                """;

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);
            if (serviceId != null) ps.setLong(2, serviceId);
            else ps.setNull(2, Types.BIGINT);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            ps.setLong(5, appointmentId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(long appointmentId, AppointmentStatus status) throws SQLException {
        String sql = "UPDATE appointment SET status = ?::appointment_status, updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

    public Appointment findNextByPatientId(long patientId) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAppointment(rs);
                }
            }
        }
        return null;
    }

    public List<Appointment> findHistoryByPatientId(long patientId, int limit) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAppointments(rs);
            }
        }
    }

    public List<Appointment> findByDoctorId(long doctorId, LocalDate date) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return mapAppointments(rs);
            }
        }
    }

    public List<Appointment> findByDoctorId(long doctorId) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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
                ORDER BY a.appointment_date, a.appointment_time
                """;

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAppointments(rs);
            }
        }
    }

    public List<Appointment> findByPatientId(long patientId) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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

        try (Connection conn = Repository.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAppointments(rs);
            }
        }
    }

    public List<Appointment> findByDate(LocalDate date) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return mapAppointments(rs);
            }
        }
    }

    public List<Appointment> findAll() throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapAppointments(rs);
        }
    }

    public List<Appointment> findWithFilters(Long doctorId, String serviceName) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.service_id,
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
                WHERE 1=1
                """);

        if (doctorId != null) {
            sql.append(" AND a.doctor_id = ?");
        }
        if (serviceName != null && !serviceName.isBlank() && !"Toate".equalsIgnoreCase(serviceName)) {
            sql.append(" AND (s.name ILIKE ? OR sp.name ILIKE ?)");
        }

        sql.append(" ORDER BY a.appointment_date DESC, a.appointment_time DESC");

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (doctorId != null) {
                ps.setLong(index++, doctorId);
            }
            if (serviceName != null && !serviceName.isBlank() && !"Toate".equalsIgnoreCase(serviceName)) {
                String searchPattern = "%" + serviceName + "%";
                ps.setString(index++, searchPattern);
                ps.setString(index++, searchPattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return mapAppointments(rs);
            }
        }
    }

    private List<Appointment> mapAppointments(ResultSet rs) throws SQLException {
        List<Appointment> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapAppointment(rs));
        }
        return result;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getLong("appointment_id"));
        appointment.setPatientId(rs.getLong("patient_id"));
        appointment.setDoctorId(rs.getLong("doctor_id"));
        appointment.setServiceId(getLong(meta, rs, "service_id", 0L));

        Date date = getDate(rs, "appointment_date");
        if (date != null) {
            appointment.setAppointmentDate(date.toLocalDate());
        }

        Time time = getTime(rs, "appointment_time");
        if (time != null) {
            appointment.setAppointmentTime(time.toLocalTime());
        }

        appointment.setStatus(parseStatus(safeGetString(rs, meta, "status")));
        appointment.setPatientName(safeGetString(rs, meta, "patient_name"));
        appointment.setDoctorName(safeGetString(rs, meta, "doctor_name"));
        appointment.setServiceName(safeGetString(rs, meta, "service_name"));
        return appointment;
    }

    private String safeGetString(ResultSet rs, ResultSetMetaData meta, String column) throws SQLException {
        if (!hasColumn(meta, column)) {
            return null;
        }
        return rs.getString(column);
    }

    private boolean hasColumn(ResultSetMetaData meta, String column) throws SQLException {
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            if (column.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private long getLong(ResultSetMetaData meta, ResultSet rs, String column, long fallback) throws SQLException {
        if (!hasColumn(meta, column)) {
            return fallback;
        }
        long value = rs.getLong(column);
        return rs.wasNull() ? fallback : value;
    }

    private Date getDate(ResultSet rs, String column) {
        try {
            return rs.getDate(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private Time getTime(ResultSet rs, String column) {
        try {
            return rs.getTime(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private AppointmentStatus parseStatus(String status) {
        if (status == null) {
            return null;
        }
        String normalized = status.toUpperCase();
        if ("CANCELLED".equals(normalized)) {
            normalized = "CANCELED";
        }
        try {
            return AppointmentStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
