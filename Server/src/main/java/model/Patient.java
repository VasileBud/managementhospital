package model;

import java.time.LocalDate;

public class Patient {
    private long patientId;
    private long userId;
    private String nationalId; // CNP-like
    private String address;
    private String phone;
    private LocalDate birthDate;

    public Patient() {}

    public Patient(long patientId, long userId, String nationalId,
                   String address, String phone, LocalDate birthDate) {
        this.patientId = patientId;
        this.userId = userId;
        this.nationalId = nationalId;
        this.address = address;
        this.phone = phone;
        this.birthDate = birthDate;
    }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
}
