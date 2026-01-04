package com.hospital_management.shared.dto;

import java.io.Serializable;

public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String email;
    private String parola;

    public LoginDTO() {
    }

    public LoginDTO(String email, String parola) {
        this.email = email;
        this.parola = parola;
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
}
