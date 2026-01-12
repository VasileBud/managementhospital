package view.patient;

import app.AppScene;
import app.SceneNavigator;
import app.ClientSession;
import presenter.patient.PatientMedicalRecordPresenter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.dto.MedicalRecordEntryDTO;
import model.dto.UserDTO;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientMedicalRecordView {

    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label initialsLabel;
    @FXML private Label totalEntriesLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private VBox recordsContainer;
    @FXML private Label statusLabel;

    private PatientMedicalRecordPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new PatientMedicalRecordPresenter(this);
        statusLabel.setText("");
        renderUserHeader(ClientSession.getInstance().getLoggedUser());
        presenter.loadMedicalRecord();
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
        SceneNavigator.navigateTo(AppScene.PATIENT_DASHBOARD);
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    public void renderEntries(List<MedicalRecordEntryDTO> entries) {
        recordsContainer.getChildren().clear();
        if (entries == null || entries.isEmpty()) {
            Label empty = new Label("Nu exista inregistrari medicale.");
            empty.getStyleClass().add("muted-text");
            recordsContainer.getChildren().add(empty);
            totalEntriesLabel.setText("0");
            lastUpdateLabel.setText("-");
            return;
        }

        totalEntriesLabel.setText(String.valueOf(entries.size()));
        lastUpdateLabel.setText(formatDate(entries.get(0).getEntryDate()));

        for (MedicalRecordEntryDTO entry : entries) {
            recordsContainer.getChildren().add(buildCard(entry));
        }
    }

    public void setInfo(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: #64748b;");
    }

    public void setError(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private VBox buildCard(MedicalRecordEntryDTO entry) {
        VBox card = new VBox(8.0);
        card.getStyleClass().add("record-card");

        HBox header = new HBox(8.0);
        Label title = new Label(valueOrDefault(entry.getDiagnosis(), "Diagnostic"));
        title.getStyleClass().add("record-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(formatDate(entry.getEntryDate()));
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

    private void renderUserHeader(UserDTO user) {
        if (user == null) {
            userNameLabel.setText("User");
            roleLabel.setText("Pacient");
            initialsLabel.setText("U");
            return;
        }
        userNameLabel.setText(user.getFullName());
        initialsLabel.setText(initials(user.getFullName()));
    }

    private String formatDate(OffsetDateTime dateTime) {
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
