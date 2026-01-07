package com.hospital_management.client.view.patient;

import com.hospital_management.client.presenter.patient.PublicPresenter;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import shared.dto.DoctorDTO;
import shared.dto.DoctorScheduleDTO;
import shared.dto.SpecializationDTO;

import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PublicView {

    private static final String ALL_SPECIALIZATIONS = "Toate specializarile";

    @FXML private VBox doctorsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> specializationFilter;
    @FXML private Button searchButton;
    @FXML private Label statusLabel;

    private PublicPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new PublicPresenter(this);
        setInfo("Se incarca informatiile...");
        searchField.setOnAction(event -> onSearchClick());
        specializationFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !Objects.equals(oldValue, newValue)) {
                onSearchClick();
            }
        });
        presenter.loadAll();
    }

    @FXML
    public void onGoToLoginClick() {
        presenter.onGoToLogin();
    }

    @FXML
    public void onGoToRegisterClick() {
        presenter.onGoToRegister();
    }

    @FXML
    public void onSearchClick() {
        presenter.onSearch(searchField.getText(), specializationFilter.getValue());
    }

    public void setBusy(boolean busy) {
        searchField.setDisable(busy);
        specializationFilter.setDisable(busy);
        if (searchButton != null) {
            searchButton.setDisable(busy);
        }
    }

    public void setInfo(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #666666;");
    }

    public void setError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    public void setSpecializations(List<SpecializationDTO> items) {
        specializationFilter.getItems().clear();
        specializationFilter.getItems().add(ALL_SPECIALIZATIONS);
        for (SpecializationDTO dto : items) {
            specializationFilter.getItems().add(dto.getName());
        }
        specializationFilter.getSelectionModel().selectFirst();
    }

    public void setDoctors(List<DoctorDTO> doctors) {
        doctorsContainer.getChildren().clear();
        if (doctors.isEmpty()) {
            Label empty = new Label("Nu exista medici disponibili.");
            empty.getStyleClass().add("muted-text");
            doctorsContainer.getChildren().add(empty);
            return;
        }

        for (DoctorDTO doctor : doctors) {
            doctorsContainer.getChildren().add(createDoctorCard(doctor));
        }
    }

    private Node createDoctorCard(DoctorDTO doctor) {
        VBox card = new VBox(12.0);
        card.getStyleClass().add("doctor-card");

        HBox header = new HBox(16.0);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("doctor-avatar");
        Label initials = new Label(getInitials(doctor.getFullName()));
        initials.getStyleClass().add("doctor-initials");
        avatar.getChildren().add(initials);

        VBox details = new VBox(6.0);
        String nameText = doctor.getFullName() == null ? "" : doctor.getFullName();
        String specText = doctor.getSpecializationName() == null ? "" : doctor.getSpecializationName();
        Label name = new Label(nameText);
        name.getStyleClass().add("doctor-name");
        Label spec = new Label(specText);
        spec.getStyleClass().add("doctor-subtitle");
        details.getChildren().addAll(name, spec);

        Button scheduleBtn = new Button("Vezi Program & Disponibilitate");
        scheduleBtn.getStyleClass().add("primary2-button");
        VBox actions = new VBox(scheduleBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(avatar, details, actions);
        HBox.setHgrow(details, Priority.ALWAYS);

        VBox detailsPane = new VBox(12.0);
        detailsPane.getStyleClass().add("doctor-details");
        detailsPane.setVisible(false);
        detailsPane.setManaged(false);
        detailsPane.setMaxWidth(Double.MAX_VALUE);

        Label scheduleTitle = new Label("Orar general de lucru");
        scheduleTitle.getStyleClass().add("details-title");
        VBox scheduleList = new VBox(4.0);
        scheduleList.getChildren().add(createMutedLabel("Apasa pentru a incarca orarul."));
        VBox scheduleBox = new VBox(6.0, scheduleTitle, scheduleList);

        Label availabilityTitle = new Label("Verifica locuri libere");
        availabilityTitle.getStyleClass().add("details-title");

        HBox dateRow = new HBox(8.0);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label("Alege data:");
        dateLabel.getStyleClass().add("details-label");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Selecteaza data...");
        datePicker.getStyleClass().add("field-date");
        dateRow.getChildren().addAll(dateLabel, datePicker);

        Label dateError = new Label();
        dateError.getStyleClass().add("field-error");
        dateError.setManaged(false);
        dateError.setVisible(false);

        FlowPane slotsPane = new FlowPane();
        slotsPane.getStyleClass().add("slots-pane");
        slotsPane.setHgap(8.0);
        slotsPane.setVgap(8.0);
        slotsPane.getChildren().add(createMutedLabel("Selecteaza o data pentru a vedea orele."));

        Label bookingInfo = createMutedLabel("Te poti programa dupa autentificare sau inregistrare.");

        VBox availabilityBox = new VBox(6.0);
        availabilityBox.getChildren().addAll(availabilityTitle, dateRow, dateError, slotsPane, bookingInfo);

        detailsPane.getChildren().addAll(scheduleBox, availabilityBox);

        final boolean[] scheduleLoaded = { false };
        final boolean[] scheduleLoading = { false };

        scheduleBtn.setOnAction(event -> {
            boolean show = !detailsPane.isVisible();
            detailsPane.setVisible(show);
            detailsPane.setManaged(show);
            scheduleBtn.setText(show ? "Ascunde Program & Disponibilitate" : "Vezi Program & Disponibilitate");

            if (!show || scheduleLoaded[0] || scheduleLoading[0]) {
                return;
            }
            scheduleLoading[0] = true;
            scheduleList.getChildren().setAll(createMutedLabel("Se incarca orarul..."));

            presenter.fetchDoctorSchedule(doctor.getDoctorId(),
                    schedule -> {
                        scheduleLoading[0] = false;
                        scheduleLoaded[0] = true;
                        scheduleList.getChildren().clear();
                        if (schedule.isEmpty()) {
                            scheduleList.getChildren().add(createMutedLabel("Medicul nu are orar configurat."));
                            return;
                        }
                        for (DoctorScheduleDTO s : schedule) {
                            String dayName = getDayName(s.getDayOfWeek());
                            String interval = s.getStartTime() + " - " + s.getEndTime();
                            Label line = new Label(dayName + ": " + interval);
                            line.getStyleClass().add("doctor-meta");
                            scheduleList.getChildren().add(line);
                        }
                    },
                    error -> {
                        scheduleLoading[0] = false;
                        scheduleLoaded[0] = false;
                        String message = (error == null || error.isBlank())
                                ? "Nu pot incarca orarul medicului."
                                : error;
                        Label errorLabel = new Label(message);
                        errorLabel.getStyleClass().add("field-error");
                        scheduleList.getChildren().setAll(errorLabel);
                    });
        });

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateError.setVisible(false);
            dateError.setManaged(false);

            if (newVal == null) {
                dateError.setText("Selecteaza o data.");
                dateError.setVisible(true);
                dateError.setManaged(true);
                slotsPane.getChildren().setAll(createMutedLabel("Selecteaza o data pentru a vedea orele."));
                return;
            }

            slotsPane.getChildren().setAll(createMutedLabel("Se cauta ore disponibile..."));

            presenter.fetchAvailableSlots(doctor.getDoctorId(), newVal,
                    slots -> {
                        slotsPane.getChildren().clear();
                        if (slots.isEmpty()) {
                            slotsPane.getChildren().add(createMutedLabel("Nu sunt ore disponibile pentru data aleasa."));
                            return;
                        }
                        for (LocalTime time : slots) {
                            Button slotButton = new Button(time.toString());
                            slotButton.getStyleClass().add("slot-button");
                            slotButton.setOnAction(evt -> {
                                for (Node node : slotsPane.getChildren()) {
                                    if (node instanceof Button button) {
                                        button.getStyleClass().remove("slot-selected");
                                    }
                                }
                                slotButton.getStyleClass().add("slot-selected");
                            });
                            slotsPane.getChildren().add(slotButton);
                        }
                    },
                    error -> {
                        slotsPane.getChildren().clear();
                        String message = (error == null || error.isBlank())
                                ? "Nu pot incarca orele disponibile."
                                : error;
                        Label errorLabel = new Label(message);
                        errorLabel.getStyleClass().add("field-error");
                        slotsPane.getChildren().add(errorLabel);
                    });
        });

        card.getChildren().addAll(header, detailsPane);
        return card;
    }

    private Label createMutedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "DR";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return ("" + parts[0].charAt(0)).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private String getDayName(int dayOfWeek) {
        try {
            return java.time.DayOfWeek.of(dayOfWeek)
                    .getDisplayName(TextStyle.FULL, new Locale("ro", "RO"));
        } catch (Exception e) {
            return "Ziua " + dayOfWeek;
        }
    }
}
