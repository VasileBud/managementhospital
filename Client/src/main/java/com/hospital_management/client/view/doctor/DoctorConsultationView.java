package com.hospital_management.client.view.doctor;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.medic.DoctorConsultationPresenter;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import shared.dto.AppointmentDTO;
import shared.dto.PatientDetailsDTO;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DoctorConsultationView {

    @FXML private Label patientBreadcrumbLabel;
    @FXML private Label patientInitialsLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label patientCnpLabel;
    @FXML private Label patientBirthDateLabel;
    @FXML private Label patientAgeLabel;
    @FXML private Label patientGenderLabel;
    @FXML private Label patientPhoneLabel;
    @FXML private Label patientAddressLabel;
    @FXML private Label patientBloodTypeLabel;
    @FXML private Label patientWeightLabel;
    @FXML private Label patientHeightLabel;
    @FXML private Label patientAllergiesLabel;
    @FXML private Label patientConditionsLabel;
    @FXML private DatePicker visitDatePicker;
    @FXML private ComboBox<String> visitTypeCombo;
    @FXML private       TextField visitReasonField;
    @FXML private TextField icdField;
    @FXML private TextArea primaryDiagnosisArea;
    @FXML private TextArea secondaryDiagnosisArea;
    @FXML private TextArea instructionsArea;
    @FXML private TextArea observationsArea;
    @FXML private VBox medicinesContainer;
    @FXML private Label statusLabel;
    @FXML private Button finalizeButton;

    private DoctorConsultationPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new DoctorConsultationPresenter(this);
        visitDatePicker.setValue(LocalDate.now());
        visitTypeCombo.getItems().setAll("Initiala", "Control", "Urgenta");
        visitTypeCombo.setValue("Initiala");

        medicinesContainer.getChildren().clear();
        medicinesContainer.getChildren().add(createMedicineRow("", "", ""));

        statusLabel.setText("");
        presenter.loadConsultationContext();
    }

    @FXML
    public void onNavDashboardClick() {
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.navigateTo(AppScene.DOCTOR_DASHBOARD);
    }

    @FXML
    public void onNavAppointmentsClick() {
        SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING);
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
    public void onAddMedicineClick() {
        medicinesContainer.getChildren().add(createMedicineRow("", "", ""));
    }

    @FXML
    public void onFinalizeClick() {
        presenter.finalizeConsultation();
    }

    @FXML
    public void onCancelClick() {
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.navigateTo(AppScene.DOCTOR_DASHBOARD);
    }

    private HBox createMedicineRow(String name, String dose, String duration) {
        TextField nameField = new TextField(name);
        nameField.setPromptText("Nume medicament");
        nameField.getStyleClass().add("medicine-input");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        TextField doseField = new TextField(dose);
        doseField.setPromptText("Doza");
        doseField.getStyleClass().add("medicine-input");
        doseField.setPrefWidth(120);

        TextField durationField = new TextField(duration);
        durationField.setPromptText("Durata");
        durationField.getStyleClass().add("medicine-input");
        durationField.setPrefWidth(140);

        Button removeButton = new Button("X");
        removeButton.getStyleClass().add("delete-button");

        HBox row = new HBox(10, nameField, doseField, durationField, removeButton);
        row.getStyleClass().add("medicine-row");

        removeButton.setOnAction(event -> medicinesContainer.getChildren().remove(row));
        return row;
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
        if (finalizeButton != null) {
            finalizeButton.setDisable(busy);
        }
    }

    public void onConsultationSaved() {
        setInfo("Fisa de consultatie a fost salvata.");
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.navigateTo(AppScene.DOCTOR_DASHBOARD);
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

    public void setAppointmentDetails(AppointmentDTO appointment) {
        if (appointment == null) {
            return;
        }

        if (appointment.getDate() != null) {
            visitDatePicker.setValue(appointment.getDate());
        }

        String serviceName = appointment.getServiceName();
        if (serviceName != null && !serviceName.isBlank()) {
            if (!visitTypeCombo.getItems().contains(serviceName)) {
                visitTypeCombo.getItems().add(serviceName);
            }
            visitTypeCombo.setValue(serviceName);
        }
    }

    public void setPatientDetails(PatientDetailsDTO details) {
        if (details == null) {
            patientBreadcrumbLabel.setText("Pacient");
            patientNameLabel.setText("Pacient");
            patientInitialsLabel.setText("P");
            patientCnpLabel.setText("-");
            patientBirthDateLabel.setText("-");
            patientAgeLabel.setText("-");
            patientGenderLabel.setText("-");
            patientPhoneLabel.setText("-");
            patientAddressLabel.setText("-");
            patientBloodTypeLabel.setText("-");
            patientWeightLabel.setText("-");
            patientHeightLabel.setText("-");
            patientAllergiesLabel.setText("-");
            patientConditionsLabel.setText("-");
            return;
        }

        String fullName = details.getFullName().trim();
        if (fullName.isBlank()) {
            fullName = "Pacient";
        }

        patientBreadcrumbLabel.setText(fullName);
        patientNameLabel.setText(fullName);
        patientInitialsLabel.setText(initials(fullName));

        String nationalId = details.getNationalId();
        patientCnpLabel.setText(valueOrDash(nationalId));
        patientBirthDateLabel.setText(formatBirthDate(details.getBirthDate()));
        patientAgeLabel.setText(formatAge(details.getBirthDate()));
        patientGenderLabel.setText(formatGender(nationalId));
        patientPhoneLabel.setText(valueOrDash(details.getPhone()));
        patientAddressLabel.setText(valueOrDash(details.getAddress()));
        patientBloodTypeLabel.setText(valueOrDash(details.getBloodType()));
        patientWeightLabel.setText(formatWeight(details.getWeightKg()));
        patientHeightLabel.setText(formatHeight(details.getHeightCm()));
        patientAllergiesLabel.setText(formatList(details.getAllergies()));
        patientConditionsLabel.setText(formatList(details.getConditions()));
    }

    public String buildDiagnosisSummary() {
        String primary = normalize(primaryDiagnosisArea.getText());
        String icd = normalize(icdField.getText());
        if (!icd.isBlank()) {
            if (!primary.isBlank()) {
                return primary + " (ICD-10: " + icd + ")";
            }
            return "ICD-10: " + icd;
        }
        return primary;
    }

    public String buildTreatmentSummary() {
        List<String> lines = new ArrayList<>();

        for (Node node : medicinesContainer.getChildren()) {
            if (!(node instanceof HBox row)) {
                continue;
            }
            List<TextField> fields = row.getChildren().stream()
                    .filter(child -> child instanceof TextField)
                    .map(child -> (TextField) child)
                    .toList();
            if (fields.size() < 3) {
                continue;
            }
            String name = normalize(fields.get(0).getText());
            String dose = normalize(fields.get(1).getText());
            String duration = normalize(fields.get(2).getText());
            if (name.isBlank() && dose.isBlank() && duration.isBlank()) {
                continue;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(name.isBlank() ? "Medicament" : name);
            if (!dose.isBlank() || !duration.isBlank()) {
                sb.append(" - ");
                if (!dose.isBlank()) {
                    sb.append(dose);
                }
                if (!dose.isBlank() && !duration.isBlank()) {
                    sb.append(", ");
                }
                if (!duration.isBlank()) {
                    sb.append(duration);
                }
            }
            lines.add(sb.toString());
        }

        String instructions = normalize(instructionsArea.getText());
        if (!instructions.isBlank()) {
            lines.add("Instructiuni: " + instructions);
        }

        return String.join("\n", lines);
    }

    public String buildNotesSummary() {
        List<String> lines = new ArrayList<>();

        String reason = normalize(visitReasonField.getText());
        if (!reason.isBlank()) {
            lines.add("Motiv: " + reason);
        }

        String secondary = normalize(secondaryDiagnosisArea.getText());
        if (!secondary.isBlank()) {
            lines.add("Diagnostic secundar: " + secondary);
        }

        String observations = normalize(observationsArea.getText());
        if (!observations.isBlank()) {
            lines.add("Observatii: " + observations);
        }

        return String.join("\n", lines);
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

    private String formatBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return "-";
        }
        return birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim();
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

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "-";
        }
        return String.join(", ", values);
    }
}
