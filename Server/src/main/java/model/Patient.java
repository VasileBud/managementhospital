package model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class Patient {
    private long patientId;
    private long userId;
    private String nationalId;
    private String address;
    private String phone;
    private LocalDate birthDate;
    private String gender;
    private OffsetDateTime createdAt;
    private String firstName;
    private String lastName;
    private String bloodType;
    private Double weightKg;
    private Double heightCm;
    private List<String> allergies;
    private List<String> conditions;

    public Patient() {}

    public Patient(long patientId, long userId, String nationalId,
                   String address, String phone, LocalDate birthDate,
                   String gender, OffsetDateTime createdAt,
                   String firstName, String lastName,
                   String bloodType, Double weightKg, Double heightCm,
                   List<String> allergies, List<String> conditions) {
        this.patientId = patientId;
        this.userId = userId;
        this.nationalId = nationalId;
        this.address = address;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.createdAt = createdAt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bloodType = bloodType;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.allergies = allergies;
        this.conditions = conditions;
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

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
