package com.hospital_management.client.presenter.medic;

import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.doctor.DoctorDashboardView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.AppointmentDTO;
import shared.dto.CommandDTO;
import shared.dto.DoctorDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class DoctorDashboardPresenter {

    private final DoctorDashboardView view;

    public DoctorDashboardPresenter(DoctorDashboardView view) {
        this.view = view;
    }

    public void loadDoctorProfile() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator neautentificat.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.updateDoctorDetails(user, null);
            return;
        }

        if (user.getDoctorId() == null) {
            view.updateDoctorDetails(user, null);
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_DOCTORS);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() != Response.Status.OK) {
                    view.updateDoctorDetails(user, null);
                    return;
                }

                @SuppressWarnings("unchecked")
                List<DoctorDTO> doctors = response.getData() instanceof List<?>
                        ? (List<DoctorDTO>) response.getData()
                        : Collections.emptyList();

                DoctorDTO details = doctors.stream()
                        .filter(d -> d.getDoctorId() == user.getDoctorId())
                        .findFirst()
                        .orElse(null);

                view.updateDoctorDetails(user, details);
            });
        });
    }

    public void loadAppointments(LocalDate date) {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator neautentificat.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        if (date == null) {
            view.setError("Selecteaza o data valida.");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_DOCTOR_APPOINTMENTS, user.getUserId())
                .put("date", date);
        if (user.getDoctorId() != null) {
            cmd.put("doctorId", user.getDoctorId());
        }

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se incarca programarile...");

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() != Response.Status.OK) {
                    view.setError("Eroare: " + response.getMessage());
                    return;
                }

                @SuppressWarnings("unchecked")
                List<AppointmentDTO> appointments = response.getData() instanceof List<?>
                        ? (List<AppointmentDTO>) response.getData()
                        : Collections.emptyList();

                view.updateAppointments(appointments);
                view.setInfo("");
            });
        });
    }

    public void onApproveAppointment(long appointmentId) {
        updateAppointment(CommandDTO.Action.APPROVE_APPOINTMENT, appointmentId,
                "Programarea a fost confirmata.");
    }

    public void onMarkDone(long appointmentId) {
        updateAppointment(CommandDTO.Action.MARK_APPOINTMENT_DONE, appointmentId,
                "Consultatia a fost inchisa.");
    }

    private void updateAppointment(CommandDTO.Action action, long appointmentId, String successMessage) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(action, user.getUserId())
                : new CommandDTO(action);
        cmd.put("appointmentId", appointmentId);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() != Response.Status.OK) {
                    view.setError("Eroare: " + response.getMessage());
                    return;
                }
                view.setInfo(successMessage);
                loadAppointments(view.getSelectedDate());
            });
        });
    }

    public void onClearFilter() {
        LocalDate today = LocalDate.now();
        view.setSelectedDate(today);
        loadAppointments(today);
    }
}
