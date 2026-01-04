package dto;

import java.io.Serializable;

public class LoginResponseDTO implements Serializable {
    private long userId;
    private String role;     // PATIENT, DOCTOR, MANAGER, ADMIN
    private String fullName; // "First Last"
    private Long patientId;  // nullable
    private Long doctorId;   // nullable

    public LoginResponseDTO(long userId, String role, String fullName, Long patientId, Long doctorId) {
        this.userId = userId;
        this.role = role;
        this.fullName = fullName;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
}
