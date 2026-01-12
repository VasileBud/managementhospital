package app;

import presenter.HospitalClient;
import model.dto.AppointmentDTO;
import model.dto.UserDTO;

public class ClientSession {
    private static ClientSession instance;
    private HospitalClient client;
    private UserDTO loggedUser;
    private AppScene previousScene;
    private Long appointmentIdToEdit = null;
    private AppointmentDTO selectedAppointment = null;

    private ClientSession() {
        try {
            client = new HospitalClient("localhost", 5555);
        } catch (Exception e) {
        }
    }

    public static synchronized ClientSession getInstance() {
        if (instance == null) {
            instance = new ClientSession();
        }
        return instance;
    }

    public HospitalClient getClient() {
        return client;
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public synchronized boolean ensureConnected() {
        if (isConnected()) {
            return true;
        }
        try {
            client = new HospitalClient("localhost", 5555);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UserDTO getLoggedUser() { return loggedUser; }
    public void setLoggedUser(UserDTO loggedUser) { this.loggedUser = loggedUser; }

    public void setAppointmentToEdit(Long id) {
        this.appointmentIdToEdit = id;
    }

    public Long getAppointmentToEdit() {
        return appointmentIdToEdit;
    }

    public void clearEditMode() {
        this.appointmentIdToEdit = null;
    }

    public void setSelectedAppointment(AppointmentDTO appointment) {
        this.selectedAppointment = appointment;
    }

    public AppointmentDTO getSelectedAppointment() {
        return selectedAppointment;
    }

    public void clearSelectedAppointment() {
        this.selectedAppointment = null;
    }

    public void setPreviousScene(AppScene scene) { this.previousScene = scene; }

    public AppScene getPreviousScene() { return previousScene; }
}
