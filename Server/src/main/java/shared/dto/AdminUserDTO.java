package shared.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class AdminUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String department;
    private Long specializationId;
    private LocalDate lastActivityDate;
    private LocalTime lastActivityTime;
    private String status;
    private String nationalId;

    public AdminUserDTO(long userId,
                        String firstName,
                        String lastName,
                        String email,
                        String role,
                        String department,
                        Long specializationId,
                        LocalDate lastActivityDate,
                        LocalTime lastActivityTime,
                        String status,
                        String nationalId) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.department = department;
        this.specializationId = specializationId;
        this.lastActivityDate = lastActivityDate;
        this.lastActivityTime = lastActivityTime;
        this.status = status;
        this.nationalId = nationalId;
    }

    public long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public Long getSpecializationId() { return specializationId; }
    public LocalDate getLastActivityDate() { return lastActivityDate; }
    public LocalTime getLastActivityTime() { return lastActivityTime; }
    public String getStatus() { return status; }
    public String getNationalId() { return nationalId; }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
