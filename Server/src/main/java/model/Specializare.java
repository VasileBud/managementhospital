package model;

public class Specializare {
    private int idSpecializare;
    private String denumire;

    public Specializare() {
    }

    public Specializare(int idSpecializare, String denumire) {
        this.idSpecializare = idSpecializare;
        this.denumire = denumire;
    }

    public int getIdSpecializare() {
        return idSpecializare;
    }

    public void setIdSpecializare(int idSpecializare) {
        this.idSpecializare = idSpecializare;
    }

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }
}
