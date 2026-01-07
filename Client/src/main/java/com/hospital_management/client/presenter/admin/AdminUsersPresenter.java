package com.hospital_management.client.presenter.admin;

import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.admin.AdminDashboardView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.AdminStatsDTO;
import shared.dto.AdminUserDTO;
import shared.dto.AppointmentDTO;
import shared.dto.CommandDTO;
import shared.dto.SpecializationDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

public class AdminUsersPresenter {

    private final AdminDashboardView view;
    private List<AppointmentDTO> allAppointments = List.of();
    private LocalDate appointmentsFilterDate = null;
    private String appointmentsStatusTab = "ALL";

    public AdminUsersPresenter(AdminDashboardView view) {
        this.view = view;
    }

    public void loadStats() {
        if (!ensureConnected()) {
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.ADMIN_GET_STATS, user.getUserId())
                : new CommandDTO(CommandDTO.Action.ADMIN_GET_STATS);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handleStats(response));
        });
    }

    public void loadUsers() {
        if (!ensureConnected()) {
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.ADMIN_LIST_USERS, user.getUserId())
                : new CommandDTO(CommandDTO.Action.ADMIN_LIST_USERS);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se incarca lista utilizatorilor...");
        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handleUsers(response));
        });
    }

    public void loadAppointments() {
        if (!ensureConnected()) {
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.GET_ALL_APPOINTMENTS, user.getUserId())
                : new CommandDTO(CommandDTO.Action.GET_ALL_APPOINTMENTS);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se incarca programarile...");
        ClientSession.getInstance().getClient().sendRequest(req, response ->
                Platform.runLater(() -> handleAppointments(response)));
    }

    public void onAppointmentsFilterDateSelected(LocalDate date) {
        appointmentsFilterDate = date;
        applyAppointmentsFilter();
    }

    public void onAppointmentsTabSelected(String tab) {
        appointmentsStatusTab = normalizeAppointmentsTab(tab);
        applyAppointmentsFilter();
    }

    public void onClearAppointmentsFilter() {
        appointmentsFilterDate = null;
        appointmentsStatusTab = "ALL";
        view.setAppointmentsFilterDate(null);
        view.setAppointmentsStatusFilter("ALL");
        applyAppointmentsFilter();
    }

    public void cancelAppointment(long appointmentId) {
        if (!ensureConnected()) {
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.CANCEL_APPOINTMENT, user.getUserId())
                : new CommandDTO(CommandDTO.Action.CANCEL_APPOINTMENT);
        cmd.put("appointmentId", appointmentId);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se anuleaza programarea...");
        ClientSession.getInstance().getClient().sendRequest(req, response ->
                Platform.runLater(() -> {
                    if (response.getStatus() != Response.Status.OK) {
                        view.setError("Eroare anulare: " + response.getMessage());
                        return;
                    }
                    view.setInfo("Programarea a fost anulata.");
                    loadAppointments();
                }));
    }

    public void loadSpecializations() {
        if (!ensureConnected()) {
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_SPECIALIZATIONS);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handleSpecializations(response));
        });
    }

    public void createUser(AdminDashboardView.UserFormData data) {
        if (data == null) {
            view.setError("Datele utilizatorului sunt incomplete.");
            return;
        }
        if (!ensureConnected()) {
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.ADMIN_CREATE_USER, user.getUserId())
                : new CommandDTO(CommandDTO.Action.ADMIN_CREATE_USER);

        cmd.put("firstName", data.firstName)
                .put("lastName", data.lastName)
                .put("email", data.email)
                .put("password", data.password)
                .put("role", data.role);

        if (data.specializationId != null) {
            cmd.put("specializationId", data.specializationId);
        }
        if (data.nationalId != null && !data.nationalId.isBlank()) {
            cmd.put("nationalId", data.nationalId);
        }

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se creeaza utilizatorul...");
        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handleMutation(response, "Utilizatorul a fost creat."));
        });
    }

    public void updateUser(AdminDashboardView.UserFormData data) {
        if (data == null || data.userId == null) {
            view.setError("Datele utilizatorului sunt incomplete.");
            return;
        }
        if (!ensureConnected()) {
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.ADMIN_UPDATE_USER, user.getUserId())
                : new CommandDTO(CommandDTO.Action.ADMIN_UPDATE_USER);

        cmd.put("userId", data.userId)
                .put("firstName", data.firstName)
                .put("lastName", data.lastName)
                .put("email", data.email)
                .put("role", data.role);

        if (data.password != null && !data.password.isBlank()) {
            cmd.put("password", data.password);
        }
        if (data.specializationId != null) {
            cmd.put("specializationId", data.specializationId);
        }
        if (data.nationalId != null && !data.nationalId.isBlank()) {
            cmd.put("nationalId", data.nationalId);
        }

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se salveaza modificarile...");
        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handleMutation(response, "Utilizatorul a fost actualizat."));
        });
    }

    public void deleteUser(long userId) {
        if (!ensureConnected()) {
            return;
        }

        UserDTO user = ClientSession.getInstance().getLoggedUser();
        CommandDTO cmd = user != null
                ? new CommandDTO(CommandDTO.Action.ADMIN_DELETE_USER, user.getUserId())
                : new CommandDTO(CommandDTO.Action.ADMIN_DELETE_USER);
        cmd.put("userId", userId);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se sterge utilizatorul...");
        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handleMutation(response, "Utilizatorul a fost sters."));
        });
    }

    private void handleStats(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare statistici: " + response.getMessage());
            return;
        }
        AdminStatsDTO stats = response.getData() instanceof AdminStatsDTO
                ? (AdminStatsDTO) response.getData()
                : null;
        view.renderStats(stats);
    }

    private void handleUsers(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare lista: " + response.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")
        List<AdminUserDTO> users = response.getData() instanceof List<?>
                ? (List<AdminUserDTO>) response.getData()
                : Collections.emptyList();
        view.setUsers(users);
        view.setInfo("");
    }

    private void handleSpecializations(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Nu pot incarca specializarile: " + response.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")
        List<SpecializationDTO> items = response.getData() instanceof List<?>
                ? (List<SpecializationDTO>) response.getData()
                : Collections.emptyList();
        view.setSpecializations(items);
    }

    private void handleMutation(Response response, String successMessage) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare: " + response.getMessage());
            return;
        }

        view.setInfo(successMessage);
        loadStats();
        loadUsers();
    }

    private void handleAppointments(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare programari: " + response.getMessage());
            view.renderAppointments(Collections.emptyList());
            return;
        }

        @SuppressWarnings("unchecked")
        List<AppointmentDTO> items = response.getData() instanceof List<?>
                ? (List<AppointmentDTO>) response.getData()
                : Collections.emptyList();

        allAppointments = new ArrayList<>(items);
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

        if (!"ALL".equals(appointmentsStatusTab)) {
            String wantedStatus = appointmentsStatusTab;
            filtered = filtered.stream()
                    .filter(appt -> wantedStatus.equals(normalizeAppointmentStatus(appt.getStatus())))
                    .toList();
        }

        filtered = filtered.stream()
                .sorted(Comparator.comparing(AppointmentDTO::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AppointmentDTO::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        view.renderAppointments(filtered);
    }

    private String normalizeAppointmentsTab(String tab) {
        if (tab == null) {
            return "ALL";
        }
        String value = tab.trim().toUpperCase();
        return switch (value) {
            case "PENDING", "CONFIRMED", "DONE", "CANCELED" -> value;
            case "CANCELLED" -> "CANCELED";
            default -> "ALL";
        };
    }

    private String normalizeAppointmentStatus(String raw) {
        if (raw == null) {
            return "PENDING";
        }
        String value = raw.trim().toUpperCase();
        if (value.equals("CANCELLED")) {
            return "CANCELED";
        }
        return value;
    }

    private boolean ensureConnected() {
        if (ClientSession.getInstance().ensureConnected()) {
            return true;
        }
        view.setError("Nu exista conexiune la server!");
        return false;
    }
}
