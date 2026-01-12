package model.repository;

import model.ChartPoint;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsRepository {

    public long getAppointmentsCountByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM appointment WHERE appointment_date = ? AND status::text != 'CANCELED'";
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }


    public long getActiveConsultationsCount(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM appointment WHERE appointment_date = ? AND status::text IN ('CONFIRMED', 'PENDING')";
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long getTotalDoctors() {
        String sql = "SELECT COUNT(*) FROM doctor";
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<ChartPoint> getAppointmentsBySpecialization() {
        List<ChartPoint> list = new ArrayList<>();
        String sql = """
            SELECT COALESCE(s.name, sp.name, 'General') as spec_name, COUNT(a.appointment_id) as count
            FROM appointment a
            JOIN doctor d ON a.doctor_id = d.doctor_id
            LEFT JOIN specialization sp ON d.specialization_id = sp.specialization_id
            LEFT JOIN medical_service s ON a.service_id = s.service_id
            WHERE a.appointment_date = CURRENT_DATE
            GROUP BY spec_name
        """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ChartPoint(rs.getString("spec_name"), rs.getDouble("count")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Map<LocalDate, Long> getLast7DaysStats() {
        Map<LocalDate, Long> result = new HashMap<>();

        String sql = "SELECT appointment_date, COUNT(*) as cnt FROM appointment " +
                "WHERE appointment_date >= CURRENT_DATE - 7 " +
                "GROUP BY appointment_date";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate date = rs.getDate("appointment_date").toLocalDate();
                result.put(date, rs.getLong("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
