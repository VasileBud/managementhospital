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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import shared.dto.DoctorDTO;
import shared.dto.MedicalServiceDTO;
import shared.dto.SpecializationDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentBookingView {

    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label initialsLabel;
    @FXML private Label selectedDoctorHint;
    @FXML private ComboBox<SpecializationDTO> specializationCombo;
    @FXML private ComboBox<DoctorDTO> doctorCombo;
    @FXML private ComboBox<MedicalServiceDTO> serviceCombo;
    @FXML private DatePicker datePicker;
    @FXML private FlowPane timeSlotsPane;
    @FXML private Label summaryDoctorLabel;
    @FXML private Label summarySpecLabel;
    @FXML private Label summaryDateLabel;
    @FXML private Label summaryTimeLabel;
    @FXML private Label summaryServiceLabel;
    @FXML private Label summaryPriceLabel;
    @FXML private Button bookButton;
    @FXML private Label statusLabel;

    private AppointmentBookingPresenter presenter;
    private LocalTime selectedTime;

    @FXML
    public void initialize() {
        presenter = new AppointmentBookingPresenter(this);
        statusLabel.setText("");
        renderUserHeader(ClientSession.getInstance().getLoggedUser());
        setupComboBoxes();
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> presenter.onDateSelected(newVal));
        presenter.loadInitialData();
        datePicker.setValue(LocalDate.now());
    }

    private void setupComboBoxes() {
        specializationCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(SpecializationDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        specializationCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(SpecializationDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        specializationCombo.setOnAction(event ->
                presenter.onSpecializationSelected(specializationCombo.getSelectionModel().getSelectedItem()));

        doctorCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(DoctorDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        doctorCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(DoctorDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        doctorCombo.setOnAction(event ->
                presenter.onDoctorSelected(doctorCombo.getSelectionModel().getSelectedItem()));

        serviceCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(MedicalServiceDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        serviceCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
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
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    @FXML
    public void onBookClick() {
        presenter.onBook();
    }

    @FXML
    public void onBackClick() {
        SceneNavigator.navigateTo(AppScene.PATIENT_DASHBOARD);
    }

    public void setSpecializations(List<SpecializationDTO> items, SpecializationDTO allOption) {
        specializationCombo.getItems().clear();
        if (allOption != null) {
            specializationCombo.getItems().add(allOption);
        }
        specializationCombo.getItems().addAll(items);
        if (!specializationCombo.getItems().isEmpty()) {
            specializationCombo.getSelectionModel().selectFirst();
        }
    }

    public void setDoctors(List<DoctorDTO> doctors) {
        doctorCombo.getItems().setAll(doctors);
        doctorCombo.getSelectionModel().clearSelection();
    }

    public void setServices(List<MedicalServiceDTO> services) {
        serviceCombo.getItems().setAll(services);
        serviceCombo.getSelectionModel().clearSelection();
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
    }

    public void updateSummary(String doctorName, String specialization, LocalDate date,
                              LocalTime time, String serviceName, String price) {
        summaryDoctorLabel.setText(doctorName == null ? "-" : doctorName);
        summarySpecLabel.setText(specialization == null ? "-" : specialization);
        summaryDateLabel.setText(date == null ? "-" : date.toString());
        summaryTimeLabel.setText(time == null ? "-" : time.toString());
        summaryServiceLabel.setText(serviceName == null ? "-" : serviceName);
        summaryPriceLabel.setText(price == null ? "-" : price);
    }

    public void setSelectedDoctorHint(String text) {
        selectedDoctorHint.setText(text == null ? "" : text);
    }

    public void setBusy(boolean busy) {
        specializationCombo.setDisable(busy);
        doctorCombo.setDisable(busy);
        serviceCombo.setDisable(busy);
        datePicker.setDisable(busy);
        bookButton.setDisable(busy);
        timeSlotsPane.setDisable(busy);
    }

    public void setInfo(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: #64748b;");
    }

    public void setError(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    public void renderUserHeader(UserDTO user) {
        if (user == null) {
            userNameLabel.setText("User");
            roleLabel.setText("Pacient");
            initialsLabel.setText("U");
            return;
        }
        userNameLabel.setText(user.getFullName());
        roleLabel.setText(user.getRole());
        initialsLabel.setText(initials(user.getFullName()));
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
