package model;

public class Rol {
    private int idRol;
    private String numeRol;

    public Rol() {
    }

    public Rol(int idRol, String numeRol) {
        this.idRol = idRol;
        this.numeRol = numeRol;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNumeRol() {
        return numeRol;
    }

    public void setNumeRol(String numeRol) {
        this.numeRol = numeRol;
    }
}
