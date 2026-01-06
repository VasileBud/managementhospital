package com.hospital_management.client.view.patient;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.patient.PatientDashboardPresenter;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Pair;
import java.util.Optional;
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
    private Long currentAppointmentId = null;

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
        if (currentAppointmentId == null) {
            setError("Nu există o programare de modificat.");
            return;
        }

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

        this.currentAppointmentId = appt.getAppointmentId();

        nextDoctorName.setText(appt.getDoctorName() == null ? "Doctor" : appt.getDoctorName());
        String spec = appt.getServiceName() == null ? "Consultatie" : appt.getServiceName();
        nextDoctorSpecialization.setText(spec);
        nextDoctorInitials.setText(initials(appt.getDoctorName()));
        nextDateLabel.setText(appt.getDate() == null ? "-" : appt.getDate().format(DateTimeFormatter.ISO_DATE));
        nextTimeLabel.setText(appt.getTime() == null ? "-" : appt.getTime().toString());
    }

    private void setHistory(List<AppointmentDTO> history) {
        historyContainer.getChildren().clear();

        if (history == null || history.isEmpty()) {
            Label hint = new Label("Nu există istoric recent.");
            hint.getStyleClass().add("muted-text");
            historyContainer.getChildren().add(hint);
            return;
        }

        for (AppointmentDTO appt : history) {
            HBox row = new HBox(10);
            row.getStyleClass().add("card"); // Sau un stil mai simplu
            row.setAlignment(Pos.CENTER_LEFT);

            Label date = new Label(appt.getDate().toString());
            Label doctor = new Label(appt.getDoctorName());
            Label status = new Label(appt.getStatus());

            // Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(date, doctor, status, spacer);

            // --- BUTONUL DE FEEDBACK ---
            // Verificăm dacă statusul este DONE (sau terminat)
            if ("DONE".equalsIgnoreCase(appt.getStatus())) {
                Button feedbackBtn = new Button("Feedback");
                feedbackBtn.getStyleClass().add("secondary-button"); // Stil mai mic
                feedbackBtn.setOnAction(e -> showFeedbackDialog(appt.getAppointmentId()));
                row.getChildren().add(feedbackBtn);
            }

            historyContainer.getChildren().add(row);
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
