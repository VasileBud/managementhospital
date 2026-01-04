package model;

public class DreptAcces {
    private int idDrept;
    private Rol rol;
    private String numeDrept;

    public DreptAcces() {
    }

    public DreptAcces(int idDrept, Rol rol, String numeDrept) {
        this.idDrept = idDrept;
        this.rol = rol;
        this.numeDrept = numeDrept;
    }

    public int getIdDrept() {
        return idDrept;
    }

    public void setIdDrept(int idDrept) {
        this.idDrept = idDrept;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getNumeDrept() {
        return numeDrept;
    }

    public void setNumeDrept(String numeDrept) {
        this.numeDrept = numeDrept;
    }
}
