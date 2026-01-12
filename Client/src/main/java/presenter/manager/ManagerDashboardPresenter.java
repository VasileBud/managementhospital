package presenter.manager;

import app.AppScene;
import app.SceneNavigator;
import app.ClientSession;
import view.manager.ManagerDashboardView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;
import model.dto.AppointmentDTO;
import model.dto.CommandDTO;
import model.dto.StatsDTO;
import model.dto.UserDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardPresenter {

    private final ManagerDashboardView view;
    private List<AppointmentDTO> allLoadedAppointments = new ArrayList<>();

    public ManagerDashboardPresenter(ManagerDashboardView view) {
        this.view = view;
    }

    public void loadDashboardData() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        view.updateUserInfo(user);

        loadAllAppointments(LocalDate.now(), null);
    }

    public void loadAllAppointments(LocalDate date, String serviceFilter) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Lipsă conexiune server.");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_ALL_APPOINTMENTS)
                .put("date", date);

        if (serviceFilter != null && !serviceFilter.isBlank() && !"Toate".equals(serviceFilter)) {
            cmd.put("service", serviceFilter);
        }

        view.setBusy(true);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                try {
                    if (response.getStatus() != Response.Status.OK) {
                        view.setError("Eroare server: " + response.getMessage());
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    List<AppointmentDTO> appointments = (List<AppointmentDTO>) response.getData();

                    this.allLoadedAppointments = appointments;
                    view.updateAppointmentsTable(appointments);

                    view.setBusy(false);

                } catch (Exception e) {
                    e.printStackTrace();
                    view.setError("Eroare client: " + e.getMessage());
                } finally {
                    view.setBusy(false);
                }
            });
        });
    }

    public void onFilterChanged(LocalDate date, String service) {
        loadAllAppointments(date, service);
    }

    public void updateAppointmentStatus(long appointmentId, String newStatus) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu există conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.UPDATE_APPOINTMENT)
                .put("appointmentId", appointmentId)
                .put("status", newStatus);

        view.setBusy(true);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() == Response.Status.OK) {
                    view.setInfo("Status modificat: " + newStatus);

                    loadAllAppointments(view.getSelectedDate(), view.getSelectedService());

                    view.refreshStats();

                } else {
                    view.setError("Eroare status: " + response.getMessage());
                }
            });
        });
    }

    public void updateUser(long userId, String firstName, String lastName, String email, String role, String cnp) {
        if (!ClientSession.getInstance().ensureConnected()) return;

        if (!validateUserUpdate(firstName, lastName, email, role)) {
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.ADMIN_UPDATE_USER)
                .put("userId", userId)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("email", email)
                .put("role", role)
                .put("nationalId", cnp);

        view.setBusy(true);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() == Response.Status.OK) {
                    view.setInfo("Utilizator actualizat cu succes!");
                    loadAllAppointments(view.getSelectedDate(), view.getSelectedService());
                } else {
                    view.setError("Eroare update: " + response.getMessage());
                }
            });
        });
    }

    public void onLogout() {
        ClientSession.getInstance().setLoggedUser(null);
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }
    public void loadStats() {
        if (!ClientSession.getInstance().ensureConnected()) {
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_STATS);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() == Response.Status.OK) {
                    StatsDTO stats = (StatsDTO) response.getData();
                    view.renderStats(stats);
                } else {
                    view.setError("Nu s-au putut încărca statisticile.");
                }
            });
        });
    }

    private boolean validateUserUpdate(String firstName, String lastName, String email, String role) {
        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()
                || role == null || role.isBlank()) {
            view.setError("Toate câmpurile utilizatorului sunt obligatorii.");
            return false;
        }
        if (firstName.length() > 60 || lastName.length() > 60) {
            view.setError("Numele este prea lung.");
            return false;
        }
        if (email.length() > 255 || !email.contains("@") || !email.contains(".")) {
            view.setError("Email invalid.");
            return false;
        }
        return true;
    }
}
