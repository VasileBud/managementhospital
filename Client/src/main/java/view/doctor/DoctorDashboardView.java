package view.doctor;

import app.AppScene;
import app.SceneNavigator;
import app.ClientSession;
import presenter.medic.DoctorDashboardPresenter;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.dto.AppointmentDTO;
import model.dto.DoctorDTO;
import model.dto.UserDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DoctorDashboardView {

    @FXML private Label doctorInitialsLabel;
    @FXML private Label doctorNameLabel;
    @FXML private Label doctorSpecializationLabel;
    @FXML private Label doctorRoleLabel;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> serviceFilterCombo;

    @FXML private VBox appointmentsContainer;
    @FXML private TextField patientSearchField;
    @FXML private ComboBox<String> patientStatusFilter;
    @FXML private VBox patientsContainer;
    @FXML private VBox patientsEmptyBox;

    @FXML private Label statusLabel;
    @FXML private VBox emptyStateBox;

    private DoctorDashboardPresenter presenter;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private boolean suppressServiceFilterActions = false;
    private List<AppointmentDTO> patientAppointmentsSource = List.of();

    @FXML
    public void initialize() {
        presenter = new DoctorDashboardPresenter(this);

        datePicker.setValue(LocalDate.now());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> presenter.loadAppointments(newVal));
        if (datePicker.getEditor() != null) {
            datePicker.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.isBlank()) {
                    if (datePicker.getValue() != null) {
                        datePicker.setValue(null);
                    }
                }
            });
        }

        serviceFilterCombo.setOnAction(e -> {
            if (suppressServiceFilterActions) {
                return;
            }
            String selected = serviceFilterCombo.getValue();
            if (selected != null) {
                presenter.onServiceFilterChanged(selected);
            }
        });

        setupPatientFilters();
        presenter.loadDoctorProfile();
        presenter.loadAppointments(LocalDate.now());
    }

    public void updateDoctorDetails(UserDTO user, DoctorDTO doctorDetails) {
        if (user == null) return;
        doctorNameLabel.setText(user.getFullName());
        doctorInitialsLabel.setText(initials(user.getFullName()));
        doctorRoleLabel.setText(roleLabel(user.getRole()));

        if (doctorSpecializationLabel != null) {
            if (doctorDetails != null) {
                doctorSpecializationLabel.setText(doctorDetails.getSpecializationName());
            } else {
                doctorSpecializationLabel.setText("-");
            }
        }
    }

    public void updateAppointments(List<AppointmentDTO> appointments) {
        appointmentsContainer.getChildren().clear();

        if (appointments == null || appointments.isEmpty()) {
            appointmentsContainer.setVisible(false);
            if (emptyStateBox != null) emptyStateBox.setVisible(true);
            return;
        }

        appointmentsContainer.setVisible(true);
        if (emptyStateBox != null) emptyStateBox.setVisible(false);

        List<AppointmentDTO> sorted = appointments.stream()
                .sorted(Comparator.comparing(AppointmentDTO::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AppointmentDTO::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        for (AppointmentDTO appt : sorted) {
            appointmentsContainer.getChildren().add(createAppointmentRow(appt));
        }
    }

    public void updateServiceFilterOptions(List<AppointmentDTO> appointments) {
        populateServiceFilter(appointments == null ? List.of() : appointments);
    }

    public void setBusy(boolean busy) {
        if (statusLabel != null) {
            statusLabel.setText(busy ? "Se lucrează..." : "");
            statusLabel.setStyle("-fx-text-fill: gray;");
        }
        if (appointmentsContainer != null) {
            appointmentsContainer.setDisable(busy);
        }
        if (patientsContainer != null) {
            patientsContainer.setDisable(busy);
        }
        if (patientSearchField != null) {
            patientSearchField.setDisable(busy);
        }
        if (patientStatusFilter != null) {
            patientStatusFilter.setDisable(busy);
        }
    }

    public void setError(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public void setInfo(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    public LocalDate getSelectedDate() {
        return datePicker.getValue();
    }

    public void setSelectedDate(LocalDate date) {
        datePicker.setValue(date);
    }

    @FXML
    public void onNewAppointmentClick(ActionEvent actionEvent) {
        SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING);
    }

    @FXML
    public void onPrevPageClick(ActionEvent event) {
    }

    @FXML
    public void onNextPageClick(ActionEvent event) {
    }

    @FXML
    public void onClearFilterClick() {
        presenter.onClearFilter();
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    private void populateServiceFilter(List<AppointmentDTO> appointments) {
        String currentSelection = serviceFilterCombo.getValue();

        List<String> services = appointments.stream()
                .map(AppointmentDTO::getServiceName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        services.add(0, "Toate");
        suppressServiceFilterActions = true;
        try {
            serviceFilterCombo.setItems(FXCollections.observableArrayList(services));

            if (currentSelection != null && services.contains(currentSelection)) {
                serviceFilterCombo.setValue(currentSelection);
            } else {
                serviceFilterCombo.setValue("Toate");
            }
        } finally {
            suppressServiceFilterActions = false;
        }
    }

    private GridPane createAppointmentRow(AppointmentDTO appt) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row");
        row.getColumnConstraints().addAll(appointmentColumnConstraints());

        Label timeLabel = new Label(formatTime(appt.getTime()));
        timeLabel.getStyleClass().add("table-text");

        Label patientLabel = new Label(valueOrDefault(appt.getPatientName(), "-"));
        patientLabel.getStyleClass().add("table-text");

        Label serviceLabel = new Label(valueOrDefault(appt.getServiceName(), "-"));
        serviceLabel.getStyleClass().add("table-text-muted");
        serviceLabel.setWrapText(true);

        Label statusBadge = new Label(translateStatus(appt.getStatus()));
        statusBadge.getStyleClass().addAll("status-pill", statusStyle(appt.getStatus()));

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if ("PENDING".equalsIgnoreCase(appt.getStatus())) {
            Button confirmBtn = new Button("Confirma");
            confirmBtn.getStyleClass().addAll("action-button", "action-primary");
            confirmBtn.setOnAction(e -> presenter.onApproveAppointment(appt.getAppointmentId()));
            actions.getChildren().add(confirmBtn);
        }

        if ("CONFIRMED".equalsIgnoreCase(appt.getStatus()) || "DONE".equalsIgnoreCase(appt.getStatus())) {
            Button consultBtn = new Button("Deschide Fisa");
            consultBtn.getStyleClass().addAll("action-button", "action-ghost");
            consultBtn.setOnAction(e -> {
                ClientSession.getInstance().setSelectedAppointment(appt);
                SceneNavigator.navigateToFresh(AppScene.DOCTOR_CONSULTATION);
            });
            actions.getChildren().add(consultBtn);
        }

        row.add(timeLabel, 0, 0);
        row.add(patientLabel, 1, 0);
        row.add(serviceLabel, 2, 0);
        row.add(statusBadge, 3, 0);
        row.add(actions, 4, 0);

        GridPane.setHgrow(patientLabel, Priority.ALWAYS);
        GridPane.setHgrow(serviceLabel, Priority.ALWAYS);
        GridPane.setHgrow(actions, Priority.ALWAYS);
        return row;
    }

    public void updateAllPatients(List<AppointmentDTO> appointments) {
        patientAppointmentsSource = appointments == null ? List.of() : List.copyOf(appointments);
        applyPatientFilters();
    }

    private void setupPatientFilters() {
        if (patientStatusFilter != null) {
            patientStatusFilter.setItems(FXCollections.observableArrayList(
                    "Toate",
                    "In asteptare",
                    "Confirmata",
                    "Finalizata"
            ));
            patientStatusFilter.setValue("Toate");
            patientStatusFilter.setOnAction(e -> applyPatientFilters());
        }
        if (patientSearchField != null) {
            patientSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyPatientFilters());
        }
    }

    private void applyPatientFilters() {
        if (patientsContainer == null) {
            return;
        }

        patientsContainer.getChildren().clear();
        Map<String, PatientSummary> patients = new LinkedHashMap<>();

        for (AppointmentDTO appt : patientAppointmentsSource) {
            if (appt == null) {
                continue;
            }
            String name = valueOrDefault(appt.getPatientName(), "Pacient");
            String key = appt.getPatientId() > 0
                    ? "id:" + appt.getPatientId()
                    : "name:" + name.toLowerCase(Locale.ROOT);
            PatientSummary summary = patients.get(key);
            if (summary == null) {
                summary = new PatientSummary(name);
                patients.put(key, summary);
            } else if ("Pacient".equals(summary.name) && !"Pacient".equals(name)) {
                summary.name = name;
            }
            summary.count++;
            if (isAfter(appt, summary.latest)) {
                summary.latest = appt;
            }
        }

        List<PatientSummary> list = patients.values().stream()
                .filter(this::matchesPatientFilters)
                .sorted(Comparator.comparing(p -> p.name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        for (PatientSummary patient : list) {
            patientsContainer.getChildren().add(createPatientRow(patient));
        }

        boolean empty = list.isEmpty();
        patientsContainer.setVisible(!empty);
        patientsContainer.setManaged(!empty);
        if (patientsEmptyBox != null) {
            patientsEmptyBox.setVisible(empty);
            patientsEmptyBox.setManaged(empty);
        }
    }

    private boolean matchesPatientFilters(PatientSummary summary) {
        String query = patientSearchField == null ? "" : patientSearchField.getText();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (!normalizedQuery.isEmpty()) {
            String name = summary.name == null ? "" : summary.name.toLowerCase(Locale.ROOT);
            if (!name.contains(normalizedQuery)) {
                return false;
            }
        }

        String statusFilter = patientStatusFilter == null ? "Toate" : patientStatusFilter.getValue();
        if (statusFilter != null && !"Toate".equalsIgnoreCase(statusFilter)) {
            String wanted = mapStatusFilter(statusFilter);
            String status = summary.latest == null ? null : summary.latest.getStatus();
            if (wanted == null || status == null || !status.equalsIgnoreCase(wanted)) {
                return false;
            }
        }
        return true;
    }

    private String mapStatusFilter(String label) {
        if (label == null) {
            return null;
        }
        return switch (label.trim().toLowerCase(Locale.ROOT)) {
            case "in asteptare" -> "PENDING";
            case "confirmata" -> "CONFIRMED";
            case "finalizata" -> "DONE";
            default -> null;
        };
    }

    private GridPane createPatientRow(PatientSummary patient) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row");
        row.getColumnConstraints().addAll(patientColumnConstraints());

        AppointmentDTO latest = patient.latest;

        Label nameLabel = new Label(valueOrDefault(patient.name, "Pacient"));
        nameLabel.getStyleClass().add("table-text");

        Label statusBadge = new Label(latest == null ? "-" : translateStatus(latest.getStatus()));
        if (latest != null && latest.getStatus() != null) {
            statusBadge.getStyleClass().addAll("status-pill", statusStyle(latest.getStatus()));
        } else {
            statusBadge.getStyleClass().add("table-text-muted");
        }
        StackPane statusWrap = new StackPane(statusBadge);
        statusWrap.setAlignment(Pos.CENTER_LEFT);
        statusWrap.getStyleClass().add("patient-status");

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("patient-actions");
        actions.setMaxWidth(Double.MAX_VALUE);
        if (latest != null && "PENDING".equalsIgnoreCase(latest.getStatus())) {
            Button confirmBtn = new Button("Confirma");
            confirmBtn.getStyleClass().addAll("action-button", "action-secondary");
            confirmBtn.setOnAction(e -> presenter.onApproveAppointment(latest.getAppointmentId()));
            actions.getChildren().add(confirmBtn);
        }
        Button dossierBtn = new Button("Vezi dosar");
        dossierBtn.getStyleClass().addAll("action-button", "action-primary");
        if (latest == null) {
            dossierBtn.setDisable(true);
        } else {
            dossierBtn.setOnAction(e -> {
                ClientSession.getInstance().setSelectedAppointment(latest);
                SceneNavigator.navigateToFresh(AppScene.DOCTOR_CONSULTATION);
            });
        }
        actions.getChildren().add(dossierBtn);

        row.add(nameLabel, 0, 0);
        row.add(statusWrap, 1, 0);
        row.add(actions, 2, 0);

        GridPane.setHgrow(nameLabel, Priority.ALWAYS);
        GridPane.setHgrow(statusWrap, Priority.ALWAYS);
        GridPane.setHgrow(actions, Priority.ALWAYS);
        return row;
    }

    private static class PatientSummary {
        private String name;
        private int count;
        private AppointmentDTO latest;

        private PatientSummary(String name) {
            this.name = name;
            this.count = 0;
        }
    }

    private List<ColumnConstraints> appointmentColumnConstraints() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(12);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(30);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(26);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(16);
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setPercentWidth(16);
        return List.of(c0, c1, c2, c3, c4);
    }

    private List<ColumnConstraints> patientColumnConstraints() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(52);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(24);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(24);
        return List.of(c0, c1, c2);
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean isAfter(AppointmentDTO candidate, AppointmentDTO current) {
        if (candidate == null) {
            return false;
        }
        if (current == null) {
            return true;
        }
        LocalDate candidateDate = candidate.getDate();
        LocalDate currentDate = current.getDate();
        if (candidateDate != null && currentDate != null) {
            int cmp = candidateDate.compareTo(currentDate);
            if (cmp != 0) {
                return cmp > 0;
            }
        } else if (candidateDate != null) {
            return true;
        } else if (currentDate != null) {
            return false;
        }

        LocalTime candidateTime = candidate.getTime();
        LocalTime currentTime = current.getTime();
        if (candidateTime != null && currentTime != null) {
            return candidateTime.isAfter(currentTime);
        }
        return candidateTime != null;
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.format(timeFormatter) : "--:--";
    }

    private String translateStatus(String status) {
        if (status == null) return "Unknown";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "În așteptare";
            case "CONFIRMED" -> "Confirmată";
            case "DONE" -> "Finalizată";
            case "CANCELED" -> "Anulată";
            default -> status;
        };
    }

    private String statusStyle(String status) {
        if (status == null) return "status-pending";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "status-pending";
            case "CONFIRMED" -> "status-confirmed";
            case "DONE" -> "status-done";
            case "CANCELED" -> "status-canceled";
            default -> "status-pending";
        };
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "DR";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return ("" + parts[0].charAt(0)).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private String roleLabel(String role) {
        if (role == null) return "Medic";
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "Administrator";
            case "MANAGER" -> "Manager";
            case "DOCTOR" -> "Medic";
            default -> "Utilizator";
        };
    }
}

