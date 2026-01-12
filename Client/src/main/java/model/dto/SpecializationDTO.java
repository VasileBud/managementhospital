package model.dto;

import java.io.Serializable;

public class SpecializationDTO implements Serializable {
    private long specializationId;
    private String name;

    public SpecializationDTO(long specializationId, String name) {
        this.specializationId = specializationId;
        this.name = name;
    }

    public long getSpecializationId() { return specializationId; }
    public String getName() { return name; }
}
