package com.hospital_management.client.view.patient;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.patient.PatientDashboardPresenter;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import shared.dto.AppointmentDTO;
import shared.dto.PatientDashboardDTO;
import shared.dto.PatientProfileDTO;
import shared.dto.UserDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientDashboardView {

    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label initialsLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label nextDoctorName;
    @FXML private Label nextDoctorSpecialization;
    @FXML private Label nextDateLabel;
    @FXML private Label nextTimeLabel;
    @FXML private Label nextDoctorInitials;
    @FXML private Label bloodTypeLabel;
    @FXML private Label weightLabel;
    @FXML private Label heightLabel;
    @FXML private FlowPane allergiesPane;
    @FXML private FlowPane conditionsPane;
    @FXML private VBox historyContainer;
    @FXML private Label statusLabel;

    private PatientDashboardPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new PatientDashboardPresenter(this);
        statusLabel.setText("");
        renderUserHeader(ClientSession.getInstance().getLoggedUser());
        presenter.loadDashboard();
    }

    @FXML
    public void onNavHomeClick() {
        SceneNavigator.navigateTo(AppScene.PATIENT_DASHBOARD);
    }

    @FXML
    public void onNavAppointmentsClick() {
        SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING);
    }

    @FXML
    public void onNavHistoryClick() {
        SceneNavigator.navigateTo(AppScene.PATIENT_MEDICAL_RECORD);
    }

    @FXML
    public void onNavResultsClick() {
        statusLabel.setText("Sectiunea Rezultate este in pregatire.");
    }

    @FXML
    public void onNewAppointmentClick() {
        SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING);
    }

    @FXML
    public void onModifyAppointmentClick() {
        statusLabel.setText("Modificarea programarii nu este disponibila.");
    }

    @FXML
    public void onCancelAppointmentClick() {
        statusLabel.setText("Anularea programarii nu este disponibila.");
    }

    @FXML
    public void onHistoryClick() {
        SceneNavigator.navigateTo(AppScene.PATIENT_MEDICAL_RECORD);
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    public void renderDashboard(PatientDashboardDTO dto) {
        if (dto == null) {
            setError("Datele nu au fost primite.");
            return;
        }

        setProfile(dto.getProfile());
        setNextAppointment(dto.getNextAppointment());
        setHistory(dto.getHistory());
        setInfo("");
    }

    public void setBusy(boolean busy) {
        // optional placeholder if needed
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
            roleLabel.setText("Pacient");
            initialsLabel.setText("U");
            welcomeLabel.setText("Buna ziua!");
            return;
        }
        userNameLabel.setText(user.getFullName());
        roleLabel.setText(user.getRole());
        initialsLabel.setText(initials(user.getFullName()));
        welcomeLabel.setText("Buna ziua, " + user.getFirstName() + "!");
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
            nextDoctorName.setText("Nu exista programari viitoare.");
            nextDoctorSpecialization.setText("-");
            nextDoctorInitials.setText("--");
            nextDateLabel.setText("-");
            nextTimeLabel.setText("-");
            return;
        }

        nextDoctorName.setText(appt.getDoctorName() == null ? "Doctor" : appt.getDoctorName());
        String spec = appt.getServiceName() == null ? "Consultatie" : appt.getServiceName();
        nextDoctorSpecialization.setText(spec);
        nextDoctorInitials.setText(initials(appt.getDoctorName()));
        nextDateLabel.setText(appt.getDate() == null ? "-" : appt.getDate().format(DateTimeFormatter.ISO_DATE));
        nextTimeLabel.setText(appt.getTime() == null ? "-" : appt.getTime().toString());
    }

    private void setHistory(List<AppointmentDTO> history) {
        historyContainer.getChildren().clear();
        Label hint = new Label("Detaliile sunt disponibile in fisa medicala.");
        hint.getStyleClass().add("muted-text");
        historyContainer.getChildren().add(hint);
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
}
