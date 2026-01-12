package presenter.medic;

import app.ClientSession;
import model.dto.*;
import view.doctor.DoctorConsultationView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;

import java.util.Collections;
import java.util.List;

public class DoctorConsultationPresenter {

    private final DoctorConsultationView view;

    public DoctorConsultationPresenter(DoctorConsultationView view) {
        this.view = view;
    }

    public void loadConsultationContext() {
        AppointmentDTO appointment = ClientSession.getInstance().getSelectedAppointment();
        if (appointment == null) {
            view.setError("Selecteaza o programare din tabloul de bord.");
            return;
        }

        view.setAppointmentDetails(appointment);

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        long patientId = appointment.getPatientId();

        loadPatientDetails(patientId);

        loadMedicalHistory(patientId);

        loadAppointmentHistory(patientId);
    }

    private void loadPatientDetails(long patientId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_DETAILS).put("patientId", patientId);
        sendRequest(cmd, response -> {
            if (response.getStatus() == Response.Status.OK) {
                PatientProfileDTO details = (PatientProfileDTO) response.getData();
                view.setPatientProfile(details);
            } else {
                view.setError("Err detalii: " + response.getMessage());
            }
        });
    }

    private void loadMedicalHistory(long patientId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_MEDICAL_RECORD).put("patientId", patientId);
        sendRequest(cmd, response -> {
            if (response.getStatus() == Response.Status.OK) {
                List<MedicalRecordEntryDTO> history = castList(response.getData());
                view.updateMedicalHistory(history);
            }
            else {
                view.setError("Couldn't connect to server!");
            }
        });
    }

    private void loadAppointmentHistory(long patientId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_APPOINTMENTS).put("patientId", patientId);
        sendRequest(cmd, response -> {
            if (response.getStatus() == Response.Status.OK) {
                List<AppointmentDTO> history = castList(response.getData());
                view.updateAppointmentHistory(history);
            }
        });
    }

    public void finalizeConsultation(String diagnosis, String treatment, String notes) {
        AppointmentDTO appt = ClientSession.getInstance().getSelectedAppointment();
        if (appt == null) return;

        if (!validateMedicalRecord(diagnosis, treatment)) {
            return;
        }

        UserDTO doctor = ClientSession.getInstance().getLoggedUser();

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.ADD_MEDICAL_RECORD_ENTRY, doctor.getUserId())
                .put("patientId", appt.getPatientId())
                .put("appointmentId", appt.getAppointmentId())
                .put("diagnosis", diagnosis)
                .put("treatment", treatment)
                .put("notes", notes);

        view.setBusy(true);
        sendRequest(cmd, response -> {
            if (response.getStatus() == Response.Status.OK) {
                markAppointmentDone(appt.getAppointmentId(), doctor.getUserId());
            } else {
                Platform.runLater(() -> {
                    view.setBusy(false);
                    view.setError("Eroare salvare fișă: " + response.getMessage());
                });
            }
        });
    }

    private void markAppointmentDone(long appointmentId, long requesterId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.MARK_APPOINTMENT_DONE, requesterId)
                .put("appointmentId", appointmentId);

        sendRequest(cmd, response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() == Response.Status.OK) {
                    view.onConsultationSaved();
                } else {
                    view.setError("Fișa salvată, dar statusul programării nu s-a actualizat.");
                }
            });
        });
    }

    private void sendRequest(CommandDTO cmd, java.util.function.Consumer<Response> handler) {
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> handler.accept(response)));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castList(Object data) {
        if (data instanceof List<?>) return (List<T>) data;
        return Collections.emptyList();
    }

    private boolean validateMedicalRecord(String diagnosis, String treatment) {
        if (diagnosis == null || diagnosis.isBlank()) {
            view.setError("Diagnosticul este obligatoriu.");
            return false;
        }
        if (treatment == null || treatment.isBlank()) {
            view.setError("Tratamentul este obligatoriu.");
            return false;
        }
        if (diagnosis.length() > 2048 || treatment.length() > 2048) {
            view.setError("Textul introdus este prea lung.");
            return false;
        }
        return true;
    }
}
