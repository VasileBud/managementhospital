package view.admin;

import app.AppScene;
import app.SceneNavigator;
import app.ClientSession;
import model.dto.*;
import presenter.admin.AdminUsersPresenter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class AdminDashboardView {

    private static final int PAGE_SIZE = 6;
    private static final String ALL_DEPARTMENTS = "Toate departamentele";
    private static final String ALL_STATUS = "Toate";

    @FXML private Label adminInitialsLabel, adminNameLabel, adminRoleLabel;
    @FXML private Label totalDoctorsValueLabel, totalPatientsValueLabel, activeAccountsValueLabel, newAccountsValueLabel;
    @FXML private Label totalDoctorsTrendLabel, totalPatientsTrendLabel, activeAccountsTrendLabel, newAccountsTrendLabel;

    @FXML private Label patientsTodayLabel, activeConsultationsLabel, doctorOccupancyLabel;
    @FXML private AreaChart<String, Number> flowChart;
    @FXML private PieChart specializationPieChart;


    @FXML private Button staffTabButton, patientsTabButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter, statusFilter;
    @FXML private VBox usersContainer;
    @FXML private Label paginationLabel;
    @FXML private Button prevPageButton, nextPageButton;
    @FXML private Label statusLabel;


    @FXML private DatePicker appointmentsFilterDatePicker;
    @FXML private ComboBox<String> appointmentsStatusFilter;
    @FXML private Button clearAppointmentsFilterButton;
    @FXML private GridPane appointmentsTableHeader;
    @FXML private VBox appointmentsContainer;

    private AdminUsersPresenter presenter;
    private List<AdminUserDTO> allUsers = new ArrayList<>();
    private List<AdminUserDTO> filteredUsers = new ArrayList<>();
    private List<SpecializationDTO> specializations = new ArrayList<>();
    private int currentPage = 1;
    private TabSelection currentTab = TabSelection.STAFF;
    private final List<RoleOption> roleOptions = List.of(
            new RoleOption("Doctor", "DOCTOR"),
            new RoleOption("Pacient", "PATIENT"),
            new RoleOption("Manager", "MANAGER"),
            new RoleOption("Administrator", "ADMIN")
    );

    private enum TabSelection { STAFF, PATIENTS }

    @FXML
    public void initialize() {
        presenter = new AdminUsersPresenter(this);
        renderUserHeader(ClientSession.getInstance().getLoggedUser());
        if (statusLabel != null) statusLabel.setText("");

        setupFilters();
        setupAppointmentsTableHeader();
        setupAppointmentsStatusFilter();

        if (appointmentsFilterDatePicker != null) {
            appointmentsFilterDatePicker.valueProperty().addListener((obs, oldVal, newVal) ->
                    presenter.onAppointmentsFilterDateSelected(newVal));
        }


        presenter.loadSpecializations();
        presenter.loadStats();
        presenter.loadManagerStats();
        presenter.loadUsers();
        presenter.loadAppointments();
    }


    public void renderManagerStats(StatsDTO stats) {
        if (stats == null) return;

        if (patientsTodayLabel != null) patientsTodayLabel.setText(String.valueOf(stats.getPatientsToday()));
        if (activeConsultationsLabel != null) activeConsultationsLabel.setText(String.valueOf(stats.getActiveConsultations()));
        if (doctorOccupancyLabel != null) doctorOccupancyLabel.setText(String.format("%.1f%%", stats.getDoctorOccupancyPercent()));

        if (specializationPieChart != null) {
            specializationPieChart.getData().clear();
            if (stats.getOccupancyBySpecialization() != null) {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                for (ChartPointDTO point : stats.getOccupancyBySpecialization()) {
                    pieData.add(new PieChart.Data(point.getLabel() + " (" + (int)point.getValue() + ")", point.getValue()));
                }
                specializationPieChart.setData(pieData);
            }
        }

        if (flowChart != null) {
            flowChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Pacienți");
            if (stats.getSeries() != null) {
                for (ChartPointDTO point : stats.getSeries()) {
                    series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
                }
            }
            flowChart.getData().add(series);
        }
    }


    private HBox buildAppointmentActions(AppointmentDTO appt) {
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (appt == null) return actions;

        actions.getChildren().add(createActionButton("Istoric", "table-action-info", () -> {
            ClientSession.getInstance().setSelectedAppointment(appt);
            ClientSession.getInstance().setPreviousScene(AppScene.ADMIN_DASHBOARD);
            SceneNavigator.navigateToFresh(AppScene.MANAGER_PATIENT_HISTORY);
        }));

        actions.getChildren().add(createActionButton("Status", "table-action", () -> openStatusDialog(appt)));

        return actions;
    }

    private void openStatusDialog(AppointmentDTO appt) {
        List<String> choices = List.of("PENDING", "CONFIRMED", "CANCELED", "DONE");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(appt.getStatus(), choices);
        dialog.setTitle("Schimbă Status");
        dialog.setHeaderText("Modifică status pentru: " + appt.getPatientName());
        dialog.setContentText("Status nou:");

        dialog.showAndWait().ifPresent(newStatus -> {
            if (!newStatus.equals(appt.getStatus())) {
                presenter.updateAppointmentStatus(appt.getAppointmentId(), newStatus);
            }
        });
    }

    @FXML public void onNavDashboardClick() { presenter.loadStats(); presenter.loadManagerStats(); presenter.loadUsers(); setInfo("Datele actualizate."); }
    @FXML public void onNavUsersClick() { setInfo("Administrare utilizatori."); }
    @FXML public void onNavAppointmentsClick() { SceneNavigator.navigateTo(AppScene.APPOINTMENT_BOOKING); }
    @FXML public void onNavDepartmentsClick() { setInfo("In lucru."); }
    @FXML public void onNavSettingsClick() { setInfo("In lucru."); }
    @FXML public void onLogoutClick() {
        ClientSession.getInstance().setLoggedUser(null);
        ClientSession.getInstance().clearSelectedAppointment();
        ClientSession.getInstance().clearEditMode();
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    @FXML public void onNewAppointmentClick() {
        ClientSession.getInstance().clearEditMode();
        ClientSession.getInstance().clearSelectedAppointment();
        SceneNavigator.navigateToFresh(AppScene.APPOINTMENT_BOOKING);
    }
    @FXML public void onClearAppointmentsFilterClick() { presenter.onClearAppointmentsFilter(); }
    @FXML public void onAddUserClick() { showUserDialog(null); }

    @FXML public void onStaffTabClick() { if (currentTab != TabSelection.STAFF) { currentTab = TabSelection.STAFF; updateTabStyles(); applyFilters(); } }
    @FXML public void onPatientsTabClick() { if (currentTab != TabSelection.PATIENTS) { currentTab = TabSelection.PATIENTS; updateTabStyles(); applyFilters(); } }

    @FXML public void onPrevPageClick() { if (currentPage > 1) { currentPage--; refreshTable(); } }
    @FXML public void onNextPageClick() { if (currentPage < totalPages()) { currentPage++; refreshTable(); } }

    public void renderStats(AdminStatsDTO stats) {
        if (stats == null) { setError("Nu am primit statistici valide."); return; }
        totalDoctorsValueLabel.setText(formatNumber(stats.getTotalDoctors()));
        totalPatientsValueLabel.setText(formatNumber(stats.getTotalPatients()));
        activeAccountsValueLabel.setText(formatNumber(stats.getActiveAccounts()));
        newAccountsValueLabel.setText(formatNumber(stats.getNewAccountsToday()));
        updateTrendLabel(totalDoctorsTrendLabel, stats.getTotalDoctorsChange());
        updateTrendLabel(totalPatientsTrendLabel, stats.getTotalPatientsChange());
        updateTrendLabel(activeAccountsTrendLabel, stats.getActiveAccountsChange());
        updateTrendLabel(newAccountsTrendLabel, stats.getNewAccountsChange());
    }

    public void setUsers(List<AdminUserDTO> users) {
        allUsers = users == null ? new ArrayList<>() : new ArrayList<>(users);
        allUsers.sort(Comparator.comparing(AdminUserDTO::getFullName, String.CASE_INSENSITIVE_ORDER));
        applyFilters();
    }

    public void setSpecializations(List<SpecializationDTO> items) {
        specializations = items == null ? new ArrayList<>() : new ArrayList<>(items);
        departmentFilter.getItems().clear(); departmentFilter.getItems().add(ALL_DEPARTMENTS);
        for (SpecializationDTO dto : specializations) departmentFilter.getItems().add(dto.getName());
        departmentFilter.getSelectionModel().selectFirst();
    }

    public void setInfo(String message) { if(statusLabel!=null) { statusLabel.setText(message == null ? "" : message); statusLabel.setStyle("-fx-text-fill: #64748b;"); } }
    public void setError(String message) { if(statusLabel!=null) { statusLabel.setText(message == null ? "" : message); statusLabel.setStyle("-fx-text-fill: #dc2626;"); } }

    public void setAppointmentsFilterDate(LocalDate date) { if(appointmentsFilterDatePicker!=null) appointmentsFilterDatePicker.setValue(date); }
    public void setAppointmentsStatusFilter(String statusCode) { if(appointmentsStatusFilter!=null) appointmentsStatusFilter.getSelectionModel().select(labelForAppointmentStatus(statusCode)); }

    public void renderAppointments(List<AppointmentDTO> appointments) {
        if (appointmentsContainer == null) return;
        appointmentsContainer.getChildren().clear();
        if (appointments == null || appointments.isEmpty()) {
            Label empty = new Label("Nu exista programari pentru filtrele selectate.");
            empty.getStyleClass().add("table-text-muted");
            appointmentsContainer.getChildren().add(empty);
            return;
        }
        for (AppointmentDTO appt : appointments) {
            appointmentsContainer.getChildren().add(buildAppointmentRow(appt));
        }
    }

    private void setupAppointmentsTableHeader() {
        if (appointmentsTableHeader == null) return;
        appointmentsTableHeader.getChildren().clear();
        appointmentsTableHeader.getColumnConstraints().setAll(appointmentsTableColumnConstraints());
        addAppointmentsHeaderLabel("Pacient", 0);
        addAppointmentsHeaderLabel("Medic", 1);
        addAppointmentsHeaderLabel("Serviciu", 2);
        addAppointmentsHeaderLabel("Data / Ora", 3);
        addAppointmentsHeaderLabel("Status", 4);
        addAppointmentsHeaderLabel("Actiuni", 5);
    }

    private List<ColumnConstraints> appointmentsTableColumnConstraints() {
        ColumnConstraints c0 = new ColumnConstraints(); c0.setPercentWidth(24);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(20);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(16);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(16);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(12);
        ColumnConstraints c5 = new ColumnConstraints(); c5.setPercentWidth(12);
        return List.of(c0, c1, c2, c3, c4, c5);
    }

    private void addAppointmentsHeaderLabel(String text, int column) {
        Label label = new Label(text); label.getStyleClass().add("table-header-text");
        appointmentsTableHeader.add(label, column, 0);
    }

    private GridPane buildAppointmentRow(AppointmentDTO appt) {
        GridPane row = new GridPane(); row.getStyleClass().add("table-row");
        row.getColumnConstraints().addAll(appointmentsTableColumnConstraints());

        HBox patientCell = buildPersonCell(appt == null ? null : appt.getPatientName(), "Pacient");
        Label doctorName = new Label(valueOrDefault(appt == null ? null : appt.getDoctorName(), "Medic")); doctorName.getStyleClass().add("table-text");
        Label service = new Label(valueOrDefault(appt == null ? null : appt.getServiceName(), "-")); service.getStyleClass().add("table-text-muted");
        Label dateTime = new Label(formatAppointmentDateTime(appt == null ? null : appt.getDate(), appt == null ? null : appt.getTime())); dateTime.getStyleClass().add("table-text-muted");

        String rawStatus = appt == null ? null : appt.getStatus();
        Label status = new Label(appointmentStatusLabel(rawStatus));
        status.getStyleClass().add("status-pill"); status.getStyleClass().add(appointmentStatusStyle(rawStatus));

        HBox actions = buildAppointmentActions(appt);

        row.add(patientCell, 0, 0); row.add(doctorName, 1, 0); row.add(service, 2, 0);
        row.add(dateTime, 3, 0); row.add(status, 4, 0); row.add(actions, 5, 0);

        GridPane.setHgrow(patientCell, Priority.ALWAYS); GridPane.setHgrow(actions, Priority.ALWAYS);
        return row;
    }

    private HBox buildPersonCell(String name, String fallback) {
        String safeName = valueOrDefault(name, fallback);
        StackPane avatar = new StackPane(); avatar.getStyleClass().add("table-avatar");
        Label initials = new Label(initials(safeName)); initials.getStyleClass().add("table-avatar-text");
        avatar.getChildren().add(initials);
        Label label = new Label(safeName); label.getStyleClass().add("table-text");
        HBox cell = new HBox(8, avatar, label); cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private Button createActionButton(String text, String styleClass, Runnable handler) {
        Button button = new Button(text);
        if (styleClass.equals("table-action-info")) {
            button.setStyle("-fx-background-color: #0dcaf0; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
        } else {
            button.getStyleClass().add(styleClass);
        }
        button.setOnAction(event -> handler.run());
        return button;
    }

    private void openEditAppointment(AppointmentDTO appt) {
        if (appt == null) return;
        ClientSession.getInstance().setSelectedAppointment(appt);
        ClientSession.getInstance().setAppointmentToEdit(appt.getAppointmentId());
        SceneNavigator.navigateToFresh(AppScene.APPOINTMENT_BOOKING);
    }

    private String formatAppointmentDateTime(LocalDate date, LocalTime time) {
        if (date == null) return "-";
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd MMM").toFormatter(Locale.forLanguageTag("ro"));
        String dateText = date.format(formatter);
        String timeText = time == null ? "" : time.format(DateTimeFormatter.ofPattern("HH:mm"));
        return timeText.isBlank() ? dateText : dateText + ", " + timeText;
    }

    private String appointmentStatusLabel(String raw) {
        String normalized = normalizeAppointmentStatus(raw);
        return switch (normalized) { case "PENDING"->"In asteptare"; case "CONFIRMED"->"Confirmat"; case "DONE"->"Finalizat"; case "CANCELED"->"Anulat"; default->normalized; };
    }
    private String appointmentStatusStyle(String raw) {
        String normalized = normalizeAppointmentStatus(raw);
        return switch (normalized) { case "PENDING"->"status-pending"; case "CONFIRMED"->"status-confirmed"; case "DONE"->"status-done"; case "CANCELED"->"status-canceled"; default->"status-active"; };
    }
    private String normalizeAppointmentStatus(String raw) {
        if (raw == null) return "PENDING";
        String value = raw.trim().toUpperCase(Locale.ROOT);
        return value.equals("CANCELLED") ? "CANCELED" : value;
    }

    private void renderUserHeader(UserDTO user) {
        if (user == null) { adminNameLabel.setText("Administrator"); adminRoleLabel.setText("Admin"); adminInitialsLabel.setText("AD"); return; }
        String fullName = user.getFullName(); adminNameLabel.setText(fullName); adminRoleLabel.setText(roleLabel(user.getRole())); adminInitialsLabel.setText(initials(fullName));
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(ALL_STATUS, "Activ", "Inactiv", "Concediu")); statusFilter.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        departmentFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        updateTabStyles();
    }

    private void applyFilters() {
        String search = normalize(searchField.getText());
        String department = departmentFilter.getValue();
        String status = statusFilter.getValue();
        filteredUsers = new ArrayList<>();
        for (AdminUserDTO user : allUsers) {
            if (!matchesTab(user)) continue;
            if (!matchesSearch(user, search)) continue;
            if (!matchesDepartment(user, department)) continue;
            if (!matchesStatus(user, status)) continue;
            filteredUsers.add(user);
        }
        currentPage = 1; refreshTable();
    }

    private void refreshTable() {
        usersContainer.getChildren().clear();
        int total = filteredUsers.size();
        if (total == 0) {
            Label empty = new Label("Nu exista utilizatori pentru filtrele selectate.");
            empty.getStyleClass().add("table-text-muted");
            usersContainer.getChildren().add(empty);
            paginationLabel.setText("Afisez 0 din 0 rezultate");
            prevPageButton.setDisable(true); nextPageButton.setDisable(true);
            return;
        }
        int totalPages = totalPages();
        currentPage = Math.min(currentPage, totalPages);
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        for (int i = start; i < end; i++) usersContainer.getChildren().add(buildUserRow(filteredUsers.get(i)));
        paginationLabel.setText("Afisez " + (start + 1) + " - " + end + " din " + total + " rezultate");
        prevPageButton.setDisable(currentPage <= 1); nextPageButton.setDisable(currentPage >= totalPages);
    }

    private int totalPages() { return Math.max(1, (int) Math.ceil(filteredUsers.size() / (double) PAGE_SIZE)); }

    private GridPane buildUserRow(AdminUserDTO user) {
        GridPane row = new GridPane(); row.getStyleClass().add("table-row");
        row.getColumnConstraints().addAll(tableColumnConstraints());

        HBox nameCell = new HBox(8.0); nameCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        StackPane avatar = new StackPane(); avatar.getStyleClass().add("table-avatar");
        Label initials = new Label(initials(user.getFullName())); initials.getStyleClass().add("table-avatar-text");
        avatar.getChildren().add(initials);
        VBox nameBox = new VBox(2.0);
        Label nameLabel = new Label(valueOrDefault(user.getFullName(), "Utilizator")); nameLabel.getStyleClass().add("table-text");
        Label emailLabel = new Label(valueOrDefault(user.getEmail(), "-")); emailLabel.getStyleClass().add("table-text-muted");
        nameBox.getChildren().addAll(nameLabel, emailLabel); nameCell.getChildren().addAll(avatar, nameBox);

        VBox roleCell = new VBox(2.0);
        Label roleLabel = new Label(roleLabel(user.getRole())); roleLabel.getStyleClass().add("table-text");
        Label departmentLabel = new Label(valueOrDefault(user.getDepartment(), "-")); departmentLabel.getStyleClass().add("table-text-muted");
        roleCell.getChildren().addAll(roleLabel, departmentLabel);

        Label activityLabel = new Label(formatDateTime(user.getLastActivityDate(), user.getLastActivityTime())); activityLabel.getStyleClass().add("table-text-muted");
        Label status = new Label(statusLabel(user.getStatus())); status.getStyleClass().add("status-pill"); status.getStyleClass().add(statusStyle(user.getStatus()));

        HBox actions = new HBox(6.0);
        Button editButton = new Button("Editeaza"); editButton.getStyleClass().add("table-action"); editButton.setOnAction(event -> showUserDialog(user));
        Button deleteButton = new Button("Sterge"); deleteButton.getStyleClass().addAll("table-action", "table-action-danger"); deleteButton.setOnAction(event -> confirmDelete(user));
        actions.getChildren().addAll(editButton, deleteButton);

        row.add(nameCell, 0, 0); row.add(roleCell, 1, 0); row.add(activityLabel, 2, 0); row.add(status, 3, 0); row.add(actions, 4, 0);
        GridPane.setHgrow(nameCell, Priority.ALWAYS); GridPane.setHgrow(actions, Priority.ALWAYS);
        return row;
    }

    private List<ColumnConstraints> tableColumnConstraints() {
        List<ColumnConstraints> cols = new ArrayList<>();
        cols.add(column(34)); cols.add(column(20)); cols.add(column(18)); cols.add(column(14)); cols.add(column(14));
        return cols;
    }
    private ColumnConstraints column(double percent) { ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(percent); return c; }

    private void confirmDelete(AdminUserDTO user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); alert.setTitle("Confirmare stergere"); alert.setHeaderText("Stergi utilizatorul " + user.getFullName() + "?"); alert.setContentText("Actiunea nu poate fi anulata.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) presenter.deleteUser(user.getUserId());
    }

    private void showUserDialog(AdminUserDTO user) {
        boolean isEdit = user != null;
        Dialog<UserFormData> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Editeaza utilizator" : "Adauga utilizator");
        dialog.setHeaderText(isEdit ? "Actualizeaza informatiile utilizatorului." : "Completeaza datele pentru utilizatorul nou.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField firstNameField = new TextField(); TextField lastNameField = new TextField(); TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField(); TextField nationalIdField = new TextField();
        ComboBox<RoleOption> roleBox = new ComboBox<>(); roleBox.getItems().addAll(roleOptions);
        roleBox.setConverter(new StringConverter<>() {
            @Override public String toString(RoleOption object) { return object == null ? "" : object.label; }
            @Override public RoleOption fromString(String string) { return roleOptions.stream().filter(option -> Objects.equals(option.label, string)).findFirst().orElse(null); }
        });
        ComboBox<SpecializationDTO> specializationBox = new ComboBox<>(); specializationBox.getItems().addAll(specializations);
        specializationBox.setConverter(new StringConverter<>() {
            @Override public String toString(SpecializationDTO object) { return object == null ? "" : object.getName(); }
            @Override public SpecializationDTO fromString(String string) { return specializations.stream().filter(spec -> Objects.equals(spec.getName(), string)).findFirst().orElse(null); }
        });

        int row = 0;
        grid.add(new Label("Prenume"), 0, row); grid.add(firstNameField, 1, row++);
        grid.add(new Label("Nume"), 0, row); grid.add(lastNameField, 1, row++);
        grid.add(new Label("Email"), 0, row); grid.add(emailField, 1, row++);
        grid.add(new Label("Rol"), 0, row); grid.add(roleBox, 1, row++);
        Label passwordLabel = new Label(isEdit ? "Parola (optional)" : "Parola");
        grid.add(passwordLabel, 0, row); grid.add(passwordField, 1, row++);
        Label specializationLabel = new Label("Specializare"); grid.add(specializationLabel, 0, row); grid.add(specializationBox, 1, row++);
        Label nationalIdLabel = new Label("CNP"); grid.add(nationalIdLabel, 0, row); grid.add(nationalIdField, 1, row);

        if (isEdit) {
            firstNameField.setText(user.getFirstName()); lastNameField.setText(user.getLastName()); emailField.setText(user.getEmail());
            roleBox.getSelectionModel().select(roleOptionFor(user.getRole()));
            if (user.getSpecializationId() != null) specializationBox.getSelectionModel().select(findSpecialization(user.getSpecializationId()));
            nationalIdField.setText(user.getNationalId());
        } else { roleBox.getSelectionModel().selectFirst(); }

        applyRoleVisibility(roleBox.getValue(), specializationLabel, specializationBox, nationalIdLabel, nationalIdField);
        roleBox.valueProperty().addListener((obs, oldVal, newVal) -> applyRoleVisibility(newVal, specializationLabel, specializationBox, nationalIdLabel, nationalIdField));

        Runnable validate = () -> {
            boolean basic = !firstNameField.getText().isBlank() && !lastNameField.getText().isBlank() && !emailField.getText().isBlank() && roleBox.getValue() != null;
            boolean needsPassword = !isEdit && passwordField.getText().isBlank();
            boolean doctorNeedsSpec = "DOCTOR".equalsIgnoreCase(roleValue(roleBox.getValue())) && specializationBox.getValue() == null;
            okButton.setDisable(!basic || needsPassword || doctorNeedsSpec);
        };
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validate.run()); lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validate.run()); passwordField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        roleBox.valueProperty().addListener((obs, oldVal, newVal) -> validate.run()); specializationBox.valueProperty().addListener((obs, oldVal, newVal) -> validate.run());
        validate.run();

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            RoleOption role = roleBox.getValue(); SpecializationDTO specialization = specializationBox.getValue();
            UserFormData data = new UserFormData();
            data.userId = isEdit ? user.getUserId() : null; data.firstName = firstNameField.getText().trim(); data.lastName = lastNameField.getText().trim();
            data.email = emailField.getText().trim(); data.password = passwordField.getText(); data.role = roleValue(role);
            data.specializationId = specialization == null ? null : specialization.getSpecializationId();
            data.nationalId = nationalIdField.getText().trim();
            return data;
        });

        dialog.showAndWait().ifPresent(data -> { if (isEdit) presenter.updateUser(data); else presenter.createUser(data); });
    }

    private void applyRoleVisibility(RoleOption role, Label specializationLabel, ComboBox<SpecializationDTO> specializationBox, Label nationalIdLabel, TextField nationalIdField) {
        String value = roleValue(role); boolean isDoctor = "DOCTOR".equalsIgnoreCase(value); boolean isPatient = "PATIENT".equalsIgnoreCase(value);
        specializationLabel.setManaged(isDoctor); specializationLabel.setVisible(isDoctor); specializationBox.setManaged(isDoctor); specializationBox.setVisible(isDoctor);
        nationalIdLabel.setManaged(isPatient); nationalIdLabel.setVisible(isPatient); nationalIdField.setManaged(isPatient); nationalIdField.setVisible(isPatient);
    }

    private RoleOption roleOptionFor(String role) { return roleOptions.stream().filter(option -> option.value.equalsIgnoreCase(role)).findFirst().orElse(null); }
    private String roleValue(RoleOption option) { return option == null ? "" : option.value; }
    private SpecializationDTO findSpecialization(Long id) { if (id == null) return null; for (SpecializationDTO dto : specializations) if (dto.getSpecializationId() == id) return dto; return null; }
    private boolean matchesTab(AdminUserDTO user) { if (user == null) return false; if (currentTab == TabSelection.PATIENTS) return "PATIENT".equalsIgnoreCase(user.getRole()); return !"PATIENT".equalsIgnoreCase(user.getRole()); }
    private boolean matchesSearch(AdminUserDTO user, String search) { if (search.isBlank()) return true; String fullName = normalize(user.getFullName()); String email = normalize(user.getEmail()); String nationalId = normalize(user.getNationalId()); String department = normalize(user.getDepartment()); return fullName.contains(search) || email.contains(search) || nationalId.contains(search) || department.contains(search); }
    private boolean matchesDepartment(AdminUserDTO user, String department) { if (department == null || department.isBlank() || ALL_DEPARTMENTS.equals(department)) return true; return Objects.equals(user.getDepartment(), department); }
    private boolean matchesStatus(AdminUserDTO user, String status) { if (status == null || status.isBlank() || ALL_STATUS.equals(status)) return true; return Objects.equals(statusLabel(user.getStatus()), status); }
    private String statusLabel(String raw) { if (raw == null) return "Activ"; return switch (raw.toUpperCase()) { case "ACTIVE"->"Activ"; case "INACTIVE"->"Inactiv"; case "ON_LEAVE"->"Concediu"; default->raw; }; }
    private String statusStyle(String raw) { if (raw == null) return "status-active"; return switch (raw.toUpperCase()) { case "ACTIVE"->"status-active"; case "INACTIVE"->"status-inactive"; case "ON_LEAVE"->"status-leave"; default->"status-active"; }; }
    private String roleLabel(String role) { if (role == null) return "Utilizator"; return switch (role.toUpperCase()) { case "ADMIN"->"Administrator"; case "MANAGER"->"Manager"; case "DOCTOR"->"Medic"; case "PATIENT"->"Pacient"; default->role; }; }
    private String formatDateTime(LocalDate date, LocalTime time) { if (date == null) return "-"; DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("ro")); String dateText = date.format(dateFormatter); String timeText = time == null ? "" : time.format(DateTimeFormatter.ofPattern("HH:mm")); return timeText.isBlank() ? dateText : dateText + ", " + timeText; }
    private String formatNumber(long value) { DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("ro")); DecimalFormat formatter = new DecimalFormat("#,###", symbols); return formatter.format(value); }
    private void updateTrendLabel(Label label, double value) { if (label == null) return; label.getStyleClass().removeAll("stat-tag-up", "stat-tag-down"); if (value >= 0) label.getStyleClass().add("stat-tag-up"); else label.getStyleClass().add("stat-tag-down"); label.setText(formatChange(value)); }
    private String formatChange(double value) { String sign = value >= 0 ? "+" : ""; return sign + Math.round(value) + "%"; }
    private void setupAppointmentsStatusFilter() { if (appointmentsStatusFilter == null) return; appointmentsStatusFilter.setItems(FXCollections.observableArrayList("Toate", "In asteptare", "Confirmate", "Finalizate", "Anulate")); appointmentsStatusFilter.getSelectionModel().selectFirst(); appointmentsStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> presenter.onAppointmentsTabSelected(statusCodeForAppointmentLabel(newVal))); }
    private String statusCodeForAppointmentLabel(String label) { if (label == null) return "ALL"; return switch (label.trim().toLowerCase(Locale.ROOT)) { case "in asteptare"->"PENDING"; case "confirmate"->"CONFIRMED"; case "finalizate"->"DONE"; case "anulate"->"CANCELED"; default->"ALL"; }; }
    private String labelForAppointmentStatus(String statusCode) { String normalized = normalizeAppointmentStatus(statusCode); return switch (normalized) { case "PENDING"->"In asteptare"; case "CONFIRMED"->"Confirmate"; case "DONE"->"Finalizate"; case "CANCELED"->"Anulate"; default->"Toate"; }; }
    private void updateTabStyles() { if (staffTabButton == null || patientsTabButton == null) return; staffTabButton.getStyleClass().remove("tab-active"); patientsTabButton.getStyleClass().remove("tab-active"); if (currentTab == TabSelection.STAFF) staffTabButton.getStyleClass().add("tab-active"); else patientsTabButton.getStyleClass().add("tab-active"); }
    private String initials(String name) { if (name == null || name.isBlank()) return "NA"; String[] parts = name.trim().split("\\s+"); if (parts.length == 1) return ("" + parts[0].charAt(0)).toUpperCase(Locale.ROOT); return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT); }
    private String valueOrDefault(String value, String fallback) { if (value == null || value.isBlank()) return fallback; return value; }
    private String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
    public static class UserFormData { public Long userId; public String firstName; public String lastName; public String email; public String role; public String password; public Long specializationId; public String nationalId; }
    private static class RoleOption { private final String label; private final String value; private RoleOption(String label, String value) { this.label = label; this.value = value; } }
}