package presenter.manager;

import app.ClientSession;
import model.dto.*;
import view.manager.ManagerPatientHistoryView;
import javafx.application.Platform;
import model.common.*;
import java.util.Collections;
import java.util.List;

public class ManagerPatientHistoryPresenter {

    private final ManagerPatientHistoryView view;

    public ManagerPatientHistoryPresenter(ManagerPatientHistoryView view) {
        this.view = view;
    }

    public void loadPatientData() {
        AppointmentDTO appointment = ClientSession.getInstance().getSelectedAppointment();

        if (appointment == null) {
            view.setError("Nu exista un pacient selectat. Te rog intoarce-te la Dashboard.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Lipsa conexiune server!");
            return;
        }

        long patientId = appointment.getPatientId();

        loadPatientDetails(patientId);
        loadMedicalHistory(patientId);
        loadAppointmentHistory(patientId);
    }

    private void loadPatientDetails(long patientId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_DETAILS)
                .put("patientId", patientId);

        sendRequest(cmd, response -> {
            if (response.getStatus() == Response.Status.OK) {
                PatientProfileDTO details = (PatientProfileDTO) response.getData();
                view.setPatientProfile(details);
            } else {
                view.setError("Eroare incarcare detalii: " + response.getMessage());
            }
        });
    }

    private void loadMedicalHistory(long patientId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_MEDICAL_RECORD)
                .put("patientId", patientId);

        sendRequest(cmd, response -> {
            if (response.getStatus() == Response.Status.OK) {
                List<MedicalRecordEntryDTO> history = castList(response.getData());
                view.updateMedicalHistory(history);
            }
        });
    }

    private void loadAppointmentHistory(long patientId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_APPOINTMENTS)
                .put("patientId", patientId);

        sendRequest(cmd, response -> {
            if (response.getStatus() == Response.Status.OK) {
                List<AppointmentDTO> history = castList(response.getData());
                view.updateAppointmentHistory(history);
            }
        });
    }


    private void sendRequest(CommandDTO cmd, java.util.function.Consumer<Response> handler) {
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handler.accept(response));
        });
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castList(Object data) {
        if (data instanceof List<?>) {
            return (List<T>) data;
        }
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


    public void addMedicalRecord(String diagnosis, String treatment) {
        AppointmentDTO appointment = ClientSession.getInstance().getSelectedAppointment();
        if (appointment == null) {
            view.setError("Eroare: Nu exista programare selectata.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Lipsa conexiune server!");
            return;
        }


        CommandDTO cmd = new CommandDTO(CommandDTO.Action.ADD_MEDICAL_RECORD_ENTRY)
                .put("patientId", appointment.getPatientId())
                .put("doctorId", appointment.getDoctorId())
                .put("appointmentId", appointment.getAppointmentId())
                .put("diagnosis", diagnosis)
                .put("treatment", treatment);

        view.setInfo("Se salveaza fișa...");

            sendRequest(cmd, response -> {
                if (response.getStatus() == Response.Status.OK) {
                    view.setInfo("Fișa medicală adăugată cu succes!");
                    loadMedicalHistory(appointment.getPatientId());
                } else {
                    view.setError("Eroare la salvare: " + response.getMessage());
                }
            });
    }
}
