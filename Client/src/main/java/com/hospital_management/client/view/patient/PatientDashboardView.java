package com.hospital_management.client.view.patient;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.patient.PatientDashboardPresenter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Pair;
import java.util.Optional;
import shared.dto.AppointmentDTO;
import shared.dto.MedicalRecordEntryDTO;
import shared.dto.PatientDashboardDTO;
import shared.dto.PatientDetailsDTO;
import shared.dto.PatientProfileDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PatientDashboardView {

    @FXML private Label userNameLabel;
    @FXML private Label initialsLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label nextDoctorName;
    @FXML private Label nextDoctorSpecialization;
    @FXML private Label nextDateLabel;
    @FXML private Label nextTimeLabel;
    @FXML private Label nextDoctorInitials;
    @FXML private Label patientNameLabel;
    @FXML private Label patientCnpLabel;
    @FXML private Label patientBirthDateLabel;
    @FXML private Label patientAgeLabel;
    @FXML private Label patientGenderLabel;
    @FXML private Label patientPhoneLabel;
    @FXML private Label patientAddressLabel;
    @FXML private Label bloodTypeLabel;
    @FXML private Label weightLabel;
    @FXML private Label heightLabel;
    @FXML private FlowPane allergiesPane;
    @FXML private FlowPane conditionsPane;
    @FXML private Label totalEntriesLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private VBox medicalRecordsContainer;
    @FXML private DatePicker appointmentsFilterDatePicker;
    @FXML private ComboBox<String> appointmentsStatusFilter;
    @FXML private Button clearAppointmentsFilterButton;
    @FXML private GridPane appointmentsTableHeader;
    @FXML private VBox appointmentsContainer;
    @FXML private Label statusLabel;

    private PatientDashboardPresenter presenter;
    private Long currentAppointmentId = null;
    private AppointmentDTO currentAppointment = null;

    @FXML
    public void initialize() {
        presenter = new PatientDashboardPresenter(this);
        statusLabel.setText("");
        renderUserHeader(ClientSession.getInstance().getLoggedUser());
        setupAppointmentsTableHeader();
        appointmentsFilterDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> presenter.onAppointmentsFilterDateSelected(newVal));
        setupAppointmentsStatusFilter();
        presenter.loadDashboard();
    }

    @FXML
    public void onClearAppointmentsFilterClick() {
        presenter.onClearAppointmentsFilter();
    }

    @FXML
    public void onNewAppointmentClick() {
        ClientSession.getInstance().clearEditMode();
        ClientSession.getInstance().clearSelectedAppointment();
        SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING);
    }

    @FXML
    public void onModifyAppointmentClick() {
        if (currentAppointmentId == null || currentAppointment == null) {
            setError("Nu există o programare de modificat.");
            return;
        }

        ClientSession.getInstance().setSelectedAppointment(currentAppointment);
        ClientSession.getInstance().setAppointmentToEdit(currentAppointmentId);

        SceneNavigator.navigateToFresh(AppScene.APPOINTMENT_BOOKING);
    }

    @FXML
    public void onCancelAppointmentClick() {
        if(currentAppointmentId == null) {
            setError("Nu există o programare de anulat!");
            return;
        }
        presenter.onCancelAppointment(currentAppointmentId);
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    public void renderDashboard(PatientDashboardDTO dto) {
        if (dto == null) {
            setError("Datele nu au fost primite.");
            return;
        }

        resetPatientDetails();
        setProfile(dto.getProfile());
        setNextAppointment(dto.getNextAppointment());
        setInfo("");
    }

    public void setBusy(boolean busy) {
        if (appointmentsFilterDatePicker != null) {
            appointmentsFilterDatePicker.setDisable(busy);
        }
        if (clearAppointmentsFilterButton != null) {
            clearAppointmentsFilterButton.setDisable(busy);
        }
    }

    public void setAppointmentsFilterDate(LocalDate date) {
        if (appointmentsFilterDatePicker == null) {
            return;
        }
        appointmentsFilterDatePicker.setValue(date);
    }

    public void setAppointmentsStatusFilter(String statusCode) {
        if (appointmentsStatusFilter == null) {
            return;
        }
        appointmentsStatusFilter.getSelectionModel().select(labelForAppointmentStatus(statusCode));
    }

    public void renderAppointments(List<AppointmentDTO> appointments) {
        appointmentsContainer.getChildren().clear();
        if (appointments == null || appointments.isEmpty()) {
            Label empty = new Label("Nu exista programari pentru criteriile selectate.");
            empty.getStyleClass().add("muted-text");
            appointmentsContainer.getChildren().add(empty);
            return;
        }

        for (AppointmentDTO appt : appointments) {
            appointmentsContainer.getChildren().add(buildAppointmentRow(appt));
        }
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
        appointmentsStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                presenter.onAppointmentsStatusSelected(statusCodeForAppointmentLabel(newVal)));
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

    private String labelForAppointmentStatus(String statusCode) {
        String normalized = normalizeStatus(statusCode);
        return switch (normalized) {
            case "PENDING" -> "In asteptare";
            case "CONFIRMED" -> "Confirmate";
            case "DONE" -> "Finalizate";
            case "CANCELED" -> "Anulate";
            default -> "Toate";
        };
    }

    private void setupAppointmentsTableHeader() {
        if (appointmentsTableHeader == null) {
            return;
        }

        appointmentsTableHeader.getChildren().clear();
        appointmentsTableHeader.getColumnConstraints().setAll(appointmentsTableColumnConstraints());

        addHeaderLabel("Pacient", 0);
        addHeaderLabel("Medic", 1);
        addHeaderLabel("Serviciu", 2);
        addHeaderLabel("Data / Ora", 3);
        addHeaderLabel("Status", 4);
        addHeaderLabel("Actiuni", 5);
    }

    private List<ColumnConstraints> appointmentsTableColumnConstraints() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(24);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(20);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(16);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(16);
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setPercentWidth(12);
        ColumnConstraints c5 = new ColumnConstraints();
        c5.setPercentWidth(12);
        return List.of(c0, c1, c2, c3, c4, c5);
    }

    private void addHeaderLabel(String text, int column) {
        Label label = new Label(text);
        label.getStyleClass().add("table-header-text");
        appointmentsTableHeader.add(label, column, 0);
    }

    private GridPane buildAppointmentRow(AppointmentDTO appt) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row");
        row.getColumnConstraints().addAll(appointmentsTableColumnConstraints());

        HBox patientCell = buildPersonCell(appt == null ? null : appt.getPatientName(), "Pacient");
        Label doctorName = new Label(valueOrDefault(appt == null ? null : appt.getDoctorName(), "Medic"));
        doctorName.getStyleClass().add("table-text");

        Label service = new Label(valueOrDefault(appt == null ? null : appt.getServiceName(), "-"));
        service.getStyleClass().add("table-text-muted");

        Label dateTime = new Label(formatAppointmentDateTime(appt == null ? null : appt.getDate(), appt == null ? null : appt.getTime()));
        dateTime.getStyleClass().add("table-text-muted");

        String rawStatus = appt == null ? null : appt.getStatus();
        Label status = new Label(appointmentStatusLabel(rawStatus));
        status.getStyleClass().add("status-pill");
        status.getStyleClass().add(appointmentStatusStyle(rawStatus));

        HBox actions = buildAppointmentActions(appt);

        row.add(patientCell, 0, 0);
        row.add(doctorName, 1, 0);
        row.add(service, 2, 0);
        row.add(dateTime, 3, 0);
        row.add(status, 4, 0);
        row.add(actions, 5, 0);

        GridPane.setHgrow(patientCell, Priority.ALWAYS);
        GridPane.setHgrow(actions, Priority.ALWAYS);

        return row;
    }

    private HBox buildPersonCell(String name, String fallback) {
        String safeName = valueOrDefault(name, fallback);

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("table-avatar");
        Label initials = new Label(initials(safeName));
        initials.getStyleClass().add("table-avatar-text");
        avatar.getChildren().add(initials);

        Label label = new Label(safeName);
        label.getStyleClass().add("table-text");

        HBox cell = new HBox(8, avatar, label);
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private HBox buildAppointmentActions(AppointmentDTO appt) {
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (appt == null) {
            return actions;
        }

        String status = normalizeStatus(appt.getStatus());
        if ("DONE".equals(status)) {
            actions.getChildren().add(createActionButton("Feedback", "secondary-button", () ->
                    showFeedbackDialog(appt.getAppointmentId())));
            return actions;
        }

        if (!"CANCELED".equals(status)) {
            actions.getChildren().add(createActionButton("Modifica", "table-action", () ->
                    openEditAppointment(appt)));
            actions.getChildren().add(createActionButton("Anuleaza", "table-action-danger", () ->
                    presenter.onCancelAppointment(appt.getAppointmentId())));
        }

        return actions;
    }

    private Button createActionButton(String text, String styleClass, Runnable handler) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> handler.run());
        return button;
    }

    private void openEditAppointment(AppointmentDTO appt) {
        if (appt == null) {
            return;
        }
        ClientSession.getInstance().setSelectedAppointment(appt);
        ClientSession.getInstance().setAppointmentToEdit(appt.getAppointmentId());
        SceneNavigator.navigateToFresh(AppScene.APPOINTMENT_BOOKING);
    }

    private String formatAppointmentDateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            return "-";
        }
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("ro"));
        String dateText = date.format(dateFormatter);
        String timeText = time == null ? "" : time.format(DateTimeFormatter.ofPattern("HH:mm"));
        return timeText.isBlank() ? dateText : dateText + ", " + timeText;
    }

    private String appointmentStatusLabel(String raw) {
        String normalized = normalizeStatus(raw);
        return switch (normalized) {
            case "PENDING" -> "In asteptare";
            case "CONFIRMED" -> "Confirmat";
            case "DONE" -> "Finalizat";
            case "CANCELED" -> "Anulat";
            default -> normalized;
        };
    }

    private String appointmentStatusStyle(String raw) {
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

    public void setInfo(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: #64748b;");
    }

    public void setError(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void renderUserHeader(UserDTO user) {
        if (user == null) {
            userNameLabel.setText("User");
            initialsLabel.setText("U");
            welcomeLabel.setText("Buna ziua!");
            return;
        }
        userNameLabel.setText(user.getFullName());
        initialsLabel.setText(initials(user.getFullName()));
        welcomeLabel.setText("Buna ziua, " + user.getFirstName() + "!");
    }

    public void setPatientDetails(PatientDetailsDTO details) {
        if (details == null) {
            return;
        }

        patientNameLabel.setText(valueOrDash(details.getFullName()));
        patientCnpLabel.setText(valueOrDash(details.getNationalId()));
        patientBirthDateLabel.setText(formatBirthDate(details.getBirthDate()));
        patientAgeLabel.setText(formatAge(details.getBirthDate()));
        patientGenderLabel.setText(formatGender(details.getNationalId()));
        patientPhoneLabel.setText(valueOrDash(details.getPhone()));
        patientAddressLabel.setText(valueOrDash(details.getAddress()));

        bloodTypeLabel.setText(valueOrDash(details.getBloodType()));
        weightLabel.setText(formatWeight(details.getWeightKg()));
        heightLabel.setText(formatHeight(details.getHeightCm()));
        renderTags(allergiesPane, details.getAllergies(), "tag");
        renderTags(conditionsPane, details.getConditions(), "tag-warning");
    }

    private void setProfile(PatientProfileDTO profile) {
        if (profile == null) {
            bloodTypeLabel.setText("-");
            weightLabel.setText("-");
            heightLabel.setText("-");
            allergiesPane.getChildren().clear();
            conditionsPane.getChildren().clear();
            return;
        }

        bloodTypeLabel.setText(emptyOrDash(profile.getBloodType()));
        weightLabel.setText(profile.getWeightKg() == null ? "-" : formatNumber(profile.getWeightKg()) + " kg");
        heightLabel.setText(profile.getHeightCm() == null ? "-" : formatNumber(profile.getHeightCm()) + " cm");

        renderTags(allergiesPane, profile.getAllergies(), "tag");
        renderTags(conditionsPane, profile.getConditions(), "tag-warning");
    }

    private void setNextAppointment(AppointmentDTO appt) {
        if (appt == null) {
            this.currentAppointmentId = null;
            this.currentAppointment = null;
            nextDoctorName.setText("Nu exista programari viitoare.");
            nextDoctorSpecialization.setText("-");
            nextDoctorInitials.setText("--");
            nextDateLabel.setText("-");
            nextTimeLabel.setText("-");
            return;
        }

        this.currentAppointmentId = appt.getAppointmentId();
        this.currentAppointment = appt;

        nextDoctorName.setText(appt.getDoctorName() == null ? "Doctor" : appt.getDoctorName());
        String spec = appt.getServiceName() == null ? "Consultatie" : appt.getServiceName();
        nextDoctorSpecialization.setText(spec);
        nextDoctorInitials.setText(initials(appt.getDoctorName()));
        nextDateLabel.setText(appt.getDate() == null ? "-" : appt.getDate().format(DateTimeFormatter.ISO_DATE));
        nextTimeLabel.setText(appt.getTime() == null ? "-" : appt.getTime().toString());
    }


    public void renderMedicalRecordEntries(List<MedicalRecordEntryDTO> entries) {
        medicalRecordsContainer.getChildren().clear();
        if (entries == null || entries.isEmpty()) {
            medicalRecordsContainer.getChildren().add(createMutedLabel("Nu exista inregistrari medicale."));
            totalEntriesLabel.setText("0");
            lastUpdateLabel.setText("-");
            return;
        }

        totalEntriesLabel.setText(String.valueOf(entries.size()));
        lastUpdateLabel.setText(formatRecordDate(entries.get(0).getEntryDate()));

        for (MedicalRecordEntryDTO entry : entries) {
            medicalRecordsContainer.getChildren().add(buildMedicalRecordCard(entry));
        }
    }

    private void renderTags(FlowPane pane, List<String> values, String styleClass) {
        pane.getChildren().clear();
        if (values == null || values.isEmpty()) {
            Label empty = new Label("-");
            empty.getStyleClass().add("muted-text");
            pane.getChildren().add(empty);
            return;
        }
        for (String value : values) {
            Label tag = new Label(value);
            tag.getStyleClass().add(styleClass);
            pane.getChildren().add(tag);
        }
    }

    private void resetPatientDetails() {
        patientNameLabel.setText("-");
        patientCnpLabel.setText("-");
        patientBirthDateLabel.setText("-");
        patientAgeLabel.setText("-");
        patientGenderLabel.setText("-");
        patientPhoneLabel.setText("-");
        patientAddressLabel.setText("-");
        totalEntriesLabel.setText("-");
        lastUpdateLabel.setText("-");
        if (medicalRecordsContainer != null) {
            medicalRecordsContainer.getChildren().clear();
        }
    }

    private Label createMutedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private VBox buildMedicalRecordCard(MedicalRecordEntryDTO entry) {
        VBox card = new VBox(8.0);
        card.getStyleClass().add("record-card");

        HBox header = new HBox(8.0);
        Label title = new Label(valueOrDefault(entry.getDiagnosis(), "Diagnostic"));
        title.getStyleClass().add("record-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(formatRecordDate(entry.getEntryDate()));
        date.getStyleClass().add("record-date");
        header.getChildren().addAll(title, spacer, date);

        String doctorName = valueOrDefault(entry.getDoctorName(), "Doctor");
        Label doctor = new Label("Medic: " + doctorName);
        doctor.getStyleClass().add("muted-text");

        String treatment = valueOrDefault(entry.getTreatment(), "Tratament: -");
        if (!treatment.startsWith("Tratament:")) {
            treatment = "Tratament: " + treatment;
        }
        Label treatmentLabel = new Label(treatment);
        treatmentLabel.getStyleClass().add("record-text");

        String notes = valueOrDefault(entry.getNotes(), "Observatii: -");
        if (!notes.startsWith("Observatii:")) {
            notes = "Observatii: " + notes;
        }
        Label notesLabel = new Label(notes);
        notesLabel.getStyleClass().add("record-text");

        String appointmentInfo = entry.getAppointmentId() == null
                ? "Programare: -"
                : "Programare: #" + entry.getAppointmentId();
        Label appointmentLabel = new Label(appointmentInfo);
        appointmentLabel.getStyleClass().add("muted-text");

        card.getChildren().addAll(header, doctor, treatmentLabel, notesLabel, appointmentLabel);
        return card;
    }

    private String emptyOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatNumber(Double value) {
        if (value == null) return "-";
        if (value == value.longValue()) {
            return String.valueOf(value.longValue());
        }
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private String formatBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return "-";
        }
        return birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatAge(LocalDate birthDate) {
        if (birthDate == null) {
            return "-";
        }
        int years = Period.between(birthDate, LocalDate.now()).getYears();
        if (years < 0) {
            return "-";
        }
        return years + " ani";
    }

    private String formatGender(String nationalId) {
        if (nationalId == null || nationalId.isBlank()) {
            return "-";
        }
        char first = nationalId.trim().charAt(0);
        if (first == '1' || first == '5') {
            return "Masculin";
        }
        if (first == '2' || first == '6') {
            return "Feminin";
        }
        return "-";
    }

    private String formatWeight(Double weightKg) {
        if (weightKg == null || weightKg <= 0) {
            return "-";
        }
        return String.format(Locale.ROOT, "%.1f kg", weightKg);
    }

    private String formatHeight(Double heightCm) {
        if (heightCm == null || heightCm <= 0) {
            return "-";
        }
        return String.format(Locale.ROOT, "%.0f cm", heightCm);
    }

    private String formatRecordDate(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim();
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return ("" + parts[0].charAt(0)).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private void showFeedbackDialog(long appointmentId) {
        Dialog<Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Feedback Consultatie");
        dialog.setHeaderText("Cum a decurs vizita la medic?");

        // Butoanele standard (Trimite / Anuleaza)
        ButtonType loginButtonType = new ButtonType("Trimite", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Input Rating (ComboBox 1-5)
        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(5, 4, 3, 2, 1);
        ratingCombo.setValue(5); // Default maxim :)

        // Input Comentariu
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Scrie aici opinia ta...");
        commentArea.setPrefRowCount(3);

        grid.add(new Label("Nota:"), 0, 0);
        grid.add(ratingCombo, 1, 0);
        grid.add(new Label("Comentariu:"), 0, 1);
        grid.add(commentArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertim rezultatul când se apasă Trimite
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(ratingCombo.getValue(), commentArea.getText());
            }
            return null;
        });

        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/patient_dashboard.css").toExternalForm()
        );

        dialog.getDialogPane().getStyleClass().add("my-dialog");

        Optional<Pair<Integer, String>> result = dialog.showAndWait();

        result.ifPresent(feedback -> {
            // AICI APELĂM PRESENTER-UL
            presenter.onSendFeedback(appointmentId, feedback.getKey(), feedback.getValue());
        });
    }
}
