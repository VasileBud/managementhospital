package presenter.admin;

import app.ClientSession;
import view.admin.AdminDashboardView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;
import model.dto.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> handleStats(response)));
    }

    public void loadManagerStats() {
        if (!ensureConnected()) {
            return;
        }
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_STATS);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> {
            if (response.getStatus() == Response.Status.OK) {
                view.renderManagerStats((StatsDTO) response.getData());
            }
        }));
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
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> handleUsers(response)));
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
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> handleAppointments(response)));
    }

    public void updateAppointmentStatus(long appointmentId, String newStatus) {
        if (!ensureConnected()) {
            return;
        }
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.UPDATE_APPOINTMENT)
                .put("appointmentId", appointmentId)
                .put("status", newStatus);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se actualizeaza statusul...");
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> {
            if (response.getStatus() == Response.Status.OK) {
                view.setInfo("Status modificat cu succes!");
                loadAppointments();
                loadManagerStats();
            } else {
                view.setError("Eroare update: " + response.getMessage());
            }
        }));
    }

    public void cancelAppointment(long appointmentId) {
        updateAppointmentStatus(appointmentId, "CANCELED");
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

    public void loadSpecializations() {
        if (!ensureConnected()) {
            return;
        }
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_SPECIALIZATIONS);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> handleSpecializations(response)));
    }

    public void createUser(AdminDashboardView.UserFormData data) {
        if (!validateUserForm(data)) {
            return;
        }
        checkAndSendUser(data, CommandDTO.Action.ADMIN_CREATE_USER, "Utilizatorul a fost creat.");
    }

    public void updateUser(AdminDashboardView.UserFormData data) {
        if (!validateUserForm(data)) {
            return;
        }
        checkAndSendUser(data, CommandDTO.Action.ADMIN_UPDATE_USER, "Utilizatorul a fost actualizat.");
    }

    public void deleteUser(long userId) {
        if (!ensureConnected()) {
            return;
        }
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.ADMIN_DELETE_USER).put("userId", userId);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);
        ClientSession.getInstance()
                .getClient()
                .sendRequest(req, response -> Platform.runLater(() -> handleMutation(response, "Utilizatorul a fost sters.")));
    }

    private void handleStats(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare: " + response.getMessage());
            return;
        }
        view.renderStats((AdminStatsDTO) response.getData());
    }

    private void handleUsers(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare: " + response.getMessage());
            return;
        }
        view.setUsers((List<AdminUserDTO>) response.getData());
        view.setInfo("");
    }

    private void handleSpecializations(Response response) {
        view.setSpecializations((List<SpecializationDTO>) response.getData());
    }

    private void handleMutation(Response response, String msg) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare: " + response.getMessage());
            return;
        }
        view.setInfo(msg);
        loadStats();
        loadUsers();
    }

    private void handleAppointments(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.renderAppointments(Collections.emptyList());
            return;
        }
        allAppointments = new ArrayList<>((List<AppointmentDTO>) response.getData());
        applyAppointmentsFilter();
        view.setInfo("");
    }

    private void checkAndSendUser(AdminDashboardView.UserFormData data, CommandDTO.Action action, String msg) {
        if (data == null) {
            return;
        }
        if (!ensureConnected()) {
            return;
        }
        CommandDTO cmd = new CommandDTO(action);
        if (data.userId != null) {
            cmd.put("userId", data.userId);
        }
        cmd.put("firstName", data.firstName)
                .put("lastName", data.lastName)
                .put("email", data.email)
                .put("role", data.role);

        if (data.password != null) {
            cmd.put("password", data.password);
        }
        if (data.specializationId != null) {
            cmd.put("specializationId", data.specializationId);
        }
        if (data.nationalId != null) {
            cmd.put("nationalId", data.nationalId);
        }

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);
        ClientSession.getInstance().getClient().sendRequest(req, response -> Platform.runLater(() -> handleMutation(response, msg)));
    }

    private boolean validateUserForm(AdminDashboardView.UserFormData data) {
        if (data == null) {
            view.setError("Date invalide.");
            return false;
        }
        if (data.firstName == null || data.firstName.isBlank()
                || data.lastName == null || data.lastName.isBlank()
                || data.email == null || data.email.isBlank()
                || data.role == null) {
            view.setError("Date invalide: toate câmpurile obligatorii trebuie completate.");
            return false;
        }
        if (data.firstName.length() > 60 || data.lastName.length() > 60) {
            view.setError("Numele este prea lung.");
            return false;
        }
        if (data.email.length() > 255 || !data.email.contains("@") || !data.email.contains(".")) {
            view.setError("Email invalid.");
            return false;
        }
        return true;
    }

    private void applyAppointmentsFilter() {
        List<AppointmentDTO> filtered = new ArrayList<>(allAppointments);
        if (appointmentsFilterDate != null) {
            filtered = filtered.stream()
                    .filter(appt -> appointmentsFilterDate.equals(appt.getDate()))
                    .toList();
        }
        if (!"ALL".equals(appointmentsStatusTab)) {
            String wanted = appointmentsStatusTab;
            filtered = filtered.stream()
                    .filter(appt -> wanted.equals(normalizeAppointmentStatus(appt.getStatus())))
                    .toList();
        }
        filtered = filtered.stream()
                .sorted(Comparator.comparing(AppointmentDTO::getDate).thenComparing(AppointmentDTO::getTime))
                .toList();
        view.renderAppointments(filtered);
    }

    private String normalizeAppointmentStatus(String raw) {
        if (raw == null) {
            return "PENDING";
        }
        String v = raw.trim().toUpperCase();
        return v.equals("CANCELLED") ? "CANCELED" : v;
    }

    private String normalizeAppointmentsTab(String tab) {
        if (tab == null) {
            return "ALL";
        }
        String v = tab.trim().toUpperCase();
        return switch (v) {
            case "PENDING", "CONFIRMED", "DONE", "CANCELED" -> v;
            case "CANCELLED" -> "CANCELED";
            default -> "ALL";
        };
    }

    private boolean ensureConnected() {
        return ClientSession.getInstance().ensureConnected();
    }
}
