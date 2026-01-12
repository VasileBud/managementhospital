package presenter.medic;

import app.ClientSession;
import view.doctor.DoctorDashboardView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;
import model.dto.AppointmentDTO;
import model.dto.CommandDTO;
import model.dto.DoctorDTO;
import model.dto.UserDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorDashboardPresenter {

    private final DoctorDashboardView view;

    private List<AppointmentDTO> allAppointments = new ArrayList<>();
    private List<AppointmentDTO> allPatientAppointments = new ArrayList<>();
    private String serviceFilter = "Toate";

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

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_DOCTOR_APPOINTMENTS, user.getUserId());
        if (date != null) {
            cmd.put("date", date);
        }
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
                List<AppointmentDTO> received = response.getData() instanceof List<?>
                        ? (List<AppointmentDTO>) response.getData()
                        : Collections.emptyList();

                this.allAppointments = new ArrayList<>(received);
                view.updateServiceFilterOptions(this.allAppointments);
                applyFilters();

                loadAllPatients();

                view.setInfo("");
            });
        });
    }

    private void loadAllPatients() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_DOCTOR_APPOINTMENTS, user.getUserId());
        if (user.getDoctorId() != null) {
            cmd.put("doctorId", user.getDoctorId());
        }

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() != Response.Status.OK) {
                    view.setError("Eroare: " + response.getMessage());
                    return;
                }

                @SuppressWarnings("unchecked")
                List<AppointmentDTO> received = response.getData() instanceof List<?>
                        ? (List<AppointmentDTO>) response.getData()
                        : Collections.emptyList();

                this.allPatientAppointments = new ArrayList<>(received);
                updatePatientList();
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
        this.serviceFilter = "Toate";
        LocalDate current = view.getSelectedDate();
        if (current != null) {
            view.setSelectedDate(null);
            return;
        }
        loadAppointments(null);
    }

    public void onServiceFilterChanged(String serviceName) {
        this.serviceFilter = serviceName;
        applyFilters();
    }

    private void applyFilters() {
        List<AppointmentDTO> filtered = filterByService(allAppointments);

        view.updateAppointments(filtered);
        updatePatientList();
    }

    private void updatePatientList() {
        view.updateAllPatients(filterByService(allPatientAppointments));
    }

    private List<AppointmentDTO> filterByService(List<AppointmentDTO> appointments) {
        if (serviceFilter == null || serviceFilter.isBlank() || "Toate".equalsIgnoreCase(serviceFilter)) {
            return new ArrayList<>(appointments);
        }
        return appointments.stream()
                .filter(a -> a.getServiceName() != null && a.getServiceName().equalsIgnoreCase(serviceFilter))
                .collect(Collectors.toList());
    }
}
