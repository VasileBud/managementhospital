package view.admin;

import app.AppScene;
import app.SceneNavigator;
import presenter.admin.AdminPatientHistoryPresenter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.dto.AppointmentDTO;
import model.dto.MedicalRecordEntryDTO;
import model.dto.PatientProfileDTO;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class AdminPatientHistoryView {

    @FXML private Label patientNameLabel;
    @FXML private Label cnpLabel;
    @FXML private Label ageLabel;
    @FXML private Label bloodTypeLabel;
    @FXML private Label allergiesLabel;

    @FXML private TableView<AppointmentDTO> appointmentsTable;
    @FXML private TableColumn<AppointmentDTO, String> appDateCol;
    @FXML private TableColumn<AppointmentDTO, String> appDoctorCol;
    @FXML private TableColumn<AppointmentDTO, String> appServiceCol;
    @FXML private TableColumn<AppointmentDTO, String> appStatusCol;

    @FXML private TableView<MedicalRecordEntryDTO> historyTable;
    @FXML private TableColumn<MedicalRecordEntryDTO, String> dateCol;
    @FXML private TableColumn<MedicalRecordEntryDTO, String> doctorCol;
    @FXML private TableColumn<MedicalRecordEntryDTO, String> diagnosisCol;

    @FXML private TextArea detailsArea;

    @FXML private TextField newDiagnosisField;
    @FXML private TextArea newTreatmentArea;
    @FXML private Label statusLabel;

    private AdminPatientHistoryPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new AdminPatientHistoryPresenter(this);
        setupTables();

        if (historyTable != null) {
            historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    showRecordDetails(newVal);
                }
            });
        }

        presenter.loadPatientData();
    }


    @FXML
    public void onBackClick() {
        SceneNavigator.navigateToFresh(AppScene.ADMIN_DASHBOARD);
    }

    @FXML
    public void onAddRecordClick() {
        String diagnosis = newDiagnosisField.getText();
        String treatment = newTreatmentArea.getText();

        if (diagnosis.isBlank() || treatment.isBlank()) {
            setError("Te rog completeaza diagnosticul si tratamentul.");
            return;
        }
        presenter.addMedicalRecord(diagnosis, treatment);
    }

    public void setPatientProfile(PatientProfileDTO profile) {
        if (profile == null) return;
        patientNameLabel.setText(profile.getFullName());
        cnpLabel.setText("CNP: " + (profile.getNationalId() == null ? "-" : profile.getNationalId()));
        ageLabel.setText(profile.getBirthDate() == null ? "-" :
                String.valueOf(Period.between(profile.getBirthDate(), LocalDate.now()).getYears()));
        bloodTypeLabel.setText("Grupa: " + (profile.getBloodType() != null ? profile.getBloodType() : "-"));

        if (profile.getAllergies() != null && !profile.getAllergies().isEmpty()) {
            allergiesLabel.setText("ALERGII: " + profile.getAllergies());
            allergiesLabel.setVisible(true);
        } else {
            allergiesLabel.setText("Fara alergii cunoscute");
            allergiesLabel.setStyle("-fx-text-fill: green;");
        }
    }

    public void updateMedicalHistory(List<MedicalRecordEntryDTO> history) {
        historyTable.setItems(FXCollections.observableArrayList(history));
    }

    public void updateAppointmentHistory(List<AppointmentDTO> history) {
        appointmentsTable.setItems(FXCollections.observableArrayList(history));
    }

    public void clearInputFields() {
        newDiagnosisField.clear();
        newTreatmentArea.clear();
    }

    public void setInfo(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    public void setError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void showRecordDetails(MedicalRecordEntryDTO record) {
        StringBuilder sb = new StringBuilder();
        sb.append("Medic: ").append(record.getDoctorName()).append("\n");
        sb.append("------------------------------------------------\n");
        sb.append("DIAGNOSTIC:\n").append(record.getDiagnosis()).append("\n\n");
        sb.append("TRATAMENT / RECOMANDARI:\n").append(record.getTreatment()).append("\n");

        detailsArea.setText(sb.toString());
    }

    private void setupTables() {
        doctorCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDoctorName()));
        diagnosisCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDiagnosis()));

        appDateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDate().toString()));
        appServiceCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getServiceName()));
        appStatusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        appDoctorCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDoctorName()));
    }
}
