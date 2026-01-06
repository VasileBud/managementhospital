package com.hospital_management.client.view.doctor;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.medic.DoctorDashboardPresenter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import shared.dto.AppointmentDTO;
import shared.dto.DoctorDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @FXML private Label doctorNameLabel;
    @FXML private Label doctorSpecializationLabel;
    @FXML private Label doctorInitialsLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private Label nextBreakLabel;
    @FXML private VBox appointmentsContainer;
    @FXML private Label paginationLabel;
    @FXML private Label statusLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;

    private DoctorDashboardPresenter presenter;
    private List<AppointmentDTO> allAppointments = new ArrayList<>();
    private List<AppointmentDTO> filteredAppointments = new ArrayList<>();
    private int currentPage = 1;
    private LocalDate selectedDate = LocalDate.now();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMMM yyyy | HH:mm", Locale.forLanguageTag("ro"));

    @FXML
    public void initialize() {
        presenter = new DoctorDashboardPresenter(this);
        statusLabel.setText("");
        renderUserHeader(ClientSession.getInstance().getLoggedUser(), null);
        startClock();

        selectedDate = LocalDate.now();
        presenter.loadDoctorProfile();
        presenter.loadAppointments(selectedDate);
    }

    private void startClock() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> updateDateTimeLabel()),
                new KeyFrame(Duration.minutes(1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateDateTimeLabel() {
        dateTimeLabel.setText(LocalDateTime.now().format(dateTimeFormatter));
    }

    @FXML
    public void onNavDashboardClick() {
        SceneNavigator.navigateToFresh(AppScene.DOCTOR_DASHBOARD);
    }

    @FXML
    public void onNavAppointmentsClick() {
        SceneNavigator.navigateTo(AppScene.DOCTOR_CONSULTATION);
    }

    @FXML
    public void onNavPatientsClick() {
        setInfo("Sectiunea Pacienti este in lucru.");
    }

    @FXML
    public void onNavReportsClick() {
        setInfo("Sectiunea Rapoarte este in lucru.");
    }

    @FXML
    public void onNavHistoryClick() {
        setInfo("Sectiunea Istoric Medical este in lucru.");
    }

    @FXML
    public void onNavSettingsClick() {
        setInfo("Sectiunea Setari este in lucru.");
    }

    @FXML
    public void onNotificationsClick() {
        setInfo("Nu exista notificari noi.");
    }

    @FXML
    public void onNewAppointmentClick() {
        setInfo("Crearea de programari din contul de medic este in lucru.");
    }

    @FXML
    public void onViewCalendarClick() {
        setInfo("Calendarul complet este in lucru.");
    }

    @FXML
    public void onCreateNoteClick() {
        setInfo("Nota rapida este in lucru.");
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
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

    public void renderUserHeader(UserDTO user, DoctorDTO doctorDetails) {
        if (user == null) {
            doctorNameLabel.setText("Doctor");
            doctorSpecializationLabel.setText("Specializare");
            doctorInitialsLabel.setText("DR");
            return;
        }
        String fullName = user.getFullName();
        doctorNameLabel.setText(fullName == null ? "Doctor" : fullName);
        doctorInitialsLabel.setText(initials(fullName));
        if (doctorDetails != null && doctorDetails.getSpecializationName() != null) {
            doctorSpecializationLabel.setText(doctorDetails.getSpecializationName());
        } else {
            doctorSpecializationLabel.setText("Medic");
        }
    }

    public void setAppointments(List<AppointmentDTO> appointments) {
        this.allAppointments = appointments == null ? new ArrayList<>() : appointments;
        rebuildAppointments();
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
    }

    public void updateAppointments(List<AppointmentDTO> appointments) {
        setAppointments(appointments);
    }

    public void updateDoctorDetails(UserDTO user, DoctorDTO doctorDetails) {
        renderUserHeader(user, doctorDetails);
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    private void rebuildAppointments() {
        filteredAppointments = allAppointments.stream()
                .sorted(Comparator.comparing(AppointmentDTO::getTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        currentPage = 1;
        updatePagination();
    }

    private void updatePagination() {
        appointmentsContainer.getChildren().clear();
        int total = filteredAppointments.size();
        if (total == 0) {
            Label empty = new Label("Nu exista programari pentru criteriile selectate.");
            empty.getStyleClass().add("page-subtitle");
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
            appointmentsContainer.getChildren().add(createAppointmentRow(appt));
        }

        paginationLabel.setText("Se afiseaza " + (fromIndex + 1) + "-" + toIndex + " din " + total + " rezultate");

        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(toIndex >= total);
    }

    private Node createAppointmentRow(AppointmentDTO appt) {
        GridPane row = new GridPane();
        row.getStyleClass().add("appointment-row");

        if (isActiveAppointment(appt)) {
            row.getStyleClass().add("row-active");
        }
        if (isUrgent(appt)) {
            row.getStyleClass().add("row-urgent");
        }

        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setPrefWidth(60);
        ColumnConstraints patientCol = new ColumnConstraints();
        patientCol.setPrefWidth(240);
        ColumnConstraints serviceCol = new ColumnConstraints();
        serviceCol.setPrefWidth(160);
        ColumnConstraints statusCol = new ColumnConstraints();
        statusCol.setPrefWidth(140);
        ColumnConstraints actionsCol = new ColumnConstraints();
        actionsCol.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        row.getColumnConstraints().addAll(timeCol, patientCol, serviceCol, statusCol, actionsCol);
        row.setHgap(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(formatTime(appt.getTime()));
        timeLabel.getStyleClass().add("patient-name");

        HBox patientCell = createPatientCell(appt);
        Label serviceLabel = createServiceLabel(appt);
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

    private HBox createPatientCell(AppointmentDTO appt) {
        String name = appt.getPatientName() == null ? "Pacient" : appt.getPatientName();

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("patient-avatar");
        Label initials = new Label(initials(name));
        initials.getStyleClass().add("patient-initials");
        avatar.getChildren().add(initials);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("patient-name");

        Label meta = new Label("ID: " + appt.getPatientId());
        meta.getStyleClass().add("patient-meta");

        VBox info = new VBox(2, nameLabel, meta);
        HBox cell = new HBox(8, avatar, info);
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private Label createServiceLabel(AppointmentDTO appt) {
        String service = serviceName(appt);
        Label label = new Label(service);
        label.getStyleClass().add("pill");
        label.getStyleClass().add(serviceStyle(service));
        return label;
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
            actions.getChildren().add(createActionButton("Cheama", "action-primary",
                    () -> presenter.onApproveAppointment(appt.getAppointmentId())));
        } else if (Objects.equals(status, "CONFIRMED")) {
            actions.getChildren().add(createActionButton("Fisa", "action-ghost",
                    () -> openConsultation(appt)));
            actions.getChildren().add(createActionButton("Consulta", "action-primary",
                    () -> openConsultation(appt)));
        } else if (Objects.equals(status, "DONE")) {
            actions.getChildren().add(createActionButton("Detalii", "action-ghost",
                    () -> setInfo("Detaliile consultului sunt in lucru.")));
        }

        if (isUrgent(appt) && !Objects.equals(status, "DONE")) {
            actions.getChildren().add(createActionButton("Tiaj Rapid", "action-secondary",
                    () -> setInfo("Tiaj rapid este in lucru.")));
        }

        return actions;
    }

    private Button createActionButton(String text, String styleClass, Runnable handler) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", styleClass);
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
        nextBreakLabel.setText(nextBreakTime());
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

    private String serviceStyle(String service) {
        String lower = service == null ? "" : service.toLowerCase();
        if (lower.contains("urg")) {
            return "pill-red";
        }
        if (lower.contains("control")) {
            return "pill-purple";
        }
        if (lower.contains("eco") || lower.contains("consult")) {
            return "pill-blue";
        }
        return "pill-gray";
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
}
