package dto;

import java.io.Serializable;

public class DoctorDTO implements Serializable {
    private long doctorId;
    private String firstName;
    private String lastName;
    private String specializationName;

    public DoctorDTO(long doctorId, String firstName, String lastName, String specializationName) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specializationName = specializationName;
    }

    public long getDoctorId() { return doctorId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecializationName() { return specializationName; }

    public String getFullName() { return firstName + " " + lastName; }
}
