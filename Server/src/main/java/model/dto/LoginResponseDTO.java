package model.dto;

import java.io.Serializable;

public class LoginResponseDTO implements Serializable {
    private long userId;
    private String role;
    private String FirstName;
    private String LastName;
    private String email;
    private Long patientId;
    private Long doctorId;

    public LoginResponseDTO(long userId, String role, String FirstName, String LastName, String email, Long patientId, Long doctorId) {
        this.userId = userId;
        this.role = role;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.email = email;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getFirstName() { return FirstName; }
    public String getLastName() { return LastName; }
    public String getEmail() { return email; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
}
