package view.manager;

import app.AppScene;
import app.SceneNavigator;
import app.ClientSession;
import presenter.manager.ManagerPatientHistoryPresenter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.dto.AppointmentDTO;
import model.dto.MedicalRecordEntryDTO;
import model.dto.PatientProfileDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManagerPatientHistoryView {

    @FXML private Label patientNameLabel;
    @FXML private Label patientCnpLabel;
    @FXML private Label patientAllergiesLabel;
    @FXML private Label patientConditionsLabel;

    @FXML private TabPane historyTabPane;
    @FXML private VBox medicalHistoryContainer;
    @FXML private VBox appointmentHistoryContainer;

    @FXML private Label statusLabel;

    private ManagerPatientHistoryPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new ManagerPatientHistoryPresenter(this);

        presenter.loadPatientData();
    }

    @FXML
    public void onBackClick() {
        AppScene previous = ClientSession.getInstance().getPreviousScene();

        if (previous != null) {
            SceneNavigator.navigateToFresh(previous);

            ClientSession.getInstance().setPreviousScene(null);
        } else {
            SceneNavigator.navigateToFresh(AppScene.MANAGER_DASHBOARD);
        }
    }

    public void setPatientProfile(PatientProfileDTO profile) {
        if (profile == null) return;

        if (patientNameLabel != null) patientNameLabel.setText(profile.getFullName());
        if (patientCnpLabel != null) patientCnpLabel.setText(profile.getNationalId());

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
            medicalHistoryContainer.getChildren().add(new Label("Nu există istoric medical (fișe)."));
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (MedicalRecordEntryDTO entry : history) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

            Label dateLbl = new Label("📅 Data: " + entry.getEntryDate().format(dtf));
            dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d6efd;");

            Label doctorLbl = new Label("Medic: " + (entry.getDoctorName() != null ? entry.getDoctorName() : "-"));

            Label diagLbl = new Label("Diagnostic: " + entry.getDiagnosis());
            diagLbl.setWrapText(true);
            diagLbl.setStyle("-fx-font-weight: bold;");

            Label treatLbl = new Label("Tratament: " + entry.getTreatment());
            treatLbl.setWrapText(true);

            card.getChildren().addAll(dateLbl, doctorLbl, diagLbl, treatLbl);
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

            String bgColor = switch(appt.getStatus()) {
                case "DONE" -> "#d1e7dd";
                case "CANCELED" -> "#f8d7da";
                case "CONFIRMED" -> "#cff4fc";
                default -> "#fff3cd";
            };

            card.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ccc;");

            Label dateLbl = new Label(appt.getDate().format(dtf) + " | " + appt.getStatus());
            dateLbl.setStyle("-fx-font-weight: bold;");

            Label infoLbl = new Label(appt.getServiceName() + " - Dr. " + appt.getDoctorName());

            card.getChildren().addAll(dateLbl, infoLbl);
            appointmentHistoryContainer.getChildren().add(card);
        }
    }

    public void setError(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }


    @FXML
    public void onAddRecordClick() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Adăugare Fișă Medicală");
        dialog.setHeaderText("Completează detaliile consultației");

        ButtonType saveButtonType = new ButtonType("Salvează", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Diagnostic...");
        diagnosisArea.setPrefHeight(100);

        TextArea treatmentArea = new TextArea();
        treatmentArea.setPromptText("Tratament prescris...");
        treatmentArea.setPrefHeight(100);

        grid.add(new Label("Diagnostic:"), 0, 0);
        grid.add(diagnosisArea, 1, 0);
        grid.add(new Label("Tratament:"), 0, 1);
        grid.add(treatmentArea, 1, 1);

        GridPane.setHgrow(diagnosisArea, Priority.ALWAYS);
        GridPane.setHgrow(treatmentArea, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        diagnosisArea.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                String diag = diagnosisArea.getText();
                String treat = treatmentArea.getText();

                presenter.addMedicalRecord(diag, treat);
            }
        });
    }

    public void setInfo(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
    }
}
