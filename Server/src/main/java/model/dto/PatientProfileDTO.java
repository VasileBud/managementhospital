package model.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class PatientProfileDTO implements Serializable {
    private long patientId;
    private String firstName;
    private String lastName;
    private String nationalId;
    private LocalDate birthDate;
    private String gender;
    private String phone;
    private String address;
    private String bloodType;
    private Double weightKg;
    private Double heightCm;
    private List<String> allergies;
    private List<String> conditions;

    public PatientProfileDTO(long patientId,
                             String firstName,
                             String lastName,
                             String nationalId,
                             LocalDate birthDate,
                             String gender,
                             String phone,
                             String address,
                             String bloodType,
                             Double weightKg,
                             Double heightCm,
                             List<String> allergies,
                             List<String> conditions) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.bloodType = bloodType;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.allergies = allergies;
        this.conditions = conditions;
    }

    public long getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getBloodType() {
        return bloodType;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
