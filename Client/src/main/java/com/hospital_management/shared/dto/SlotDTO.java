package com.hospital_management.shared.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class SlotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int doctorId;
    private LocalDate data;
    private LocalTime oraStart;
    private LocalTime oraSfarsit;
    private boolean disponibil;

    public SlotDTO() {
    }

    public SlotDTO(int doctorId, LocalDate data, LocalTime oraStart, LocalTime oraSfarsit, boolean disponibil) {
        this.doctorId = doctorId;
        this.data = data;
        this.oraStart = oraStart;
        this.oraSfarsit = oraSfarsit;
        this.disponibil = disponibil;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOraStart() {
        return oraStart;
    }

    public void setOraStart(LocalTime oraStart) {
        this.oraStart = oraStart;
    }

    public LocalTime getOraSfarsit() {
        return oraSfarsit;
    }

    public void setOraSfarsit(LocalTime oraSfarsit) {
        this.oraSfarsit = oraSfarsit;
    }

    public boolean isDisponibil() {
        return disponibil;
    }

    public void setDisponibil(boolean disponibil) {
        this.disponibil = disponibil;
    }
}
