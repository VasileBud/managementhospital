package com.hospital_management.client.view.doctor;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.medic.DoctorDashboardPresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import shared.dto.AppointmentDTO;
import shared.dto.DoctorDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class DoctorDashboardView {

    private static final int PAGE_SIZE = 6;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label initialsLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private VBox appointmentsContainer;
    @FXML private Label paginationLabel;
    @FXML private Label statusLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private DatePicker filterDatePicker;
    @FXML private Button clearFilterButton;
    @FXML private ComboBox<String> appointmentsStatusFilter;

    private DoctorDashboardPresenter presenter;
    private List<AppointmentDTO> allAppointments = new ArrayList<>();
    private List<AppointmentDTO> filteredAppointments = new ArrayList<>();
    private int currentPage = 1;
    private LocalDate selectedDate = LocalDate.now();
    private String appointmentsStatusCode = "ALL";

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        presenter = new DoctorDashboardPresenter(this);
        statusLabel.setText("");
        renderUserHeader(ClientSession.getInstance().getLoggedUser());

        selectedDate = LocalDate.now();
        if (filterDatePicker != null) {
            filterDatePicker.setValue(selectedDate);
            filterDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    return;
                }
                selectedDate = newVal;
                presenter.loadAppointments(selectedDate);
            });
        }
        presenter.loadDoctorProfile();
        presenter.loadAppointments(selectedDate);
        setupAppointmentsStatusFilter();
    }

    @FXML
    public void onNewAppointmentClick(){
        SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING);
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    @FXML
    public void onPrevPageClick() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    public void onNextPageClick() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredAppointments.size() / (double) PAGE_SIZE));
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    public void renderUserHeader(UserDTO user) {
        if (user == null) {
            userNameLabel.setText("User");
            userRoleLabel.setText("Medic");
            initialsLabel.setText("U");
            return;
        }

        String name = user.getFullName();
        userNameLabel.setText(name == null || name.isBlank() ? "User" : name);
        userRoleLabel.setText(roleLabel(user.getRole()));
        initialsLabel.setText(initials(name));
    }

    public void setAppointments(List<AppointmentDTO> appointments) {
        this.allAppointments = appointments == null ? new ArrayList<>() : appointments;
        applyAppointmentFilters();
        updateStats();
    }

    public void setInfo(String message) {
        statusLabel.setText(message == null ? "" : message);
        statusLabel.setStyle("-fx-text-fill: #64748b;");
    }

    public void setError(String message) {
        statusLabel.setText(message == null ? "" : message);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }

    public void setBusy(boolean busy) {
        prevPageButton.setDisable(busy);
        nextPageButton.setDisable(busy);
        if (filterDatePicker != null) {
            filterDatePicker.setDisable(busy);
        }
        if (clearFilterButton != null) {
            clearFilterButton.setDisable(busy);
        }
    }

    public void updateAppointments(List<AppointmentDTO> appointments) {
        setAppointments(appointments);
    }

    public void updateDoctorDetails(UserDTO user, DoctorDTO doctorDetails) {
        renderUserHeader(user);
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate = date;
        if (filterDatePicker != null) {
            filterDatePicker.setValue(date);
        }
    }

    private void setupAppointmentsStatusFilter() {
        if (appointmentsStatusFilter == null) {
            return;
        }
        appointmentsStatusFilter.setItems(javafx.collections.FXCollections.observableArrayList(
                "Toate",
                "In asteptare",
                "Confirmate",
                "Finalizate",
                "Anulate"
        ));
        appointmentsStatusFilter.getSelectionModel().selectFirst();
        appointmentsStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            appointmentsStatusCode = statusCodeForAppointmentLabel(newVal);
            applyAppointmentFilters();
        });
    }

    private String statusCodeForAppointmentLabel(String label) {
        if (label == null) {
            return "ALL";
        }
        return switch (label.trim().toLowerCase(Locale.ROOT)) {
            case "in asteptare" -> "PENDING";
            case "confirmate" -> "CONFIRMED";
            case "finalizate" -> "DONE";
            case "anulate" -> "CANCELED";
            default -> "ALL";
        };
    }

    private void applyAppointmentFilters() {
        filteredAppointments = allAppointments.stream()
                .filter(this::matchesAppointmentsTab)
                .sorted(Comparator.comparing(AppointmentDTO::getTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        currentPage = 1;
        updatePagination();
    }

    private boolean matchesAppointmentsTab(AppointmentDTO appt) {
        if (appt == null || "ALL".equals(appointmentsStatusCode)) {
            return true;
        }
        String status = normalizedStatus(appt.getStatus());
        return status.equals(appointmentsStatusCode);
    }

    private void updatePagination() {
        appointmentsContainer.getChildren().clear();
        int total = filteredAppointments.size();
        if (total == 0) {
            Label empty = new Label("Nu exista programari pentru criteriile selectate.");
            empty.getStyleClass().add("table-text-muted");
            appointmentsContainer.getChildren().add(empty);
            paginationLabel.setText("Se afiseaza 0 rezultate");
            prevPageButton.setDisable(true);
            nextPageButton.setDisable(true);
            return;
        }

        int fromIndex = Math.min((currentPage - 1) * PAGE_SIZE, total);
        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
        List<AppointmentDTO> page = filteredAppointments.subList(fromIndex, toIndex);

        for (AppointmentDTO appt : page) {
            appointmentsContainer.getChildren().add(buildAppointmentRow(appt));
        }

        paginationLabel.setText("Se afiseaza " + (fromIndex + 1) + "-" + toIndex + " din " + total + " rezultate");

        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(toIndex >= total);
    }

    private Node buildAppointmentRow(AppointmentDTO appt) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row");

        if (isActiveAppointment(appt)) {
            row.getStyleClass().add("row-active");
        }
        if (isUrgent(appt)) {
            row.getStyleClass().add("row-urgent");
        }

        row.getColumnConstraints().addAll(appointmentsTableColumnConstraints());
        row.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(formatTime(appt.getTime()));
        timeLabel.getStyleClass().add("table-text-muted");

        HBox patientCell = buildPatientCell(appt);

        Label serviceLabel = new Label(serviceName(appt));
        serviceLabel.getStyleClass().add("table-text-muted");

        Label statusLabel = createStatusLabel(appt);
        HBox actions = createActions(appt);

        GridPane.setHgrow(patientCell, Priority.ALWAYS);
        GridPane.setHgrow(actions, Priority.ALWAYS);
        GridPane.setHalignment(actions, HPos.RIGHT);

        row.add(timeLabel, 0, 0);
        row.add(patientCell, 1, 0);
        row.add(serviceLabel, 2, 0);
        row.add(statusLabel, 3, 0);
        row.add(actions, 4, 0);

        return row;
    }

    private List<ColumnConstraints> appointmentsTableColumnConstraints() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(34);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(22);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(14);
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setPercentWidth(20);
        return List.of(c0, c1, c2, c3, c4);
    }

    private HBox buildPatientCell(AppointmentDTO appt) {
        String name = appt.getPatientName() == null ? "Pacient" : appt.getPatientName();

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("table-avatar");
        Label initials = new Label(initials(name));
        initials.getStyleClass().add("table-avatar-text");
        avatar.getChildren().add(initials);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("table-text");

        HBox cell = new HBox(8, avatar, nameLabel);
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private Label createStatusLabel(AppointmentDTO appt) {
        String status = normalizedStatus(appt.getStatus());
        Label label = new Label(statusLabel(status, appt));
        label.getStyleClass().add("status-pill");
        label.getStyleClass().add(statusStyle(status));
        return label;
    }

    private HBox createActions(AppointmentDTO appt) {
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);

        String status = normalizedStatus(appt.getStatus());
        if (Objects.equals(status, "PENDING")) {
            actions.getChildren().add(createActionButton("Confirma", "table-action",
                    () -> presenter.onApproveAppointment(appt.getAppointmentId())));
        } else if (Objects.equals(status, "CONFIRMED")) {
            actions.getChildren().add(createActionButton("Fisa", "table-action",
                    () -> openConsultation(appt)));
        } else if (Objects.equals(status, "DONE")) {
            actions.getChildren().add(createActionButton("Detalii", "table-action",
                    () -> setInfo("Detaliile consultului sunt in lucru.")));
        }

        if (isUrgent(appt) && !Objects.equals(status, "DONE")) {
            actions.getChildren().add(createActionButton("Tiaj Rapid", "secondary-button",
                    () -> setInfo("Tiaj rapid este in lucru.")));
        }

        return actions;
    }

    private Button createActionButton(String text, String styleClass, Runnable handler) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> handler.run());
        return button;
    }

    private void openConsultation(AppointmentDTO appt) {
        if (appt == null) {
            setError("Programarea selectata nu este valida.");
            return;
        }
        ClientSession session = ClientSession.getInstance();
        session.setSelectedAppointment(appt);
        session.setAppointmentToEdit(appt.getAppointmentId());
        SceneNavigator.navigateTo(AppScene.DOCTOR_CONSULTATION);
    }

    private void updateStats() {
        long total = allAppointments.stream()
                .filter(appt -> !normalizedStatus(appt.getStatus()).equals("CANCELED"))
                .count();
        long waiting = allAppointments.stream()
                .filter(appt -> normalizedStatus(appt.getStatus()).equals("PENDING"))
                .count();

        totalAppointmentsLabel.setText(String.valueOf(total));
        waitingPatientsLabel.setText(String.valueOf(waiting));
    }

    private String nextBreakTime() {
        if (selectedDate == null || !selectedDate.equals(LocalDate.now())) {
            return "-";
        }

        List<LocalTime> times = allAppointments.stream()
                .map(AppointmentDTO::getTime)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        if (times.isEmpty()) {
            return "Liber";
        }

        LocalTime now = LocalTime.now();
        LocalTime lastEnd = null;

        for (LocalTime time : times) {
            if (lastEnd == null) {
                if (time.isAfter(now.plusMinutes(30))) {
                    return now.format(timeFormatter);
                }
            } else if (time.isAfter(lastEnd.plusMinutes(30)) && lastEnd.isAfter(now)) {
                return lastEnd.format(timeFormatter);
            }
            lastEnd = time.plusMinutes(30);
        }

        if (lastEnd != null && lastEnd.isAfter(now)) {
            return lastEnd.format(timeFormatter);
        }
        return "-";
    }

    private boolean isActiveAppointment(AppointmentDTO appt) {
        if (appt.getDate() == null || appt.getTime() == null) {
            return false;
        }
        if (!appt.getDate().equals(LocalDate.now())) {
            return false;
        }
        if (!normalizedStatus(appt.getStatus()).equals("CONFIRMED")) {
            return false;
        }
        LocalTime now = LocalTime.now();
        return !now.isBefore(appt.getTime()) && now.isBefore(appt.getTime().plusMinutes(30));
    }

    private boolean isUrgent(AppointmentDTO appt) {
        String service = serviceName(appt);
        return service.toLowerCase().contains("urg");
    }

    private String formatTime(LocalTime time) {
        return time == null ? "--" : time.format(timeFormatter);
    }

    private String serviceName(AppointmentDTO appt) {
        if (appt == null) {
            return "Consultatie";
        }
        return appt.getServiceName() == null ? "Consultatie" : appt.getServiceName();
    }

    private String normalizedStatus(String raw) {
        if (raw == null) {
            return "PENDING";
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.equals("CANCELLED")) {
            return "CANCELED";
        }
        return value;
    }

    private String statusLabel(String normalized, AppointmentDTO appt) {
        if (normalized.equals("CONFIRMED") && isActiveAppointment(appt)) {
            return "In cabinet";
        }
        return switch (normalized) {
            case "PENDING" -> "In asteptare";
            case "CONFIRMED" -> "Confirmat";
            case "DONE" -> "Finalizat";
            case "CANCELED" -> "Anulat";
            default -> normalized;
        };
    }

    private String statusStyle(String normalized) {
        return switch (normalized) {
            case "PENDING" -> "status-pending";
            case "CONFIRMED" -> "status-confirmed";
            case "DONE" -> "status-done";
            case "CANCELED" -> "status-canceled";
            default -> "status-done";
        };
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "DR";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return ("" + parts[0].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT);
    }

    private String roleLabel(String role) {
        if (role == null) {
            return "Medic";
        }
        return switch (role.toUpperCase(Locale.ROOT)) {
            case "ADMIN" -> "Administrator";
            case "MANAGER" -> "Manager";
            case "DOCTOR" -> "Medic";
            default -> "Pacient";
        };
    }

    public void onClearFilterClick(ActionEvent actionEvent) {
        appointmentsStatusCode = "ALL";
        if (appointmentsStatusFilter != null) {
            appointmentsStatusFilter.getSelectionModel().selectFirst();
        }
        presenter.onClearFilter();
    }
}
