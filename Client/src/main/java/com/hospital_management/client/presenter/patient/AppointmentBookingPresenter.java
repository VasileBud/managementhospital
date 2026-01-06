package com.hospital_management.client.presenter.patient;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.patient.AppointmentBookingView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.CommandDTO;
import shared.dto.DoctorDTO;
import shared.dto.MedicalServiceDTO;
import shared.dto.SpecializationDTO;
import shared.dto.UserDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AppointmentBookingPresenter {

    private final AppointmentBookingView view;
    private final SpecializationDTO allSpecializations =
            new SpecializationDTO(0, "Toate specializarile");

    private List<DoctorDTO> doctors = Collections.emptyList();
    private List<SpecializationDTO> specializations = Collections.emptyList();
    private List<MedicalServiceDTO> services = Collections.emptyList();
    private List<CommandDTO.Action> pendingActions = Collections.emptyList();
    private CommandDTO.Action currentAction;
    private boolean initialLoading = false;

    private SpecializationDTO selectedSpecialization;
    private DoctorDTO selectedDoctor;
    private MedicalServiceDTO selectedService;
    private LocalDate selectedDate;
    private LocalTime selectedTime;

    public AppointmentBookingPresenter(AppointmentBookingView view) {
        this.view = view;
    }

    public void loadInitialData() {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }
        initialLoading = true;
        pendingActions = List.of(
                CommandDTO.Action.GET_SPECIALIZATIONS,
                CommandDTO.Action.GET_DOCTORS,
                CommandDTO.Action.GET_MEDICAL_SERVICES
        );
        view.setBusy(true);
        view.setInfo("Se incarca datele...");
        sendNext();
    }

    public void onSpecializationSelected(SpecializationDTO specialization) {
        selectedSpecialization = specialization;
        if (initialLoading) {
            updateSummary();
            return;
        }
        List<DoctorDTO> filtered = filterDoctors(specialization);
        view.setDoctors(filtered);
        selectedDoctor = null;
        selectedTime = null;
        view.setSelectedTime(null);
        loadAvailableSlots();
        updateSummary();
    }

    public void onDoctorSelected(DoctorDTO doctor) {
        selectedDoctor = doctor;
        if (initialLoading) {
            updateSummary();
            return;
        }
        selectedTime = null;
        view.setSelectedTime(null);
        loadAvailableSlots();
        updateSummary();
    }

    public void onServiceSelected(MedicalServiceDTO service) {
        selectedService = service;
        if (initialLoading) {
            updateSummary();
            return;
        }
        updateSummary();
    }

    public void onDateSelected(LocalDate date) {
        selectedDate = date;
        if (initialLoading) {
            updateSummary();
            return;
        }
        selectedTime = null;
        view.setSelectedTime(null);
        loadAvailableSlots();
        updateSummary();
    }

    public void onTimeSelected(LocalTime time) {
        selectedTime = time;
        view.setSelectedTime(time);
        updateSummary();
    }

    public void onBook() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator invalid.");
            return;
        }
        if (user.getPatientId() == null) {
            view.setError("Profil pacient lipsa.");
            return;
        }
        if (selectedDoctor == null || selectedDate == null || selectedTime == null) {
            view.setError("Selecteaza medicul, data si ora.");
            return;
        }

        Long editId = ClientSession.getInstance().getAppointmentToEdit();

        CommandDTO cmd;

        if (editId != null) {
            cmd = new CommandDTO(CommandDTO.Action.UPDATE_APPOINTMENT, user.getUserId())
                    .put("appointmentId", editId)
                    .put("patientId", user.getPatientId())
                    .put("doctorId", selectedDoctor.getDoctorId())
                    .put("date", selectedDate)
                    .put("time", selectedTime);

            view.setInfo("Se actualizează programarea...");
            currentAction = CommandDTO.Action.UPDATE_APPOINTMENT;

        } else {
            cmd = new CommandDTO(CommandDTO.Action.BOOK_APPOINTMENT, user.getUserId())
                    .put("patientId", user.getPatientId())
                    .put("doctorId", selectedDoctor.getDoctorId())
                    .put("date", selectedDate)
                    .put("time", selectedTime);

            view.setInfo("Se creează programarea...");
            currentAction = CommandDTO.Action.BOOK_APPOINTMENT;
        }

        if (selectedService != null) {
            cmd.put("serviceId", selectedService.getServiceId());
        }

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        currentAction = CommandDTO.Action.BOOK_APPOINTMENT;
        view.setBusy(true);
        view.setInfo("Se creeaza programarea...");
        ClientSession.getInstance().getClient().setOnResponseReceived(this::handleResponse);
        ClientSession.getInstance().getClient().sendRequest(req);
    }

    private void sendNext() {
        if (pendingActions.isEmpty()) {
            view.setBusy(false);
            view.setInfo("");
            initialLoading = false;
            return;
        }

        currentAction = pendingActions.get(0);
        pendingActions = pendingActions.subList(1, pendingActions.size());

        CommandDTO cmd = new CommandDTO(currentAction);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().setOnResponseReceived(this::handleResponse);
        ClientSession.getInstance().getClient().sendRequest(req);
    }

    private void handleResponse(Response response) {
        Platform.runLater(() -> {
            if (response.getStatus() != Response.Status.OK) {
                view.setBusy(false);
                view.setError("Eroare: " + response.getMessage());
                return;
            }

            switch (currentAction) {
                case GET_SPECIALIZATIONS -> {
                    specializations = castList(response.getPayload());
                    view.setSpecializations(specializations, allSpecializations);
                    selectedSpecialization = allSpecializations;
                }
                case GET_DOCTORS -> {
                    doctors = castList(response.getPayload());
                    view.setDoctors(doctors);
                }
                case GET_MEDICAL_SERVICES -> {
                    services = castList(response.getPayload());
                    view.setServices(services);
                }
                case GET_AVAILABLE_SLOTS -> {
                    List<LocalTime> times = castList(response.getPayload());
                    view.setAvailableTimes(times);
                    view.setBusy(false);
                    view.setInfo("");
                    return;
                }
                case BOOK_APPOINTMENT -> {
                    view.setBusy(false);
                    view.setInfo("Programare creata cu succes.");
                    SceneNavigator.navigateToFresh(AppScene.PATIENT_DASHBOARD);
                    return;
                }
                case UPDATE_APPOINTMENT -> {
                    view.setBusy(false);
                    view.setInfo("Programare actualizata cu succes.");

                    ClientSession.getInstance().clearEditMode();

                    SceneNavigator.navigateToFresh(AppScene.PATIENT_DASHBOARD);
                    return;
                }
                default -> view.setInfo("Raspuns primit.");
            }

            sendNext();
        });
    }

    private void loadAvailableSlots() {
        if (initialLoading) {
            return;
        }
        if (selectedDoctor == null || selectedDate == null) {
            view.setAvailableTimes(Collections.emptyList());
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_AVAILABLE_SLOTS)
                .put("doctorId", selectedDoctor.getDoctorId())
                .put("date", selectedDate);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        currentAction = CommandDTO.Action.GET_AVAILABLE_SLOTS;
        view.setBusy(true);
        view.setInfo("Se incarca orele disponibile...");
        ClientSession.getInstance().getClient().setOnResponseReceived(this::handleResponse);
        ClientSession.getInstance().getClient().sendRequest(req);
    }

    private List<DoctorDTO> filterDoctors(SpecializationDTO spec) {
        if (spec == null || spec.getSpecializationId() == 0) {
            return doctors;
        }
        return doctors.stream()
                .filter(d -> Objects.equals(d.getSpecializationName(), spec.getName()))
                .toList();
    }

    private void updateSummary() {
        String doctorName = selectedDoctor == null ? null : selectedDoctor.getFullName();
        String specName = null;
        if (selectedDoctor != null) {
            specName = selectedDoctor.getSpecializationName();
        } else if (selectedSpecialization != null && selectedSpecialization.getSpecializationId() != 0) {
            specName = selectedSpecialization.getName();
        }
        String serviceName = selectedService == null ? null : selectedService.getName();
        String price = selectedService == null || selectedService.getPrice() == null
                ? null
                : selectedService.getPrice().toPlainString() + " RON";

        view.updateSummary(doctorName, specName, selectedDate, selectedTime, serviceName, price);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castList(Object data) {
        if (data instanceof List<?>) {
            return (List<T>) data;
        }
        return Collections.emptyList();
    }
}
