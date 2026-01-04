package repository;

import dto.ChartPointDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT COUNT(*) FROM appointment WHERE status = 'DONE'";
        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public List<ChartPointDTO> getAppointmentsBySpecialization() throws SQLException {
        String sql = """
                SELECT s.name, COUNT(a.appointment_id) as count
                FROM appointment a
                JOIN doctor d ON d.doctor_id = a.doctor_id
                JOIN specialization s ON s.specialization_id = d.specialization_id
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
}