package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Programare {
    private int idProgramare;
    private Pacient pacient;
    private Medic medic;
    private ServiciuMedical serviciu;
    private LocalDate dataProgramare;
    private LocalTime oraProgramare;
    private String status;

    public Programare() {
    }

    public Programare(int idProgramare, Pacient pacient, Medic medic, ServiciuMedical serviciu, LocalDate dataProgramare, LocalTime oraProgramare, String status) {
        this.idProgramare = idProgramare;
        this.pacient = pacient;
        this.medic = medic;
        this.serviciu = serviciu;
        this.dataProgramare = dataProgramare;
        this.oraProgramare = oraProgramare;
        this.status = status;
    }

    public int getIdProgramare() {
        return idProgramare;
    }

    public void setIdProgramare(int idProgramare) {
        this.idProgramare = idProgramare;
    }

    public Pacient getPacient() {
        return pacient;
    }

    public void setPacient(Pacient pacient) {
        this.pacient = pacient;
    }

    public Medic getMedic() {
        return medic;
    }

    public void setMedic(Medic medic) {
        this.medic = medic;
    }

    public ServiciuMedical getServiciu() {
        return serviciu;
    }

    public void setServiciu(ServiciuMedical serviciu) {
        this.serviciu = serviciu;
    }

    public LocalDate getDataProgramare() {
        return dataProgramare;
    }

    public void setDataProgramare(LocalDate dataProgramare) {
        this.dataProgramare = dataProgramare;
    }

    public LocalTime getOraProgramare() {
        return oraProgramare;
    }

    public void setOraProgramare(LocalTime oraProgramare) {
        this.oraProgramare = oraProgramare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
