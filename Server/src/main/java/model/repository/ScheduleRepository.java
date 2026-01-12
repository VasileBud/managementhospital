package model.repository;

import model.dto.DoctorScheduleDTO;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleRepository {
    private static final long CACHE_TTL_MS = Long.getLong("cache.schedule.ttl.ms", 60000L);
    private static final Map<Long, CacheEntry<List<DoctorScheduleDTO>>> CACHE_BY_DOCTOR = new ConcurrentHashMap<>();

    public List<DoctorScheduleDTO> findByDoctorId(long doctorId) throws SQLException {
        CacheEntry<List<DoctorScheduleDTO>> cached = CACHE_BY_DOCTOR.get(doctorId);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

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
        if (CACHE_TTL_MS > 0) {
            List<DoctorScheduleDTO> snapshot = List.copyOf(schedule);
            CACHE_BY_DOCTOR.put(doctorId,
                    new CacheEntry<>(snapshot, System.currentTimeMillis() + CACHE_TTL_MS));
            return snapshot;
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
        invalidateCache(doctorId);
    }

    public void deleteScheduleForDoctor(long doctorId) throws SQLException {
        String sql = "DELETE FROM doctor_schedule WHERE doctor_id = ?";
        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            ps.executeUpdate();
        }
        invalidateCache(doctorId);
    }

    private static void invalidateCache(long doctorId) {
        if (CACHE_TTL_MS <= 0) {
            return;
        }
        CACHE_BY_DOCTOR.remove(doctorId);
    }

    private static final class CacheEntry<T> {
        private final T value;
        private final long expiresAt;

        private CacheEntry(T value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
