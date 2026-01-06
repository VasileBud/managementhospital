package server.repository;

import shared.dto.AppointmentDTO;
import shared.dto.ChartPointDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsRepository {

    public long countTotalPatients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patient";
        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public long countTotalConsultations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointment WHERE status::text = 'DONE'";
        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public long countPatientsOnDate(LocalDate date) throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT patient_id)
                FROM appointment
                WHERE appointment_date = ?
                  AND status::text NOT IN ('CANCELED', 'CANCELLED')
                """;
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    public long countActiveConsultations(LocalDate date) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM appointment
                WHERE appointment_date = ?
                  AND status::text = 'CONFIRMED'
                """;
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    public long countTotalDoctors() throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctor";
        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public long countDoctorsWithAppointments(LocalDate date) throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT doctor_id)
                FROM appointment
                WHERE appointment_date = ?
                  AND status::text NOT IN ('CANCELED', 'CANCELLED')
                """;
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    public double sumRevenueForMonth(LocalDate date) throws SQLException {
        LocalDate firstDay = date.withDayOfMonth(1);
        LocalDate firstDayNext = firstDay.plusMonths(1);
        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM invoice
                WHERE created_at >= ?
                  AND created_at < ?
                """;
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(firstDay.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(firstDayNext.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    public List<ChartPointDTO> getAppointmentsBySpecialization() throws SQLException {
        String sql = """
                SELECT s.name, COUNT(a.appointment_id) as count
                FROM appointment a
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN specialization s ON s.specialization_id = d.specialization_id
                WHERE a.status::text NOT IN ('CANCELED', 'CANCELLED')
                GROUP BY s.name
                """;

        List<ChartPointDTO> list = new ArrayList<>();
        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                list.add(new ChartPointDTO(
                        rs.getString("name"),
                        rs.getDouble("count")
                ));
            }
        }
        return list;
    }

    public List<ChartPointDTO> getAppointmentSeries(LocalDate start, LocalDate end, boolean doneOnly) throws SQLException {
        String sql = doneOnly
                ? """
                SELECT appointment_date, COUNT(*) AS count
                FROM appointment
                WHERE appointment_date BETWEEN ? AND ?
                  AND status::text = 'DONE'
                GROUP BY appointment_date
                ORDER BY appointment_date
                """
                : """
                SELECT appointment_date, COUNT(*) AS count
                FROM appointment
                WHERE appointment_date BETWEEN ? AND ?
                  AND status::text NOT IN ('CANCELED', 'CANCELLED')
                GROUP BY appointment_date
                ORDER BY appointment_date
                """;

        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            counts.put(cursor, 0L);
            cursor = cursor.plusDays(1);
        }

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date date = rs.getDate("appointment_date");
                    long count = rs.getLong("count");
                    if (date != null) {
                        counts.put(date.toLocalDate(), count);
                    }
                }
            }
        }

        List<ChartPointDTO> series = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : counts.entrySet()) {
            series.add(new ChartPointDTO(entry.getKey().toString(), entry.getValue()));
        }
        return series;
    }

    public List<ChartPointDTO> toPercentSeries(List<ChartPointDTO> raw) {
        double total = 0.0;
        for (ChartPointDTO point : raw) {
            total += point.getValue();
        }
        if (total <= 0) {
            return new ArrayList<>();
        }
        List<ChartPointDTO> result = new ArrayList<>();
        for (ChartPointDTO point : raw) {
            double percent = (point.getValue() / total) * 100.0;
            result.add(new ChartPointDTO(point.getLabel(), percent));
        }
        return result;
    }

    public List<AppointmentDTO> findRecentAppointments(int limit) throws SQLException {
        String sql = """
                SELECT a.appointment_id,
                       a.patient_id,
                       a.doctor_id,
                       a.appointment_date,
                       a.appointment_time,
                       a.status,
                       pu.first_name || ' ' || pu.last_name AS patient_name,
                       du.first_name || ' ' || du.last_name AS doctor_name,
                       sp.name AS section_name
                FROM appointment a
                JOIN patient p ON p.patient_id = a.patient_id
                JOIN "user" pu ON pu.user_id = p.user_id
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN "user" du ON du.user_id = d.user_id
                JOIN specialization sp ON sp.specialization_id = d.specialization_id
                ORDER BY a.appointment_date DESC, a.appointment_time DESC
                LIMIT ?
                """;

        List<AppointmentDTO> result = new ArrayList<>();
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date date = rs.getDate("appointment_date");
                    Time time = rs.getTime("appointment_time");
                    result.add(new AppointmentDTO(
                            rs.getLong("appointment_id"),
                            rs.getLong("patient_id"),
                            rs.getString("patient_name"),
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getString("section_name"),
                            date == null ? null : date.toLocalDate(),
                            time == null ? null : time.toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        }
        return result;
    }
}
