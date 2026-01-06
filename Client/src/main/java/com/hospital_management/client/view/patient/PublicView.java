package com.hospital_management.client.view.patient;

import com.hospital_management.client.presenter.patient.PublicPresenter;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import shared.dto.DoctorDTO;
import shared.dto.MedicalServiceDTO;
import shared.dto.SpecializationDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;
import shared.dto.DoctorScheduleDTO;

import java.util.List;
import java.util.Objects;

public class PublicView {

    private static final String ALL_SPECIALIZATIONS = "Toate specializarile";

    @FXML private ScrollPane scrollPane;
    @FXML private VBox contentRoot;
    @FXML private StackPane heroSection;
    @FXML private VBox specializationsSection;
    @FXML private VBox doctorsSection;
    @FXML private FlowPane specializationChips;
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
                updateChipSelection(newValue);
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
    public void onNavHomeClick() {
        scrollTo(heroSection);
    }

    @FXML
    public void onNavDoctorsClick() {
        scrollTo(doctorsSection);
    }

    @FXML
    public void onNavSpecializationsClick() {
        scrollTo(specializationsSection);
    }

    @FXML
    public void onSearchClick() {
        presenter.onSearch(searchField.getText(), specializationFilter.getValue());
    }

    public void setBusy(boolean busy) {
        searchField.setDisable(busy);
        specializationFilter.setDisable(busy);
        if (searchButton != null) searchButton.setDisable(busy);
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

        specializationChips.getChildren().clear();
        addChip(ALL_SPECIALIZATIONS);
        for (SpecializationDTO dto : items) {
            addChip(dto.getName());
        }
        updateChipSelection(ALL_SPECIALIZATIONS);
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

    public void setServices(List<MedicalServiceDTO> services) {
        // no-op: services section removed
    }

    public void selectSpecialization(String name) {
        if (name == null || name.isEmpty()) {
            name = ALL_SPECIALIZATIONS;
        }
        specializationFilter.getSelectionModel().select(name);
        updateChipSelection(name);
    }

    private void addChip(String name) {
        Button chip = new Button(name);
        chip.getStyleClass().add("chip");
        chip.setOnAction(event -> {
            selectSpecialization(name);
            onSearchClick();
        });
        specializationChips.getChildren().add(chip);
    }

    private void updateChipSelection(String selected) {
        for (Node node : specializationChips.getChildren()) {
            if (node instanceof Button button) {
                boolean active = Objects.equals(button.getText(), selected);
                button.getStyleClass().remove("chip-active");
                if (active) {
                    button.getStyleClass().add("chip-active");
                }
            }
        }
    }

    private Node createDoctorCard(DoctorDTO doctor) {
        HBox card = new HBox(16.0);
        card.getStyleClass().add("doctor-card"); // Asigură-te că ai stilul 'card' sau 'doctor-card' în CSS

        // Avatar
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("doctor-avatar"); // Sau 'profile-avatar' conform CSS-ului tau
        Label initials = new Label(getInitials(doctor.getFullName()));
        initials.getStyleClass().add("doctor-initials"); // Sau 'profile-initials'
        avatar.getChildren().add(initials);

        // Detalii Text
        VBox details = new VBox(6.0);
        Label name = new Label(doctor.getFullName());
        name.getStyleClass().add("doctor-name");
        Label spec = new Label(doctor.getSpecializationName());
        spec.getStyleClass().add("doctor-subtitle");

        // Putem scoate "Disponibil pentru programare" daca punem butonul explicit
        details.getChildren().addAll(name, spec);

        // Actiuni (Butoane)
        VBox actions = new VBox(8.0);

        // --- MODIFICARE AICI ---
        Button scheduleBtn = new Button("Vezi Program & Disponibilitate");
        scheduleBtn.getStyleClass().add("secondary-button"); // Asigură-te că ai clasa asta în CSS
        // Aici apelăm metoda nouă pe care o vom scrie mai jos
        scheduleBtn.setOnAction(e -> showDoctorDetailsDialog(doctor));

        Button bookBtn = new Button("Autentificare pentru Programare");
        bookBtn.getStyleClass().add("primary-button");
        bookBtn.setOnAction(e -> onGoToLoginClick()); // Redirecționăm la login

        actions.getChildren().addAll(scheduleBtn, bookBtn);

        card.getChildren().addAll(avatar, details, actions);
        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);
        return card;
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

    private void scrollTo(Node node) {
        if (node == null || contentRoot == null || scrollPane == null) {
            return;
        }
        Platform.runLater(() -> {
            double contentHeight = contentRoot.getBoundsInLocal().getHeight();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            if (contentHeight <= viewportHeight) {
                scrollPane.setVvalue(0);
                return;
            }
            double y = node.getBoundsInParent().getMinY();
            double v = y / (contentHeight - viewportHeight);
            v = Math.max(0, Math.min(1, v));
            scrollPane.setVvalue(v);
        });
    }

    // --- METODE NOI PENTRU SCHEDULE ---

    private void showDoctorDetailsDialog(DoctorDTO doctor) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalii " + doctor.getFullName());
        dialog.setHeaderText("Orar și Disponibilitate - " + doctor.getSpecializationName());

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Încărcare CSS (opțional, ca să arate bine)
        var cssUrl = getClass().getResource("/css/patient_dashboard.css");
        if (cssUrl != null) {
            dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setMinWidth(450);

        // 1. ORAR GENERAL
        VBox scheduleBox = new VBox(10);
        Label scheduleTitle = new Label("Orar General de Lucru");
        scheduleTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox scheduleList = new VBox(5);
        Label loadingLabel = new Label("Se încarcă orarul...");
        scheduleList.getChildren().add(loadingLabel);

        scheduleBox.getChildren().addAll(scheduleTitle, scheduleList);

        // 2. DISPONIBILITATE
        VBox checkBox = new VBox(10);
        Label checkTitle = new Label("Verifică locuri libere");
        checkTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox pickerRow = new HBox(10);
        pickerRow.setAlignment(Pos.CENTER_LEFT);
        Label pickLabel = new Label("Alege data:");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Selectează data...");
        pickerRow.getChildren().addAll(pickLabel, datePicker);

        FlowPane slotsPane = new FlowPane();
        slotsPane.setHgap(10);
        slotsPane.setVgap(10);
        Label slotsHint = new Label("Selectează o dată pentru a vedea orele.");
        slotsPane.getChildren().add(slotsHint);

        checkBox.getChildren().addAll(checkTitle, pickerRow, slotsPane);

        content.getChildren().addAll(scheduleBox, new Separator(), checkBox);
        dialog.getDialogPane().setContent(content);

        // LOGICA DE INCARCARE
        // A. Cerem orarul
        presenter.fetchDoctorSchedule(doctor.getDoctorId(), schedule -> {
            scheduleList.getChildren().clear();
            if (schedule.isEmpty()) {
                scheduleList.getChildren().add(new Label("Medicul nu are orar configurat."));
            } else {
                for (DoctorScheduleDTO s : schedule) {
                    String dayName = getDayName(s.getDayOfWeek());
                    String interval = s.getStartTime() + " - " + s.getEndTime();
                    scheduleList.getChildren().add(new Label("• " + dayName + ": " + interval));
                }
            }
        });

        // B. Listener pe DatePicker
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            slotsPane.getChildren().clear();
            slotsPane.getChildren().add(new Label("Se caută..."));

            presenter.fetchAvailableSlots(doctor.getDoctorId(), newVal, slots -> {
                slotsPane.getChildren().clear();
                if (slots.isEmpty()) {
                    Label empty = new Label("Nu sunt locuri libere.");
                    empty.setStyle("-fx-text-fill: red;");
                    slotsPane.getChildren().add(empty);
                } else {
                    for (LocalTime time : slots) {
                        Label slotLabel = new Label(time.toString());
                        slotLabel.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 8; -fx-background-radius: 6;");
                        slotsPane.getChildren().add(slotLabel);
                    }
                }
            });
        });

        dialog.showAndWait();
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
