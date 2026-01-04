package model;

public class Medic {
    private String numeMedic;
    private Utilizator utilizator;
    private Specializare specializare;

    public Medic() {
    }

    public Medic(String nume_medic, Utilizator utilizator, Specializare specializare) {
        this.numeMedic = nume_medic;
        this.utilizator = utilizator;
        this.specializare = specializare;
    }

    public String getNumeMedic() {
        return numeMedic;
    }
    public void setNumeMedic(String numeMedic) {
        this.numeMedic = numeMedic;
    }

    public Utilizator getUtilizator() {
        return utilizator;
    }

    public void setUtilizator(Utilizator utilizator) {
        this.utilizator = utilizator;
    }

    public Specializare getSpecializare() {
        return specializare;
    }

    public void setSpecializare(Specializare specializare) {
        this.specializare = specializare;
    }
}
