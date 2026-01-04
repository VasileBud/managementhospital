package com.hospital_management.shared.dto;

import java.io.Serializable;

public class MedicDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idUtilizator;
    private String numeComplet;
    private SpecializationDTO specializare;

    public MedicDTO() {
    }

    public MedicDTO(int idUtilizator, String numeComplet, SpecializationDTO specializare) {
        this.idUtilizator = idUtilizator;
        this.numeComplet = numeComplet;
        this.specializare = specializare;
    }

    public int getIdUtilizator() {
        return idUtilizator;
    }

    public void setIdUtilizator(int idUtilizator) {
        this.idUtilizator = idUtilizator;
    }

    public String getNumeComplet() {
        return numeComplet;
    }

    public void setNumeComplet(String numeComplet) {
        this.numeComplet = numeComplet;
    }

    public SpecializationDTO getSpecializare() {
        return specializare;
    }

    public void setSpecializare(SpecializationDTO specializare) {
        this.specializare = specializare;
    }
}
