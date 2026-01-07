package com.hospital_management.client.presenter.patient;

import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.patient.PatientDashboardView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.AppointmentDTO;
import shared.dto.CommandDTO;
import shared.dto.PatientDashboardDTO;
import shared.dto.PatientDetailsDTO;
import shared.dto.MedicalRecordEntryDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

public class PatientDashboardPresenter {

    private final PatientDashboardView view;
    private List<AppointmentDTO> allAppointments = List.of();
    private LocalDate appointmentsFilterDate = null;
    private String appointmentsStatusCode = "ALL";

    public PatientDashboardPresenter(PatientDashboardView view) {
        this.view = view;
    }

    public void loadDashboard() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator neautentificat.");
            SceneNavigator.navigateTo(AppScene.LOGIN);
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_DASHBOARD, user.getUserId());
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se incarca datele...");

        ClientSession.getInstance().getClient().sendRequest(req, this::handleResponse);
    }

    private void handleResponse(Response response) {
        Platform.runLater(() -> {
            view.setBusy(false);

            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                return;
            }

            PatientDashboardDTO data = (PatientDashboardDTO) response.getData();
            view.renderDashboard(data);
            if (data != null && data.getProfile() != null) {
                loadPatientDetails(data.getProfile().getPatientId());
            }
            loadMedicalRecord();
            loadAppointments();
        });
    }

    public void onCancelAppointment(long appointmentId) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu există conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.CANCEL_APPOINTMENT)
                .put("appointmentId", appointmentId);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se anulează programarea...");

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() != Response.Status.OK) {
                    view.setError("Eroare anulare: " + response.getMessage());
                } else {
                    view.setInfo("Programarea a fost anulată cu succes.");
                    // reload la dashboard
                    loadDashboard();
                }
            });
        });
    }

    public void onSendFeedback(long appointmentId, int rating, String comment) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Lipsă conexiune server.");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.SEND_FEEDBACK)
                .put("appointmentId", appointmentId)
                .put("rating", rating)
                .put("comment", comment);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se trimite feedback-ul...");

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() == Response.Status.OK) {
                    view.setInfo("Mulțumim pentru feedback!");
                    // Opțional: Reîncarci dashboard-ul ca să ascunzi butonul de feedback pentru asta
                } else {
                    view.setError("Eroare: " + response.getMessage());
                }
            });
        });
    }

    private void loadPatientDetails(long patientId) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_DETAILS)
                .put("patientId", patientId);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() != Response.Status.OK) {
                    return;
                }

                PatientDetailsDTO details = response.getData() instanceof PatientDetailsDTO
                        ? (PatientDetailsDTO) response.getData()
                        : null;
                view.setPatientDetails(details);
            });
        });
    }

    private void loadMedicalRecord() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            return;
        }
        if (!ClientSession.getInstance().ensureConnected()) {
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_MY_MEDICAL_RECORD, user.getUserId());
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() != Response.Status.OK) {
                    view.renderMedicalRecordEntries(Collections.emptyList());
                    return;
                }

                @SuppressWarnings("unchecked")
                List<MedicalRecordEntryDTO> entries = response.getData() instanceof List<?>
                        ? (List<MedicalRecordEntryDTO>) response.getData()
                        : Collections.emptyList();

                view.renderMedicalRecordEntries(entries);
            });
        });
    }

    public void onClearAppointmentsFilter() {
        appointmentsFilterDate = null;
        appointmentsStatusCode = "ALL";
        view.setAppointmentsFilterDate(null);
        view.setAppointmentsStatusFilter("ALL");
        applyAppointmentsFilter();
    }

    public void onAppointmentsFilterDateSelected(LocalDate date) {
        appointmentsFilterDate = date;
        applyAppointmentsFilter();
    }

    public void onAppointmentsStatusSelected(String statusCode) {
        appointmentsStatusCode = normalizeAppointmentsStatus(statusCode);
        applyAppointmentsFilter();
    }

    private void loadAppointments() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            return;
        }
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_MY_APPOINTMENTS, user.getUserId());
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se incarca programarile...");

        ClientSession.getInstance().getClient().sendRequest(req, response ->
                Platform.runLater(() -> handleAppointmentsResponse(response)));
    }

    private void handleAppointmentsResponse(Response response) {
        view.setBusy(false);

        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare: " + response.getMessage());
            view.renderAppointments(Collections.emptyList());
            return;
        }

        @SuppressWarnings("unchecked")
        List<AppointmentDTO> received = response.getData() instanceof List<?>
                ? (List<AppointmentDTO>) response.getData()
                : Collections.emptyList();

        allAppointments = new ArrayList<>(received);
        applyAppointmentsFilter();
        view.setInfo("");
    }

    private void applyAppointmentsFilter() {
        List<AppointmentDTO> filtered = new ArrayList<>(allAppointments);
        if (appointmentsFilterDate != null) {
            filtered = filtered.stream()
                    .filter(appt -> appointmentsFilterDate.equals(appt.getDate()))
                    .toList();
        }

        if (!"ALL".equals(appointmentsStatusCode)) {
            String wantedStatus = appointmentsStatusCode;
            filtered = filtered.stream()
                    .filter(appt -> wantedStatus.equals(normalizeAppointmentsStatus(appt.getStatus())))
                    .toList();
        }

        filtered = filtered.stream()
                .sorted(Comparator.comparing(AppointmentDTO::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AppointmentDTO::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        view.renderAppointments(filtered);
    }

    private String normalizeAppointmentsStatus(String raw) {
        if (raw == null) {
            return "ALL";
        }
        String value = raw.trim().toUpperCase();
        if (value.equals("CANCELLED")) {
            return "CANCELED";
        }
        return switch (value) {
            case "ALL", "PENDING", "CONFIRMED", "DONE", "CANCELED" -> value;
            default -> "ALL";
        };
    }
}
