package model.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class CommandDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Action {
        // AUTH
        REGISTER,
        LOGIN,
        LOGOUT,

        // PUBLIC/PATIENT
        GET_DOCTORS,
        GET_DOCTOR_SCHEDULE,
        GET_AVAILABLE_SLOTS,
        GET_SPECIALIZATIONS,
        GET_MEDICAL_SERVICES,

        // APPOINTMENTS
        BOOK_APPOINTMENT,
        UPDATE_APPOINTMENT,
        CANCEL_APPOINTMENT,
        GET_MY_APPOINTMENTS,          // patient
        GET_PATIENT_DASHBOARD,        // patient dashboard summary
        GET_ALL_APPOINTMENTS,         // admin/manager: hospital-wide
        GET_DOCTOR_APPOINTMENTS,      // doctor/manager
        GET_PATIENT_DETAILS,          // doctor/manager
        APPROVE_APPOINTMENT,          // admin
        MARK_APPOINTMENT_DONE,        // doctor/manager (optional)
        GET_PATIENT_APPOINTMENTS,

        // MEDICAL RECORD
        GET_MY_MEDICAL_RECORD,        // patient
        GET_PATIENT_MEDICAL_RECORD,   // doctor/manager/admin
        ADD_MEDICAL_RECORD_ENTRY,     // doctor/manager

        // FEEDBACK
        SEND_FEEDBACK,
        GET_FEEDBACK_FOR_DOCTOR,      // optional

        // ADMIN: USERS
        ADMIN_LIST_USERS,
        ADMIN_CREATE_USER,
        ADMIN_UPDATE_USER,
        ADMIN_DELETE_USER,
        ADMIN_GET_STATS,

        GET_STATS
    }

    private Action action;

    private Long requesterUserId;

    private Map<String, Object> data = new HashMap<>();

    public CommandDTO(Action action) {
        this.action = action;
    }

    public CommandDTO(Action action, Long requesterUserId) {
        this.action = action;
        this.requesterUserId = requesterUserId;
    }

    public CommandDTO(Action action, Long requesterUserId, Map<String, Object> data) {
        this.action = action;
        this.requesterUserId = requesterUserId;
        if (data != null) this.data = data;
    }

    public Action getAction() { return action; }
    public Long getRequesterUserId() { return requesterUserId; }
    public Map<String, Object> getData() { return data; }

    public CommandDTO put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public String getString(String key) { return (String) data.get(key); }
    public LocalDate getDate(String key) { return (LocalDate) data.get(key); }
    public Long getLong(String key) { return (Long) data.get(key); }
    public Integer getInt(String key) { return (Integer) data.get(key); }
    public Boolean getBool(String key) { return (Boolean) data.get(key); }
    public LocalTime getTime(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof LocalTime) return (LocalTime) value;
        if (value instanceof String) return LocalTime.parse((String) value);
        throw new IllegalArgumentException("Value at key " + key + " is not a LocalTime");
    }
}
