package model.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class CommandDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Action {

        REGISTER,
        LOGIN,
        LOGOUT,

        GET_DOCTORS,
        GET_DOCTOR_SCHEDULE,
        GET_AVAILABLE_SLOTS,
        GET_SPECIALIZATIONS,
        GET_MEDICAL_SERVICES,

        BOOK_APPOINTMENT,
        UPDATE_APPOINTMENT,
        CANCEL_APPOINTMENT,
        GET_MY_APPOINTMENTS,
        GET_PATIENT_DASHBOARD,
        GET_ALL_APPOINTMENTS,
        GET_DOCTOR_APPOINTMENTS,
        GET_PATIENT_DETAILS,
        APPROVE_APPOINTMENT,
        MARK_APPOINTMENT_DONE,
        GET_PATIENT_APPOINTMENTS,

        GET_MY_MEDICAL_RECORD,
        GET_PATIENT_MEDICAL_RECORD,
        ADD_MEDICAL_RECORD_ENTRY,

        SEND_FEEDBACK,
        GET_FEEDBACK_FOR_DOCTOR,

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
}
