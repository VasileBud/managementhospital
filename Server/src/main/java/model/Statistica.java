package model;

public class Statistica {
    private int idStatistica;
    private Utilizator manager;
    private String perioada;
    private int nrPacienti;
    private int nrConsultatii;
    private double gradOcupare;

    public Statistica() {
    }

    public Statistica(int idStatistica, Utilizator manager, String perioada, int nrPacienti, int nrConsultatii, double gradOcupare) {
        this.idStatistica = idStatistica;
        this.manager = manager;
        this.perioada = perioada;
        this.nrPacienti = nrPacienti;
        this.nrConsultatii = nrConsultatii;
        this.gradOcupare = gradOcupare;
    }

    public int getIdStatistica() {
        return idStatistica;
    }

    public void setIdStatistica(int idStatistica) {
        this.idStatistica = idStatistica;
    }

    public Utilizator getManager() {
        return manager;
    }

    public void setManager(Utilizator manager) {
        this.manager = manager;
    }

    public String getPerioada() {
        return perioada;
    }

    public void setPerioada(String perioada) {
        this.perioada = perioada;
    }

    public int getNrPacienti() {
        return nrPacienti;
    }

    public void setNrPacienti(int nrPacienti) {
        this.nrPacienti = nrPacienti;
    }

    public int getNrConsultatii() {
        return nrConsultatii;
    }

    public void setNrConsultatii(int nrConsultatii) {
        this.nrConsultatii = nrConsultatii;
    }

    public double getGradOcupare() {
        return gradOcupare;
    }

    public void setGradOcupare(double gradOcupare) {
        this.gradOcupare = gradOcupare;
    }
}
