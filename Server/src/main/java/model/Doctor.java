package model;

public class Doctor {
    private long doctorId;
    private long userId;
    private long specializationId;
    private String firstName;
    private String lastName;
    private String specializationName;

    public Doctor() {}

    public Doctor(long doctorId, long userId, long specializationId) {
        this.doctorId = doctorId;
        this.userId = userId;
        this.specializationId = specializationId;
    }

    public Doctor(long doctorId, long userId, long specializationId,
                  String firstName, String lastName, String specializationName) {
        this.doctorId = doctorId;
        this.userId = userId;
        this.specializationId = specializationId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specializationName = specializationName;
    }

    public long getDoctorId() { return doctorId; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getSpecializationId() { return specializationId; }
    public void setSpecializationId(long specializationId) { this.specializationId = specializationId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecializationName() { return specializationName; }
    public void setSpecializationName(String specializationName) { this.specializationName = specializationName; }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
