package com.hospital_management.shared.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class RegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nume;
    private String prenume;
    private String email;
    private String parola;
    private String cnp;
    private String adresa;
    private String telefon;
    private LocalDate dataNasterii;
    private String rol;

    public RegisterDTO() {
    }

    public RegisterDTO(String nume, String prenume, String email, String parola, String cnp, String adresa, String telefon, LocalDate dataNasterii, String rol) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.parola = parola;
        this.cnp = cnp;
        this.adresa = adresa;
        this.telefon = telefon;
        this.dataNasterii = dataNasterii;
        this.rol = rol;
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
