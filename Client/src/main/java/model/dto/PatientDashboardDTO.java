package model.dto;

import java.io.Serializable;
import java.util.List;

public class PatientDashboardDTO implements Serializable {
    private PatientProfileDTO profile;
    private AppointmentDTO nextAppointment;
    private List<AppointmentDTO> history;

    public PatientDashboardDTO(PatientProfileDTO profile,
                               AppointmentDTO nextAppointment,
                               List<AppointmentDTO> history) {
        this.profile = profile;
        this.nextAppointment = nextAppointment;
        this.history = history;
    }

    public PatientProfileDTO getProfile() {
        return profile;
    }

    public AppointmentDTO getNextAppointment() {
        return nextAppointment;
    }

    public List<AppointmentDTO> getHistory() {
        return history;
    }
}
