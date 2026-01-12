package view.doctor;

import app.AppScene;
import app.SceneNavigator;
import presenter.medic.DoctorConsultationPresenter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.dto.AppointmentDTO;
import model.dto.MedicalRecordEntryDTO;
import model.dto.PatientProfileDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorConsultationView {


    @FXML private Label patientNameLabel;
    @FXML private Label patientCnpLabel;
    @FXML private Label patientAgeLabel;
    @FXML private Label patientAllergiesLabel;
    @FXML private Label patientConditionsLabel;

    @FXML private TextArea diagnosisArea;
    @FXML private TextArea treatmentArea;
    @FXML private TextArea notesArea;

    @FXML private TabPane historyTabPane;
    @FXML private VBox medicalHistoryContainer;
    @FXML private VBox appointmentHistoryContainer;

    @FXML private Label statusLabel;

    private DoctorConsultationPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new DoctorConsultationPresenter(this);

        presenter.loadConsultationContext();
    }

    @FXML
    public void onFinalizeClick() {
        String diag = diagnosisArea.getText();
        String treat = treatmentArea.getText();
        String notes = notesArea.getText();

        if (diag.isEmpty() || treat.isEmpty()) {
            setError("Diagnosticul și Tratamentul sunt obligatorii!");
            return;
        }
        presenter.finalizeConsultation(diag, treat, notes);
    }

    @FXML
    public void onBackClick() {
        SceneNavigator.navigateTo(AppScene.DOCTOR_DASHBOARD);
    }

    public void setAppointmentDetails(AppointmentDTO appt) {
    }

    public void setPatientProfile(PatientProfileDTO profile) {
        if (profile == null) return;
        if (patientNameLabel != null) patientNameLabel.setText(profile.getFullName());
        if (patientCnpLabel != null) patientCnpLabel.setText("CNP: " + (profile.getNationalId() == null ? "-" : profile.getNationalId()));

        if (patientAllergiesLabel != null) {
            String allergies = (profile.getAllergies() == null || profile.getAllergies().isEmpty())
                    ? "Nu sunt cunoscute"
                    : String.join(", ", profile.getAllergies());
            patientAllergiesLabel.setText(allergies);
        }

        if (patientConditionsLabel != null) {
            String conds = (profile.getConditions() == null || profile.getConditions().isEmpty())
                    ? "-"
                    : String.join(", ", profile.getConditions());
            patientConditionsLabel.setText(conds);
        }
    }

    public void updateMedicalHistory(List<MedicalRecordEntryDTO> history) {
        if (medicalHistoryContainer == null) return;
        medicalHistoryContainer.getChildren().clear();

        if (history == null || history.isEmpty()) {
            medicalHistoryContainer.getChildren().add(new Label("Nu exista istoric medical."));
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (MedicalRecordEntryDTO entry : history) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

            Label dateLbl = new Label("Data: " + entry.getEntryDate().format(dtf));
            dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d6efd;");

            Label doctorLbl = new Label("Medic: " + (entry.getDoctorName() != null ? entry.getDoctorName() : "-"));
            Label diagLbl = new Label("Diagnostic: " + entry.getDiagnosis());
            diagLbl.setWrapText(true);

            card.getChildren().addAll(dateLbl, doctorLbl, diagLbl);
            medicalHistoryContainer.getChildren().add(card);
        }
    }

    public void updateAppointmentHistory(List<AppointmentDTO> history) {
        if (appointmentHistoryContainer == null) return;
        appointmentHistoryContainer.getChildren().clear();

        if (history == null || history.isEmpty()) {
            appointmentHistoryContainer.getChildren().add(new Label("Nu există programări anterioare."));
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (AppointmentDTO appt : history) {
            VBox card = new VBox(5);
            String color = switch(appt.getStatus()) {
                case "DONE" -> "#d1e7dd";
                case "CANCELED" -> "#f8d7da";
                default -> "#fff3cd";
            };

            card.setStyle("-fx-background-color: " + color + "; -fx-padding: 8; -fx-background-radius: 5;");

            Label dateLbl = new Label(appt.getDate().format(dtf) + " | " + appt.getStatus());
            dateLbl.setStyle("-fx-font-weight: bold;");

            Label infoLbl = new Label(appt.getServiceName() + " - " + appt.getDoctorName());

            card.getChildren().addAll(dateLbl, infoLbl);
            appointmentHistoryContainer.getChildren().add(card);
        }
    }

    public void onConsultationSaved() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText("Consultația a fost salvată și închisă!");
        alert.showAndWait();
        onBackClick();
    }

    public void setBusy(boolean busy) {
        if (statusLabel != null) statusLabel.setText(busy ? "Se lucrează..." : "");
    }

    public void setError(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
