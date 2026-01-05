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
        card.getStyleClass().add("doctor-card");

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("doctor-avatar");
        Label initials = new Label(getInitials(doctor.getFullName()));
        initials.getStyleClass().add("doctor-initials");
        avatar.getChildren().add(initials);

        VBox details = new VBox(6.0);
        Label name = new Label(doctor.getFullName());
        name.getStyleClass().add("doctor-name");
        Label spec = new Label(doctor.getSpecializationName());
        spec.getStyleClass().add("doctor-subtitle");
        Label meta = new Label("Disponibil pentru programare");
        meta.getStyleClass().add("doctor-meta");
        details.getChildren().addAll(name, spec, meta);

        VBox actions = new VBox(8.0);
        Button viewBtn = new Button("Vezi profil");
        viewBtn.getStyleClass().add("ghost-button");
        Button bookBtn = new Button("Programeaza");
        bookBtn.getStyleClass().add("primary-button");
        actions.getChildren().addAll(viewBtn, bookBtn);

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

}
