package com.hospital_management.client.view.patient;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.presenter.patient.AppointmentBookingPresenter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import shared.dto.AdminUserDTO;
import shared.dto.DoctorDTO;
import shared.dto.MedicalServiceDTO;
import shared.dto.SpecializationDTO;
import shared.dto.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AppointmentBookingView {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label initialsLabel;
    @FXML private Label formTitleLabel;
    @FXML private Label formHintLabel;
    @FXML private Label patientLabel;
    @FXML private Label doctorLabel;
    @FXML private VBox patientBox;
    @FXML private VBox specializationBox;
    @FXML private VBox doctorBox;
    @FXML private ComboBox<AdminUserDTO> patientCombo;
    @FXML private ComboBox<SpecializationDTO> specializationCombo;
    @FXML private ComboBox<DoctorDTO> doctorCombo;
    @FXML private ComboBox<MedicalServiceDTO> serviceCombo;
    @FXML private DatePicker datePicker;
    @FXML private FlowPane timeSlotsPane;
    @FXML private Label bookingDetailsLabel;
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private Button clearEditButton;
    @FXML private Label statusLabel;

    private AppointmentBookingPresenter presenter;
    private LocalTime selectedTime;
    private String currentRole = "PATIENT";

    @FXML
    public void initialize() {
        presenter = new AppointmentBookingPresenter(this);
        statusLabel.setText("");
        renderUserHeader(ClientSession.getInstance().getLoggedUser());
        setupComboBoxes();

        datePicker.setValue(LocalDate.now());

        patientCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateBookingDetails());
        doctorCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateBookingDetails());
        serviceCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateBookingDetails());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateBookingDetails();
            presenter.onDateSelected(newVal);
        });

        updateBookingDetails();
        presenter.loadInitialData();
    }

    private void setupComboBoxes() {
        patientCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(AdminUserDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        patientCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(AdminUserDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        patientCombo.setOnAction(event ->
                presenter.onPatientSelected(patientCombo.getSelectionModel().getSelectedItem()));

        doctorCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(DoctorDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        doctorCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(DoctorDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        doctorCombo.setOnAction(event ->
                presenter.onDoctorSelected(doctorCombo.getSelectionModel().getSelectedItem()));

        specializationCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SpecializationDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        specializationCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SpecializationDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        specializationCombo.setOnAction(event ->
                presenter.onSpecializationSelected(specializationCombo.getSelectionModel().getSelectedItem()));

        serviceCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MedicalServiceDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        serviceCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(MedicalServiceDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        serviceCombo.setOnAction(event ->
                presenter.onServiceSelected(serviceCombo.getSelectionModel().getSelectedItem()));
    }

    @FXML
    public void onNavDashboardClick() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null || user.getRole() == null) {
            SceneNavigator.navigateToFresh(AppScene.LOGIN);
            return;
        }
        switch (user.getRole().toUpperCase(Locale.ROOT)) {
            case "DOCTOR" -> SceneNavigator.navigateToFresh(AppScene.DOCTOR_DASHBOARD);
            case "MANAGER" -> SceneNavigator.navigateToFresh(AppScene.MANAGER_DASHBOARD);
            case "ADMIN" -> SceneNavigator.navigateToFresh(AppScene.ADMIN_DASHBOARD);
            default -> SceneNavigator.navigateToFresh(AppScene.PATIENT_DASHBOARD);
        }
    }

    @FXML
    public void onSaveClick() {
        presenter.onSave();
    }

    @FXML
    public void onResetClick() {
        presenter.onReset();
    }

    @FXML
    public void onClearEditClick() {
        presenter.onClearEdit();
    }

    @FXML
    public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    public void setRole(String role) {
        currentRole = role == null ? "PATIENT" : role.toUpperCase(Locale.ROOT);
    }

    public void setPatients(List<AdminUserDTO> patients) {
        patientCombo.getItems().setAll(patients);
        patientCombo.getSelectionModel().clearSelection();
    }

    public void setDoctors(List<DoctorDTO> doctors) {
        doctorCombo.getItems().setAll(doctors);
        doctorCombo.getSelectionModel().clearSelection();
    }

    public void setSpecializations(List<SpecializationDTO> specializations) {
        specializationCombo.getItems().setAll(specializations);
        specializationCombo.getSelectionModel().selectFirst();
    }

    public void setServices(List<MedicalServiceDTO> services) {
        serviceCombo.getItems().setAll(services);
        serviceCombo.getSelectionModel().clearSelection();
    }

    public void setSelectedDoctor(DoctorDTO doctor) {
        doctorCombo.getSelectionModel().select(doctor);
    }

    public void setSelectedSpecialization(SpecializationDTO specialization) {
        specializationCombo.getSelectionModel().select(specialization);
    }

    public void setSelectedService(MedicalServiceDTO service) {
        serviceCombo.getSelectionModel().select(service);
    }

    public void setSelectedPatient(AdminUserDTO patient) {
        patientCombo.getSelectionModel().select(patient);
    }

    public void lockPatient(String name) {
        patientLabel.setText(name == null ? "-" : name);
        patientLabel.setVisible(true);
        patientLabel.setManaged(true);
        patientCombo.setVisible(false);
        patientCombo.setManaged(false);
        updateBookingDetails();
    }

    public void unlockPatient() {
        patientLabel.setVisible(false);
        patientLabel.setManaged(false);
        patientCombo.setVisible(true);
        patientCombo.setManaged(true);
        updateBookingDetails();
    }

    public void lockDoctor(String name) {
        doctorLabel.setText(name == null ? "-" : name);
        doctorLabel.setVisible(true);
        doctorLabel.setManaged(true);
        doctorCombo.setVisible(false);
        doctorCombo.setManaged(false);
        updateBookingDetails();
    }

    public void unlockDoctor() {
        doctorLabel.setVisible(false);
        doctorLabel.setManaged(false);
        doctorCombo.setVisible(true);
        doctorCombo.setManaged(true);
        updateBookingDetails();
    }

    public void setSpecializationFilterVisible(boolean visible) {
        if (specializationBox == null) {
            return;
        }
        specializationBox.setManaged(visible);
        specializationBox.setVisible(visible);
    }

    public void setSelectedDate(LocalDate date) {
        datePicker.setValue(date);
    }

    public LocalDate getSelectedDate() {
        return datePicker.getValue();
    }

    public void setAvailableTimes(List<LocalTime> times) {
        timeSlotsPane.getChildren().clear();
        if (times == null || times.isEmpty()) {
            Label empty = new Label("Nu exista ore disponibile.");
            empty.getStyleClass().add("muted-text");
            timeSlotsPane.getChildren().add(empty);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        for (LocalTime time : times) {
            Button btn = new Button(time.format(fmt));
            btn.getStyleClass().add("slot-button");
            if (time.equals(selectedTime)) {
                btn.getStyleClass().add("slot-selected");
            }
            btn.setOnAction(event -> presenter.onTimeSelected(time));
            timeSlotsPane.getChildren().add(btn);
        }
    }

    public void setSelectedTime(LocalTime time) {
        this.selectedTime = time;
        timeSlotsPane.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                button.getStyleClass().remove("slot-selected");
                if (time != null && button.getText().equals(time.format(DateTimeFormatter.ofPattern("HH:mm")))) {
                    button.getStyleClass().add("slot-selected");
                }
            }
        });
        updateBookingDetails();
    }

    public void setEditMode(boolean editing, String hint) {
        if (editing) {
            formTitleLabel.setText("Modificare programare");
            saveButton.setText("Salveaza modificarile");
            formHintLabel.setText(hint == null ? "" : hint);
        } else {
            formTitleLabel.setText("Programare noua");
            saveButton.setText("Creeaza programarea");
            formHintLabel.setText("");
        }
        clearEditButton.setManaged(editing);
        clearEditButton.setVisible(editing);
    }

    public void clearForm() {
        patientCombo.getSelectionModel().clearSelection();
        doctorCombo.getSelectionModel().clearSelection();
        serviceCombo.getSelectionModel().clearSelection();
        selectedTime = null;
        setSelectedTime(null);
        timeSlotsPane.getChildren().clear();
    }

    public void setBusy(boolean busy) {
        patientCombo.setDisable(busy);
        specializationCombo.setDisable(busy);
        doctorCombo.setDisable(busy);
        serviceCombo.setDisable(busy);
        datePicker.setDisable(busy);
        saveButton.setDisable(busy);
        resetButton.setDisable(busy);
        clearEditButton.setDisable(busy);
        timeSlotsPane.setDisable(busy);
    }

    public void setInfo(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: #64748b;");
    }

    public void setError(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }

    public void renderUserHeader(UserDTO user) {
        if (user == null) {
            userNameLabel.setText("User");
            userRoleLabel.setText("Pacient");
            initialsLabel.setText("U");
            return;
        }
        userNameLabel.setText(user.getFullName());
        userRoleLabel.setText(roleLabel(user.getRole()));
        initialsLabel.setText(initials(user.getFullName()));
    }

    private String roleLabel(String role) {
        if (role == null) {
            return "Pacient";
        }
        return switch (role.toUpperCase(Locale.ROOT)) {
            case "ADMIN" -> "Administrator";
            case "MANAGER" -> "Manager";
            case "DOCTOR" -> "Medic";
            default -> "Pacient";
        };
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return ("" + parts[0].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT);
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

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private void updateBookingDetails() {
        if (bookingDetailsLabel == null) {
            return;
        }

        String patient = resolveSelectedPatient();
        String doctor = resolveSelectedDoctor();
        String service = resolveSelectedService();
        String dateTime = formatDateTime(datePicker == null ? null : datePicker.getValue(), selectedTime);
        String price = resolveSelectedPrice();

        bookingDetailsLabel.setText(
                "Pacient: " + patient + "\n" +
                        "Medic: " + doctor + "\n" +
                        "Serviciu: " + service + "\n" +
                        "Data / Ora: " + dateTime + "\n" +
                        "Pret: " + price
        );
    }

    private String resolveSelectedPatient() {
        if (patientLabel != null && patientLabel.isVisible() && patientLabel.getText() != null && !patientLabel.getText().isBlank()) {
            return patientLabel.getText();
        }
        AdminUserDTO patient = patientCombo == null ? null : patientCombo.getSelectionModel().getSelectedItem();
        if (patient != null && patient.getFullName() != null && !patient.getFullName().isBlank()) {
            return patient.getFullName();
        }
        return "-";
    }

    private String resolveSelectedDoctor() {
        if (doctorLabel != null && doctorLabel.isVisible() && doctorLabel.getText() != null && !doctorLabel.getText().isBlank()) {
            return doctorLabel.getText();
        }
        DoctorDTO doctor = doctorCombo == null ? null : doctorCombo.getSelectionModel().getSelectedItem();
        if (doctor != null && doctor.getFullName() != null && !doctor.getFullName().isBlank()) {
            return doctor.getFullName();
        }
        return "-";
    }

    private String resolveSelectedService() {
        MedicalServiceDTO service = serviceCombo == null ? null : serviceCombo.getSelectionModel().getSelectedItem();
        if (service != null && service.getName() != null && !service.getName().isBlank()) {
            return service.getName();
        }
        return "-";
    }

    private String resolveSelectedPrice() {
        MedicalServiceDTO service = serviceCombo == null ? null : serviceCombo.getSelectionModel().getSelectedItem();
        BigDecimal price = service == null ? null : service.getPrice();
        if (price == null) {
            return "-";
        }
        return price.stripTrailingZeros().toPlainString() + " RON";
    }
}
