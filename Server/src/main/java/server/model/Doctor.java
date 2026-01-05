package server.model;

public class Doctor {
    private long doctorId;
    private long userId;
    private long specializationId;

    public Doctor() {}

    public Doctor(long doctorId, long userId, long specializationId) {
        this.doctorId = doctorId;
        this.userId = userId;
        this.specializationId = specializationId;
    }

    public long getDoctorId() { return doctorId; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getSpecializationId() { return specializationId; }
    public void setSpecializationId(long specializationId) { this.specializationId = specializationId; }
}
