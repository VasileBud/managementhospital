package server.repository;

import shared.dto.DoctorDTO;
import shared.dto.DoctorScheduleDTO;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {

    // =========================================================
    // AUTH HELPERS
    // =========================================================

    /**
     * Used in LOGIN to get doctor_id for a user_id (if role is DOCTOR).
     * Returns null if not found.
     */
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

    // =========================================================
    // DOCTORS (PUBLIC LISTING)
    // =========================================================

    /**
     * Public list of doctors (name + specialization).
     */
    public List<DoctorDTO> findAllDoctors() throws SQLException {
        String sql = """
                SELECT d.doctor_id,
                       u.first_name,
                       u.last_name,
                       s.name AS specialization_name
                FROM doctor d
                JOIN "user" u ON u.user_id = d.user_id
                JOIN specialization s ON s.specialization_id = d.specialization_id
                ORDER BY u.last_name, u.first_name
                """;

        List<DoctorDTO> result = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new DoctorDTO(
                        rs.getLong("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization_name")
                ));
            }
        }

        return result;
    }

    /**
     * Find one doctor by doctor_id (name + specialization).
     * Returns null if not found.
     */
    public DoctorDTO findDoctorById(long doctorId) throws SQLException {
        String sql = """
                SELECT d.doctor_id,
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

                return new DoctorDTO(
                        rs.getLong("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization_name")
                );
            }
        }
    }

    /**
     * Optional: search doctors by name (partial match).
     */
    public List<DoctorDTO> searchDoctorsByName(String search) throws SQLException {
        String sql = """
                SELECT d.doctor_id,
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

        List<DoctorDTO> result = new ArrayList<>();
        String pattern = "%" + (search == null ? "" : search.trim()) + "%";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new DoctorDTO(
                            rs.getLong("doctor_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("specialization_name")
                    ));
                }
            }
        }

        return result;
    }

    // =========================================================
    // DOCTOR SCHEDULE
    // =========================================================

    /**
     * Returns weekly schedule entries for a doctor.
     */
    public List<DoctorScheduleDTO> findScheduleByDoctorId(long doctorId) throws SQLException {
        String sql = """
                SELECT doctor_id, day_of_week, start_time, end_time
                FROM doctor_schedule
                WHERE doctor_id = ?
                ORDER BY day_of_week, start_time
                """;

        List<DoctorScheduleDTO> result = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time st = rs.getTime("start_time");
                    Time et = rs.getTime("end_time");

                    LocalTime start = (st != null) ? st.toLocalTime() : null;
                    LocalTime end = (et != null) ? et.toLocalTime() : null;

                    result.add(new DoctorScheduleDTO(
                            rs.getLong("doctor_id"),
                            rs.getInt("day_of_week"),
                            start,
                            end
                    ));
                }
            }
        }

        return result;
    }

    /**
     * Adds one schedule interval for a doctor.
     * Returns generated schedule_id.
     */
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
                if (rs.next()) return rs.getLong("schedule_id");
            }
        }

        throw new SQLException("Failed to add doctor schedule (no schedule_id returned).");
    }

    /**
     * Updates one schedule interval (by schedule_id).
     */
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

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a schedule interval by schedule_id.
     */
    public boolean deleteSchedule(long scheduleId) throws SQLException {
        String sql = """
                DELETE FROM doctor_schedule
                WHERE schedule_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, scheduleId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes all schedule entries for a doctor.
     */
    public int deleteAllSchedulesForDoctor(long doctorId) throws SQLException {
        String sql = """
                DELETE FROM doctor_schedule
                WHERE doctor_id = ?
                """;

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, doctorId);
            return ps.executeUpdate();
        }
    }

    // =========================================================
    // ADMIN / MANAGEMENT: DOCTOR CRUD
    // =========================================================

    /**
     * Creates a doctor profile for an existing user_id.
     * Returns generated doctor_id.
     */
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
                if (rs.next()) return rs.getLong("doctor_id");
            }
        }

        throw new SQLException("Failed to create doctor (no doctor_id returned).");
    }

    /**
     * Updates doctor's specialization.
     */
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
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a doctor.
     * Note: This deletes doctor_schedule first to avoid FK issues,
     * then deletes doctor row.
     */
    public boolean deleteDoctor(long doctorId) throws SQLException {
        try (Connection conn = Repository.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // delete schedules first
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM doctor_schedule WHERE doctor_id = ?"
                )) {
                    ps1.setLong(1, doctorId);
                    ps1.executeUpdate();
                }

                // delete doctor
                int affected;
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM doctor WHERE doctor_id = ?"
                )) {
                    ps2.setLong(1, doctorId);
                    affected = ps2.executeUpdate();
                }

                conn.commit();
                return affected > 0;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
