package model;

import java.time.LocalDate;

public class Pacient {
    private int idPacient;
    private Utilizator utilizator;
    private String cnp;
    private String adresa;
    private String telefon;
    private LocalDate dataNasterii;

    public Pacient() {
    }

    public Pacient(int idPacient, Utilizator utilizator, String cnp, String adresa, String telefon, LocalDate dataNasterii) {
        this.idPacient = idPacient;
        this.utilizator = utilizator;
        this.cnp = cnp;
        this.adresa = adresa;
        this.telefon = telefon;
        this.dataNasterii = dataNasterii;
    }

    public int getIdPacient() {
        return idPacient;
    }

    public void setIdPacient(int idPacient) {
        this.idPacient = idPacient;
    }

    public Utilizator getUtilizator() {
        return utilizator;
    }

    public void setUtilizator(Utilizator utilizator) {
        this.utilizator = utilizator;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public LocalDate getDataNasterii() {
        return dataNasterii;
    }

    public void setDataNasterii(LocalDate dataNasterii) {
        this.dataNasterii = dataNasterii;
    }
}
