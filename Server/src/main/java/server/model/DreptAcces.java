package server.model;

public class DreptAcces {
    private int idDrept;
    private Role rol;
    private String numeDrept;

    public DreptAcces() {
    }

    public DreptAcces(int idDrept, Role rol, String numeDrept) {
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

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    public String getNumeDrept() {
        return numeDrept;
    }

    public void setNumeDrept(String numeDrept) {
        this.numeDrept = numeDrept;
    }
}
