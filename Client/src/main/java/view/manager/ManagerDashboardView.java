package view.manager;

import app.AppScene;
import app.SceneNavigator;
import app.ClientSession;
import presenter.manager.ManagerDashboardPresenter;
import presenter.manager.ManagerStatsPresenter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.dto.AppointmentDTO;
import model.dto.ChartPointDTO;
import model.dto.StatsDTO;
import model.dto.UserDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerDashboardView {

    @FXML private Label managerNameLabel;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> serviceFilterCombo;
    @FXML private TableView<AppointmentDTO> appointmentsTable;
    @FXML private TableColumn<AppointmentDTO, String> timeCol;
    @FXML private TableColumn<AppointmentDTO, String> doctorCol;
    @FXML private TableColumn<AppointmentDTO, String> patientCol;
    @FXML private TableColumn<AppointmentDTO, String> serviceCol;
    @FXML private TableColumn<AppointmentDTO, String> statusCol;

    @FXML private Label patientsTodayLabel;
    @FXML private Label activeConsultationsLabel;
    @FXML private Label doctorOccupancyLabel;

    @FXML private AreaChart<String, Number> flowChart;
    @FXML private PieChart specializationPieChart;

    @FXML private Label statusLabel;

    private ManagerDashboardPresenter dashboardPresenter;
    private ManagerStatsPresenter statsPresenter;

    @FXML
    public void initialize() {
        dashboardPresenter = new ManagerDashboardPresenter(this);
        statsPresenter = new ManagerStatsPresenter(this);

        setupTableColumns();

        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            datePicker.valueProperty().addListener((obs, old, newVal) -> refreshData());
        }

        if (serviceFilterCombo != null) {
            serviceFilterCombo.setItems(FXCollections.observableArrayList("Toate"));
            serviceFilterCombo.setValue("Toate");
            serviceFilterCombo.setOnAction(e -> refreshData());
        }

        Platform.runLater(() -> {
            dashboardPresenter.loadDashboardData();
            statsPresenter.loadStats();
        });
    }


    public void renderStats(StatsDTO stats) {
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

            if (stats.getSeries() != null && !stats.getSeries().isEmpty()) {
                for (ChartPointDTO point : stats.getSeries()) {
                    series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
                }
            } else {
                series.getData().add(new XYChart.Data<>("Fără date", 0));
            }

            flowChart.getData().add(series);
        }
    }

    public void refreshStats() {
        if (statsPresenter != null) {
            statsPresenter.loadStats();
        }
    }

    private void setupTableColumns() {
        if (timeCol != null)
            timeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTime().toString()));
        if (doctorCol != null)
            doctorCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDoctorName()));
        if (patientCol != null)
            patientCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getPatientName()));
        if (serviceCol != null)
            serviceCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getServiceName()));

        if (statusCol != null) {
            statusCol.setCellValueFactory(cell ->
                    new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus())
            );

            statusCol.setCellFactory(column -> new TableCell<AppointmentDTO, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label lbl = new Label(item);
                        lbl.getStyleClass().add("status-badge");

                        switch (item.toUpperCase()) {
                            case "CONFIRMED":
                            case "DONE":
                            case "ACTIV":
                                lbl.getStyleClass().add("status-success");
                                break;
                            case "CANCELED":
                            case "ANULAT":
                                lbl.getStyleClass().add("status-danger");
                                break;
                            case "PENDING":
                            case "ASTEPTARE":
                                lbl.getStyleClass().add("status-warning");
                                break;
                            default:
                                lbl.getStyleClass().add("status-neutral");
                                break;
                        }

                        HBox container = new HBox(lbl);
                        container.setAlignment(Pos.CENTER);
                        setGraphic(container);
                        setText(null);
                    }
                }
            });
        }
    }

    private void refreshData() {
        if (datePicker != null && serviceFilterCombo != null) {
            dashboardPresenter.onFilterChanged(datePicker.getValue(), serviceFilterCombo.getValue());
        }
    }

    public void updateUserInfo(UserDTO user) {
        if (user != null && managerNameLabel != null) {
            managerNameLabel.setText(user.getFullName());
        }
    }

    public void updateAppointmentsTable(List<AppointmentDTO> list) {
        if (appointmentsTable != null) {
            AppointmentDTO currentSelection = appointmentsTable.getSelectionModel().getSelectedItem();
            long selectedId = (currentSelection != null) ? currentSelection.getAppointmentId() : -1;

            appointmentsTable.setItems(FXCollections.observableArrayList(list));

            updateServiceFilter(list);

            if (selectedId != -1) {
                for (AppointmentDTO item : appointmentsTable.getItems()) {
                    if (item.getAppointmentId() == selectedId) {
                        appointmentsTable.getSelectionModel().select(item);
                        break;
                    }
                }
            }
        }
    }

    private void updateServiceFilter(List<AppointmentDTO> list) {
        if (serviceFilterCombo == null) return;
        serviceFilterCombo.setOnAction(null);

        String current = serviceFilterCombo.getValue();
        List<String> services = list.stream()
                .map(AppointmentDTO::getServiceName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        services.add(0, "Toate");

        serviceFilterCombo.setItems(FXCollections.observableArrayList(services));

        if (services.contains(current)) {
            serviceFilterCombo.setValue(current);
        } else {
            serviceFilterCombo.setValue("Toate");
        }
        serviceFilterCombo.setOnAction(e -> refreshData());
    }


    public LocalDate getSelectedDate() {
        return datePicker != null ? datePicker.getValue() : LocalDate.now();
    }

    public String getSelectedService() {
        return serviceFilterCombo != null ? serviceFilterCombo.getValue() : "Toate";
    }

    public void setBusy(boolean busy) {
        if (statusLabel != null) {
            if (busy) {
                statusLabel.setText("Se încarcă datele...");
                statusLabel.setStyle("-fx-text-fill: gray;");
            } else if ("Se încarcă datele...".equals(statusLabel.getText())) {
                statusLabel.setText("");
            }
        }
    }

    public void setInfo(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
        }
    }

    public void setError(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void onLogoutClick() {
        dashboardPresenter.onLogout();
    }

    @FXML
    public void onEditUserClick() {
        setError("Funcționalitate în lucru...");
    }

    @FXML
    public void onChangeStatusClick() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setError("Selectează o programare din tabel pentru a modifica statusul!");
            return;
        }

        List<String> choices = List.of("PENDING", "CONFIRMED", "CANCELED", "DONE");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus(), choices);
        dialog.setTitle("Modificare Status");
        dialog.setHeaderText("Schimbă statusul pentru: " + selected.getPatientName());
        dialog.setContentText("Alege noul status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            if (!newStatus.equals(selected.getStatus())) {
                dashboardPresenter.updateAppointmentStatus(selected.getAppointmentId(), newStatus);
            }
        });
    }

    @FXML
    public void onViewHistoryClick() {
        AppointmentDTO selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setError("Te rog selectează un pacient din tabel pentru a-i vedea istoricul!");
            return;
        }
        ClientSession.getInstance().setSelectedAppointment(selected);
        SceneNavigator.navigateToFresh(AppScene.MANAGER_PATIENT_HISTORY);
    }
}