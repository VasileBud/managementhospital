package shared.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role; // PATIENT, DOCTOR, MANAGER, ADMIN

    // Optional profile refs for convenience
    private Long patientId;
    private Long doctorId;

    public UserDTO(long userId, String firstName, String lastName, String email, String role,
                   Long patientId, Long doctorId) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }

    public String getFullName() { return firstName + " " + lastName; }
}
