package shared.dto;

import java.io.Serializable;
import java.util.List;

public class PatientProfileDTO implements Serializable {
    private long patientId;
    private String bloodType;
    private Double weightKg;
    private Double heightCm;
    private List<String> allergies;
    private List<String> conditions;

    public PatientProfileDTO(long patientId,
                             String bloodType,
                             Double weightKg,
                             Double heightCm,
                             List<String> allergies,
                             List<String> conditions) {
        this.patientId = patientId;
        this.bloodType = bloodType;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.allergies = allergies;
        this.conditions = conditions;
    }

    public long getPatientId() {
        return patientId;
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
}
