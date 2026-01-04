package model;

import java.time.LocalTime;

public class OrarMedic {
    private int idOrar;
    private Medic medic;
    private String ziSaptamana;
    private LocalTime oraStart;
    private LocalTime oraSfarsit;

    public OrarMedic() {
    }

    public OrarMedic(int idOrar, Medic medic, String ziSaptamana, LocalTime oraStart, LocalTime oraSfarsit) {
        this.idOrar = idOrar;
        this.medic = medic;
        this.ziSaptamana = ziSaptamana;
        this.oraStart = oraStart;
        this.oraSfarsit = oraSfarsit;
    }

    public int getIdOrar() {
        return idOrar;
    }

    public void setIdOrar(int idOrar) {
        this.idOrar = idOrar;
    }

    public Medic getMedic() {
        return medic;
    }

    public void setMedic(Medic medic) {
        this.medic = medic;
    }

    public String getZiSaptamana() {
        return ziSaptamana;
    }

    public void setZiSaptamana(String ziSaptamana) {
        this.ziSaptamana = ziSaptamana;
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
}
