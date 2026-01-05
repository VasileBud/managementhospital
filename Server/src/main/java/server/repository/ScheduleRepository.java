package server.repository;

import shared.dto.DoctorScheduleDTO;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    public List<DoctorScheduleDTO> findByDoctorId(long doctorId) throws SQLException {
        String sql = """
                SELECT doctor_id, day_of_week, start_time, end_time
                FROM doctor_schedule
                WHERE doctor_id = ?
                ORDER BY day_of_week
                """;

        List<DoctorScheduleDTO> schedule = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");

                    schedule.add(new DoctorScheduleDTO(
                            rs.getLong("doctor_id"),
                            rs.getInt("day_of_week"),
                            (start != null) ? start.toLocalTime() : null,
                            (end != null) ? end.toLocalTime() : null
                    ));
                }
            }
        }
        return schedule;
    }

    public void addSchedule(long doctorId, int dayOfWeek, LocalTime start, LocalTime end) throws SQLException {
        String sql = """
                INSERT INTO doctor_schedule (doctor_id, day_of_week, start_time, end_time)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);
            ps.setInt(2, dayOfWeek);
            ps.setTime(3, Time.valueOf(start));
            ps.setTime(4, Time.valueOf(end));

            ps.executeUpdate();
        }
    }

    public void deleteScheduleForDoctor(long doctorId) throws SQLException {
        String sql = "DELETE FROM doctor_schedule WHERE doctor_id = ?";
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            ps.executeUpdate();
        }
    }
}