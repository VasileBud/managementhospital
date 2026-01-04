package model;

import java.math.BigDecimal;

public class ServiciuMedical {
    private int idServiciu;
    private String denumire;
    private BigDecimal pret;

    public ServiciuMedical() {
    }

    public ServiciuMedical(int idServiciu, String denumire, BigDecimal pret) {
        this.idServiciu = idServiciu;
        this.denumire = denumire;
        this.pret = pret;
    }

    public int getIdServiciu() {
        return idServiciu;
    }

    public void setIdServiciu(int idServiciu) {
        this.idServiciu = idServiciu;
    }

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }

    public BigDecimal getPret() {
        return pret;
    }

    public void setPret(BigDecimal pret) {
        this.pret = pret;
    }
}
