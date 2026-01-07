package com.hospital_management.client.view.manager;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.manager.ManagerStatsPresenter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Polyline;
import shared.dto.AppointmentDTO;
import shared.dto.ChartPointDTO;
import shared.dto.StatsDTO;
import shared.dto.UserDTO;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManagerDashboardView {

    @FXML private Label managerInitialsLabel;
    @FXML private Label managerNameLabel;
    @FXML private Label managerRoleLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    @FXML private Label patientsTodayValueLabel;
    @FXML private Label activeConsultationsValueLabel;
    @FXML private Label doctorOccupancyValueLabel;
    @FXML private Label revenueValueLabel;
    @FXML private Label patientsTodayTrendLabel;
    @FXML private Label activeConsultationsTrendLabel;
    @FXML private Label doctorOccupancyTrendLabel;
    @FXML private Label revenueTrendLabel;

    @FXML private Pane lineChartPane;
    @FXML private Polyline admissionsLine;
    @FXML private Polyline dischargesLine;

    @FXML private Arc arcCardio;
    @FXML private Arc arcNeuro;
    @FXML private Arc arcPeds;
    @FXML private Arc arcUrgent;
    @FXML private Label donutValueLabel;
    @FXML private HBox cardioRow;
    @FXML private HBox neuroRow;
    @FXML private HBox pedsRow;
    @FXML private HBox urgentRow;
    @FXML private Label cardioLabel;
    @FXML private Label neuroLabel;
    @FXML private Label pedsLabel;
    @FXML private Label urgentLabel;
    @FXML private Label cardioPercentLabel;
    @FXML private Label neuroPercentLabel;
    @FXML private Label pedsPercentLabel;
    @FXML private Label urgentPercentLabel;

    @FXML private VBox recentAppointmentsContainer;
    @FXML private TextField appointmentsSearchField;
    @FXML private ComboBox<String> appointmentsStatusFilter;

    private ManagerStatsPresenter presenter;
    private List<ChartPointDTO> pendingAdmissionsSeries = List.of();
    private List<ChartPointDTO> pendingDischargesSeries = List.of();
    private List<AppointmentDTO> recentAppointments = List.of();
    private String appointmentsStatusCode = "ALL";

    @FXML
    public void initialize() {
        presenter = new ManagerStatsPresenter(this);
        renderUserHeader(ClientSession.getInstance().getLoggedUser());
        if (statusLabel != null) {
            statusLabel.setText("");
        }
        if (lineChartPane != null) {
            lineChartPane.widthProperty().addListener((obs, oldVal, newVal) -> updateLineChart());
            lineChartPane.heightProperty().addListener((obs, oldVal, newVal) -> updateLineChart());
        }
        if (appointmentsSearchField != null) {
            appointmentsSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyRecentAppointmentsFilter());
        }
        setupAppointmentsStatusFilter();
        presenter.loadStats();
    }

    @FXML
    public void onNavDashboardClick() {
        if (presenter != null) {
            presenter.loadStats();
        }
    }

    @FXML
    public void onNavStaffClick() {
        setInfo("Sectiunea Personal Medical este in lucru.");
    }

    @FXML
    public void onNavPatientsClick() {
        setInfo("Sectiunea Pacienti este in lucru.");
    }

    @FXML
    public void onNavAppointmentsClick() {
        SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING);
    }

    @FXML
    public void onNavFinanceClick() {
        setInfo("Sectiunea Financiar este in lucru.");
    }

    @FXML
    public void onNavInventoryClick() {
        setInfo("Sectiunea Inventar este in lucru.");
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
    public void onHelpClick() {
        setInfo("Ajutorul este in lucru.");
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    public void renderStats(StatsDTO stats) {
        if (stats == null) {
            setError("Nu am primit statistici valide.");
            return;
        }

        patientsTodayValueLabel.setText(formatNumber(stats.getPatientsToday()));
        activeConsultationsValueLabel.setText(formatNumber(stats.getActiveConsultations()));
        doctorOccupancyValueLabel.setText(formatPercent(stats.getDoctorOccupancyPercent()));
        revenueValueLabel.setText(formatNumber(stats.getRevenueEstimate()));

        updateTrendLabel(patientsTodayTrendLabel, stats.getPatientsTodayChange());
        updateTrendLabel(activeConsultationsTrendLabel, stats.getActiveConsultationsChange());
        updateTrendLabel(doctorOccupancyTrendLabel, stats.getDoctorOccupancyChange());
        updateTrendLabel(revenueTrendLabel, stats.getRevenueChange());

        pendingAdmissionsSeries = safeSeries(stats.getAdmissionsSeries());
        pendingDischargesSeries = safeSeries(stats.getDischargesSeries());
        updateLineChart();

        updateDonut(stats.getOccupancyBySpecialization(), stats.getDoctorOccupancyPercent());
        recentAppointments = stats.getRecentAppointments() == null ? List.of() : stats.getRecentAppointments();
        applyRecentAppointmentsFilter();
    }

    private void setupAppointmentsStatusFilter() {
        if (appointmentsStatusFilter == null) {
            return;
        }
        appointmentsStatusFilter.setItems(FXCollections.observableArrayList(
                "Toate",
                "In asteptare",
                "Confirmate",
                "Finalizate",
                "Anulate"
        ));
        appointmentsStatusFilter.getSelectionModel().selectFirst();
        appointmentsStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            appointmentsStatusCode = statusCodeForAppointmentLabel(newVal);
            applyRecentAppointmentsFilter();
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

    private void applyRecentAppointmentsFilter() {
        String search = normalize(appointmentsSearchField == null ? null : appointmentsSearchField.getText());

        List<AppointmentDTO> filtered = new ArrayList<>(recentAppointments == null ? List.of() : recentAppointments);
        filtered = filtered.stream()
                .filter(this::matchesTab)
                .filter(appt -> matchesSearch(appt, search))
                .toList();

        renderRecentAppointments(filtered);
    }

    private boolean matchesTab(AppointmentDTO appt) {
        if (appt == null || "ALL".equals(appointmentsStatusCode)) {
            return true;
        }
        return normalizeStatus(appt.getStatus()).equals(appointmentsStatusCode);
    }

    private boolean matchesSearch(AppointmentDTO appt, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String patient = normalize(appt == null ? null : appt.getPatientName());
        String doctor = normalize(appt == null ? null : appt.getDoctorName());
        String service = normalize(appt == null ? null : appt.getServiceName());
        return patient.contains(search) || doctor.contains(search) || service.contains(search);
    }

    private void renderUserHeader(UserDTO user) {
        if (user == null) {
            managerNameLabel.setText("Manager");
            managerRoleLabel.setText("Manager General");
            managerInitialsLabel.setText("MG");
            if (welcomeLabel != null) {
                welcomeLabel.setText("Bun venit, Manager");
            }
            return;
        }

        String fullName = user.getFullName();
        managerNameLabel.setText(fullName);
        managerRoleLabel.setText(roleLabel(user.getRole()));
        managerInitialsLabel.setText(initials(fullName));
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bun venit, " + fullName);
        }
    }

    private String roleLabel(String role) {
        if (role == null) {
            return "Manager General";
        }
        if (role.equalsIgnoreCase("MANAGER")) {
            return "Manager General";
        }
        return role;
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "MG";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return ("" + parts[0].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT);
    }

    public void setInfo(String message) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(message == null ? "" : message);
        statusLabel.setStyle("-fx-text-fill: #64748b;");
    }

    public void setError(String message) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(message == null ? "" : message);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }

    private void updateTrendLabel(Label label, double value) {
        if (label == null) {
            return;
        }
        label.getStyleClass().removeAll("stat-tag-up", "stat-tag-down");
        if (value >= 0) {
            label.getStyleClass().add("stat-tag-up");
        } else {
            label.getStyleClass().add("stat-tag-down");
        }
        label.setText(formatChange(value));
    }

    private void updateLineChart() {
        if (lineChartPane == null || admissionsLine == null || dischargesLine == null) {
            return;
        }
        double width = lineChartPane.getWidth();
        double height = lineChartPane.getHeight();
        if (width <= 0 || height <= 0) {
            width = lineChartPane.getPrefWidth();
            height = lineChartPane.getPrefHeight();
        }
        if (width <= 0 || height <= 0) {
            Platform.runLater(this::updateLineChart);
            return;
        }

        setPolylineSeries(admissionsLine, pendingAdmissionsSeries, width, height);
        setPolylineSeries(dischargesLine, pendingDischargesSeries, width, height);
    }

    private void setPolylineSeries(Polyline polyline, List<ChartPointDTO> series, double width, double height) {
        if (series == null || series.isEmpty()) {
            polyline.getPoints().setAll(0.0, height, width, height);
            return;
        }

        double max = series.stream().mapToDouble(ChartPointDTO::getValue).max().orElse(1.0);
        if (max <= 0) {
            max = 1.0;
        }

        int count = series.size();
        double stepX = count == 1 ? 0 : width / (count - 1);
        List<Double> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double value = series.get(i).getValue();
            double x = stepX * i;
            double y = height - ((value / max) * height);
            points.add(x);
            points.add(y);
        }
        polyline.getPoints().setAll(points);
    }

    private void updateDonut(List<ChartPointDTO> occupancy, double totalPercent) {
        List<ChartPointDTO> sorted = occupancy == null
                ? new ArrayList<>()
                : new ArrayList<>(occupancy);
        sorted.sort(Comparator.comparingDouble(ChartPointDTO::getValue).reversed());

        ChartPointDTO cardio = getAt(sorted, 0);
        ChartPointDTO neuro = getAt(sorted, 1);
        ChartPointDTO peds = getAt(sorted, 2);
        ChartPointDTO urgent = getAt(sorted, 3);

        applyArcValues(cardio, neuro, peds, urgent);
        updateLegendRow(cardioRow, cardioLabel, cardioPercentLabel, cardio, "Cardiologie");
        updateLegendRow(neuroRow, neuroLabel, neuroPercentLabel, neuro, "Neurologie");
        updateLegendRow(pedsRow, pedsLabel, pedsPercentLabel, peds, "Pediatrie");
        updateLegendRow(urgentRow, urgentLabel, urgentPercentLabel, urgent, "Urgente");

        if (donutValueLabel != null) {
            donutValueLabel.setText(formatPercent(totalPercent));
        }
    }

    private void applyArcValues(ChartPointDTO cardio, ChartPointDTO neuro,
                                ChartPointDTO peds, ChartPointDTO urgent) {
        double startAngle = 90.0;
        startAngle = applyArc(arcCardio, startAngle, cardio);
        startAngle = applyArc(arcNeuro, startAngle, neuro);
        startAngle = applyArc(arcPeds, startAngle, peds);
        applyArc(arcUrgent, startAngle, urgent);
    }

    private double applyArc(Arc arc, double startAngle, ChartPointDTO point) {
        if (arc == null) {
            return startAngle;
        }
        double percent = point == null ? 0.0 : point.getValue();
        double length = -(percent / 100.0) * 360.0;
        arc.setStartAngle(startAngle);
        arc.setLength(length);
        return startAngle + length;
    }

    private void updateLegendRow(HBox row, Label nameLabel, Label percentLabel,
                                 ChartPointDTO point, String fallback) {
        if (row == null || nameLabel == null || percentLabel == null) {
            return;
        }
        if (point == null || point.getValue() <= 0) {
            row.setVisible(false);
            row.setManaged(false);
            return;
        }
        row.setVisible(true);
        row.setManaged(true);
        nameLabel.setText(point.getLabel() == null || point.getLabel().isBlank() ? fallback : point.getLabel());
        percentLabel.setText(formatPercent(point.getValue()));
    }

    private void renderRecentAppointments(List<AppointmentDTO> appointments) {
        if (recentAppointmentsContainer == null) {
            return;
        }
        recentAppointmentsContainer.getChildren().clear();
        if (appointments == null || appointments.isEmpty()) {
            Label empty = new Label("Nu exista programari recente.");
            empty.getStyleClass().add("table-text-muted");
            recentAppointmentsContainer.getChildren().add(empty);
            return;
        }

        for (AppointmentDTO appt : appointments) {
            recentAppointmentsContainer.getChildren().add(buildAppointmentRow(appt));
        }
    }

    private GridPane buildAppointmentRow(AppointmentDTO appt) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row");

        row.getColumnConstraints().addAll(tableColumnConstraints());

        HBox patientCell = new HBox(8.0);
        patientCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("table-avatar");
        Label initials = new Label(initials(appt.getPatientName()));
        initials.getStyleClass().add("table-avatar-text");
        avatar.getChildren().add(initials);
        Label patientName = new Label(valueOrDefault(appt.getPatientName(), "Pacient"));
        patientName.getStyleClass().add("table-text");
        patientCell.getChildren().addAll(avatar, patientName);

        Label doctorName = new Label(valueOrDefault(appt.getDoctorName(), "Medic"));
        doctorName.getStyleClass().add("table-text-muted");

        Label section = new Label(valueOrDefault(appt.getServiceName(), "-"));
        section.getStyleClass().add("table-text-muted");

        Label dateTime = new Label(formatDateTime(appt.getDate(), appt.getTime()));
        dateTime.getStyleClass().add("table-text-muted");

        Label status = new Label(statusLabel(appt.getStatus()));
        status.getStyleClass().add("status-pill");
        status.getStyleClass().add(statusStyle(appt.getStatus()));

        Button action = new Button("Editare");
        action.getStyleClass().add("table-action");
        action.setOnAction(event -> setInfo("Editarea programarii este in lucru."));

        row.add(patientCell, 0, 0);
        row.add(doctorName, 1, 0);
        row.add(section, 2, 0);
        row.add(dateTime, 3, 0);
        row.add(status, 4, 0);
        row.add(action, 5, 0);

        GridPane.setHgrow(patientCell, Priority.ALWAYS);
        GridPane.setHgrow(action, Priority.ALWAYS);

        return row;
    }

    private List<ColumnConstraints> tableColumnConstraints() {
        List<ColumnConstraints> cols = new ArrayList<>();
        cols.add(column(24));
        cols.add(column(18));
        cols.add(column(14));
        cols.add(column(16));
        cols.add(column(14));
        cols.add(column(14));
        return cols;
    }

    private ColumnConstraints column(double percent) {
        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(percent);
        return c;
    }

    private String statusLabel(String raw) {
        String normalized = normalizeStatus(raw);
        return switch (normalized) {
            case "PENDING" -> "In asteptare";
            case "CONFIRMED" -> "Confirmat";
            case "DONE" -> "Finalizat";
            case "CANCELED" -> "Anulat";
            default -> normalized;
        };
    }

    private String statusStyle(String raw) {
        String normalized = normalizeStatus(raw);
        return switch (normalized) {
            case "PENDING" -> "status-pending";
            case "CONFIRMED" -> "status-confirmed";
            case "DONE" -> "status-done";
            case "CANCELED" -> "status-canceled";
            default -> "status-active";
        };
    }

    private String normalizeStatus(String raw) {
        if (raw == null) {
            return "PENDING";
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.equals("CANCELLED")) {
            return "CANCELED";
        }
        return value;
    }

    private String formatDateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            return "-";
        }
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("ro"));
        String dateText = date.format(dateFormatter);
        String timeText = time == null ? "" : time.format(DateTimeFormatter.ofPattern("HH:mm"));
        return timeText.isBlank() ? dateText : dateText + ", " + timeText;
    }

    private String formatNumber(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("ro"));
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(Math.round(value));
    }

    private String formatNumber(long value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("ro"));
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(value);
    }

    private String formatPercent(double value) {
        return Math.round(value) + "%";
    }

    private String formatChange(double value) {
        String sign = value >= 0 ? "+" : "";
        return sign + Math.round(value) + "%";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private List<ChartPointDTO> safeSeries(List<ChartPointDTO> series) {
        return series == null ? List.of() : series;
    }

    private ChartPointDTO getAt(List<ChartPointDTO> list, int index) {
        if (list == null || list.size() <= index) {
            return null;
        }
        return list.get(index);
    }
}
