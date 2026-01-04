package model;

import java.time.LocalDate;

public class FisaMedicala {
    private int idFisa;
    private Programare programare;
    private String diagnostic;
    private String tratament;
    private String observatii;
    private LocalDate dataFisa;

    public FisaMedicala() {
    }

    public FisaMedicala(int idFisa, Programare programare, String diagnostic, String tratament, String observatii, LocalDate dataFisa) {
        this.idFisa = idFisa;
        this.programare = programare;
        this.diagnostic = diagnostic;
        this.tratament = tratament;
        this.observatii = observatii;
        this.dataFisa = dataFisa;
    }

    public int getIdFisa() {
        return idFisa;
    }

    public void setIdFisa(int idFisa) {
        this.idFisa = idFisa;
    }

    public Programare getProgramare() {
        return programare;
    }

    public void setProgramare(Programare programare) {
        this.programare = programare;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getTratament() {
        return tratament;
    }

    public void setTratament(String tratament) {
        this.tratament = tratament;
    }

    public String getObservatii() {
        return observatii;
    }

    public void setObservatii(String observatii) {
        this.observatii = observatii;
    }

    public LocalDate getDataFisa() {
        return dataFisa;
    }

    public void setDataFisa(LocalDate dataFisa) {
        this.dataFisa = dataFisa;
    }
}
