package model;

import java.time.LocalDate;

public class Utilizator {
    private int idUtilizator;
    private String nume;
    private String prenume;
    private String email;
    private String parola;
    private LocalDate dataCreare;
    private Rol rol;

    public Utilizator() {
    }

    public Utilizator(int idUtilizator, String nume, String prenume, String email, String parola, LocalDate dataCreare, Rol rol) {
        this.idUtilizator = idUtilizator;
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.parola = parola;
        this.dataCreare = dataCreare;
        this.rol = rol;
    }

    public int getIdUtilizator() {
        return idUtilizator;
    }

    public void setIdUtilizator(int idUtilizator) {
        this.idUtilizator = idUtilizator;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public LocalDate getDataCreare() {
        return dataCreare;
    }

    public void setDataCreare(LocalDate dataCreare) {
        this.dataCreare = dataCreare;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
