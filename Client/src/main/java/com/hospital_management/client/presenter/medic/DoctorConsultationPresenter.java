package com.hospital_management.client.presenter.medic;

import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.doctor.DoctorConsultationView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.AppointmentDTO;
import shared.dto.CommandDTO;
import shared.dto.PatientDetailsDTO;
import shared.dto.UserDTO;


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

        loadPatientDetails(appointment.getPatientId());
    }

    public void finalizeConsultation() {
        AppointmentDTO appointment = ClientSession.getInstance().getSelectedAppointment();
        if (appointment == null) {
            view.setError("Nu exista programare selectata.");
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator neautentificat.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        String diagnosis = view.buildDiagnosisSummary();
        if (diagnosis.isBlank()) {
            view.setError("Completeaza diagnosticul principal.");
            return;
        }

        String treatment = view.buildTreatmentSummary();
        String notes = view.buildNotesSummary();

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.ADD_MEDICAL_RECORD_ENTRY, user.getUserId())
                .put("patientId", appointment.getPatientId())
                .put("appointmentId", appointment.getAppointmentId())
                .put("diagnosis", diagnosis)
                .put("treatment", treatment)
                .put("notes", notes);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se salveaza fisa de consultatie...");

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() != Response.Status.OK) {
                    view.setBusy(false);
                    view.setError("Eroare: " + response.getMessage());
                    return;
                }
                markAppointmentDone(appointment.getAppointmentId(), user.getUserId());
            });
        });
    }

    private void loadPatientDetails(long patientId) {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.GET_PATIENT_DETAILS, user.getUserId())
                : new CommandDTO(CommandDTO.Action.GET_PATIENT_DETAILS);
        cmd.put("patientId", patientId);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() != Response.Status.OK) {
                    view.setError("Nu pot incarca pacientul: " + response.getMessage());
                    view.setPatientDetails(null);
                    return;
                }

                PatientDetailsDTO details = response.getData() instanceof PatientDetailsDTO
                        ? (PatientDetailsDTO) response.getData()
                        : null;

                view.setPatientDetails(details);
            });
        });
    }

    private void markAppointmentDone(long appointmentId, long requesterId) {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.MARK_APPOINTMENT_DONE, requesterId)
                .put("appointmentId", appointmentId);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() != Response.Status.OK) {
                    view.setError("Fisa salvata, dar programarea nu a fost inchisa.");
                    return;
                }
                view.onConsultationSaved();
            });
        });
    }
}
