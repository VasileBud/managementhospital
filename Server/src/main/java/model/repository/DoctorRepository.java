package model.repository;

import model.Doctor;
import model.DoctorSchedule;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoctorRepository {
    private static final long SCHEDULE_CACHE_TTL_MS = Long.getLong("cache.schedule.ttl.ms", 60000L);
    private static final Map<Long, CacheEntry<List<DoctorSchedule>>> SCHEDULE_CACHE = new ConcurrentHashMap<>();
    private static final long DOCTOR_LIST_CACHE_TTL_MS = Long.getLong("cache.doctors.ttl.ms", 60000L);
    private static volatile CacheEntry<List<Doctor>> DOCTOR_LIST_CACHE;
    private static final Map<Long, CacheEntry<Doctor>> DOCTOR_BY_ID_CACHE = new ConcurrentHashMap<>();

    public Long findDoctorIdByUserId(long userId) throws SQLException {
        String sql = """
                SELECT doctor_id
                FROM doctor
                WHERE user_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("doctor_id");
            }
        }
        return null;
    }


    public List<Doctor> findAllDoctors() throws SQLException {
        CacheEntry<List<Doctor>> cached = DOCTOR_LIST_CACHE;
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        String sql = """
                SELECT d.doctor_id,
                       d.user_id,
                       d.specialization_id,
                       u.first_name,
                       u.last_name,
                       s.name AS specialization_name
                FROM doctor d
                JOIN "user" u ON u.user_id = d.user_id
                JOIN specialization s ON s.specialization_id = d.specialization_id
                ORDER BY u.last_name, u.first_name
                """;

        List<Doctor> result = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new Doctor(
                        rs.getLong("doctor_id"),
                        rs.getLong("user_id"),
                        rs.getLong("specialization_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization_name")
                ));
            }
        }

        if (DOCTOR_LIST_CACHE_TTL_MS > 0) {
            List<Doctor> snapshot = List.copyOf(result);
            long expiresAt = System.currentTimeMillis() + DOCTOR_LIST_CACHE_TTL_MS;
            DOCTOR_LIST_CACHE = new CacheEntry<>(snapshot, expiresAt);
            for (Doctor doctor : snapshot) {
                DOCTOR_BY_ID_CACHE.put(doctor.getDoctorId(), new CacheEntry<>(doctor, expiresAt));
            }
            return snapshot;
        }

        return result;
    }

    public Doctor findDoctorById(long doctorId) throws SQLException {
        CacheEntry<Doctor> cached = DOCTOR_BY_ID_CACHE.get(doctorId);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        CacheEntry<List<Doctor>> listCache = DOCTOR_LIST_CACHE;
        if (listCache != null && !listCache.isExpired()) {
            for (Doctor doc : listCache.value) {
                if (doc.getDoctorId() == doctorId) {
                    DOCTOR_BY_ID_CACHE.put(doctorId, new CacheEntry<>(doc, listCache.expiresAt));
                    return doc;
                }
            }
            return null;
        }

        String sql = """
                SELECT d.doctor_id,
                       d.user_id,
                       d.specialization_id,
                       u.first_name,
                       u.last_name,
                       s.name AS specialization_name
                FROM doctor d
                JOIN "user" u ON u.user_id = d.user_id
                JOIN specialization s ON s.specialization_id = d.specialization_id
                WHERE d.doctor_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Doctor doctor = new Doctor(
                        rs.getLong("doctor_id"),
                        rs.getLong("user_id"),
                        rs.getLong("specialization_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization_name")
                );
                if (DOCTOR_LIST_CACHE_TTL_MS > 0) {
                    DOCTOR_BY_ID_CACHE.put(doctorId, new CacheEntry<>(doctor, System.currentTimeMillis() + DOCTOR_LIST_CACHE_TTL_MS));
                }
                return doctor;
            }
        }
    }

    public List<Doctor> searchDoctorsByName(String search) throws SQLException {
        CacheEntry<List<Doctor>> cachedList = DOCTOR_LIST_CACHE;
        if (cachedList != null && !cachedList.isExpired()) {
            List<Doctor> result = new ArrayList<>();
            String lower = search == null ? "" : search.trim().toLowerCase();
            for (Doctor doc : cachedList.value) {
                String first = doc.getFirstName() == null ? "" : doc.getFirstName().toLowerCase();
                String last = doc.getLastName() == null ? "" : doc.getLastName().toLowerCase();
                if (first.contains(lower) || last.contains(lower)) {
                    result.add(doc);
                }
            }
            return result;
        }

        String sql = """
                SELECT d.doctor_id,
                       d.user_id,
                       d.specialization_id,
                       u.first_name,
                       u.last_name,
                       s.name AS specialization_name
                FROM doctor d
                JOIN "user" u ON u.user_id = d.user_id
                JOIN specialization s ON s.specialization_id = d.specialization_id
                WHERE LOWER(u.first_name) LIKE LOWER(?)
                   OR LOWER(u.last_name) LIKE LOWER(?)
                ORDER BY u.last_name, u.first_name
                """;

        List<Doctor> result = new ArrayList<>();
        String pattern = "%" + (search == null ? "" : search.trim()) + "%";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Doctor(
                            rs.getLong("doctor_id"),
                            rs.getLong("user_id"),
                            rs.getLong("specialization_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("specialization_name")
                    ));
                }
            }
        }

        return result;
    }


    public List<DoctorSchedule> findScheduleByDoctorId(long doctorId) throws SQLException {
        CacheEntry<List<DoctorSchedule>> cached = SCHEDULE_CACHE.get(doctorId);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        String sql = """
                SELECT schedule_id, doctor_id, day_of_week, start_time, end_time
                FROM doctor_schedule
                WHERE doctor_id = ?
                ORDER BY day_of_week, start_time
                """;

        List<DoctorSchedule> result = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time st = rs.getTime("start_time");
                    Time et = rs.getTime("end_time");

                    LocalTime start = (st != null) ? st.toLocalTime() : null;
                    LocalTime end = (et != null) ? et.toLocalTime() : null;

                    result.add(new DoctorSchedule(
                            rs.getLong("schedule_id"),
                            rs.getLong("doctor_id"),
                            rs.getInt("day_of_week"),
                            start,
                            end
                    ));
                }
            }
        }

        if (SCHEDULE_CACHE_TTL_MS > 0) {
            List<DoctorSchedule> snapshot = List.copyOf(result);
            SCHEDULE_CACHE.put(doctorId, new CacheEntry<>(snapshot, System.currentTimeMillis() + SCHEDULE_CACHE_TTL_MS));
            return snapshot;
        }
        return result;
    }

    public long addSchedule(long doctorId, int dayOfWeek, LocalTime startTime, LocalTime endTime) throws SQLException {
        String sql = """
                INSERT INTO doctor_schedule (doctor_id, day_of_week, start_time, end_time)
                VALUES (?, ?, ?, ?)
                RETURNING schedule_id
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);
            ps.setInt(2, dayOfWeek);
            ps.setTime(3, Time.valueOf(startTime));
            ps.setTime(4, Time.valueOf(endTime));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    invalidateScheduleCache(doctorId);
                    return rs.getLong("schedule_id");
                }
            }
        }

        throw new SQLException("Failed to add doctor schedule (no schedule_id returned).");
    }

    public boolean updateSchedule(long scheduleId, int dayOfWeek, LocalTime startTime, LocalTime endTime) throws SQLException {
        String sql = """
                UPDATE doctor_schedule
                SET day_of_week = ?, start_time = ?, end_time = ?
                WHERE schedule_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dayOfWeek);
            ps.setTime(2, Time.valueOf(startTime));
            ps.setTime(3, Time.valueOf(endTime));
            ps.setLong(4, scheduleId);

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                clearScheduleCache();
            }
            return updated;
        }
    }

    public boolean deleteSchedule(long scheduleId) throws SQLException {
        String sql = """
                DELETE FROM doctor_schedule
                WHERE schedule_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, scheduleId);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                clearScheduleCache();
            }
            return deleted;
        }
    }

    public int deleteAllSchedulesForDoctor(long doctorId) throws SQLException {
        String sql = """
                DELETE FROM doctor_schedule
                WHERE doctor_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                invalidateScheduleCache(doctorId);
            }
            return deleted;
        }
    }


    public long createDoctor(long userId, long specializationId) throws SQLException {
        String sql = """
                INSERT INTO doctor (user_id, specialization_id)
                VALUES (?, ?)
                RETURNING doctor_id
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, specializationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    invalidateDoctorCaches();
                    return rs.getLong("doctor_id");
                }
            }
        }

        throw new SQLException("Failed to create doctor (no doctor_id returned).");
    }


    public boolean updateDoctorSpecialization(long doctorId, long specializationId) throws SQLException {
        String sql = """
                UPDATE doctor
                SET specialization_id = ?
                WHERE doctor_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, specializationId);
            ps.setLong(2, doctorId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                invalidateDoctorCaches();
            }
            return updated;
        }
    }

    public boolean deleteDoctor(long doctorId) throws SQLException {
        try (Connection conn = Repository.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM doctor_schedule WHERE doctor_id = ?"
                )) {
                    ps1.setLong(1, doctorId);
                    ps1.executeUpdate();
                }

                int affected;
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM doctor WHERE doctor_id = ?"
                )) {
                    ps2.setLong(1, doctorId);
                    affected = ps2.executeUpdate();
                }

                conn.commit();
                invalidateDoctorCaches();
                invalidateScheduleCache(doctorId);
                return affected > 0;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void invalidateScheduleCache(long doctorId) {
        if (SCHEDULE_CACHE_TTL_MS <= 0) {
            return;
        }
        SCHEDULE_CACHE.remove(doctorId);
    }

    private static void clearScheduleCache() {
        if (SCHEDULE_CACHE_TTL_MS <= 0) {
            return;
        }
        SCHEDULE_CACHE.clear();
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

    private static void invalidateDoctorCaches() {
        if (DOCTOR_LIST_CACHE_TTL_MS <= 0) {
            return;
        }
        DOCTOR_LIST_CACHE = null;
        DOCTOR_BY_ID_CACHE.clear();
    }
}
