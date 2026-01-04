package com.hospital_management.shared.dto;

import java.io.Serializable;

public class SpecializationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String denumire;

    public SpecializationDTO() {
    }

    public SpecializationDTO(int id, String denumire) {
        this.id = id;
        this.denumire = denumire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }
}
