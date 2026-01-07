package com.hospital_management.client.presenter.patient;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.patient.PublicView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PublicPresenter {

    private final PublicView view;
    private List<CommandDTO.Action> pendingActions = Collections.emptyList();
    private CommandDTO.Action currentAction;
    private List<DoctorDTO> doctors = Collections.emptyList();
    private List<SpecializationDTO> specializations = Collections.emptyList();
    private List<MedicalServiceDTO> services = Collections.emptyList();
    private String lastQuery = "";
    private String lastSpecialization = "";

    public PublicPresenter(PublicView view) {
        this.view = view;
    }

    public void loadAll() {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        pendingActions = List.of(
                CommandDTO.Action.GET_DOCTORS,
                CommandDTO.Action.GET_SPECIALIZATIONS
        );
        view.setBusy(true);
        view.setInfo("Se incarca informatiile publice...");
        sendNext();
    }

    public void onGoToLogin() {
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    public void onGoToRegister() {
        SceneNavigator.navigateTo(AppScene.REGISTER);
    }

    public void onSearch(String query, String specializationName) {
        if (!validateSearch(query, specializationName)) {
            return;
        }
        lastQuery = query == null ? "" : query.trim().toLowerCase();
        lastSpecialization = specializationName == null ? "" : specializationName.trim();
        applyFilters();
    }

    private void sendNext() {
        if (pendingActions.isEmpty()) {
            view.setBusy(false);
            view.setInfo("Informatiile au fost incarcate.");
            return;
        }

        currentAction = pendingActions.get(0);
        pendingActions = pendingActions.subList(1, pendingActions.size());

        CommandDTO cmd = new CommandDTO(currentAction);
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, this::handleResponse);
    }

    private void handleResponse(Response response) {
        Platform.runLater(() -> {
            System.out.println("DEBUG: Am primit raspuns pentru " + currentAction);
            System.out.println("DEBUG: Status: " + response.getStatus());
            System.out.println("DEBUG: Payload este null? " + (response.getData() == null));
            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                view.setBusy(false);
                return;
            }

            switch (currentAction) {
                case GET_DOCTORS -> {
                    doctors = castList(response.getData());
                    System.out.println("DEBUG: Lista doctori dupa cast: " + doctors.size());
                    applyFilters();
                    view.setInfo("Doctori incarcati: " + doctors.size());
                }
                case GET_SPECIALIZATIONS -> {
                    specializations = castList(response.getData());
                    view.setSpecializations(specializations);
                    view.setInfo("Specializari incarcate: " + specializations.size());
                }
                case GET_MEDICAL_SERVICES -> {
                    services = castList(response.getData());
                    view.setInfo("Servicii incarcate: " + services.size());
                }
                default -> view.setInfo("Raspuns primit.");
            }

            sendNext();
        });
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castList(Object data) {
        if (data instanceof List<?>) {
            return (List<T>) data;
        }
        return Collections.emptyList();
    }

    private void applyFilters() {
        System.out.println("DEBUG: Aplic filtre. Total: " + doctors.size() +
                ", Query: '" + lastQuery + "'" +
                ", Spec: '" + lastSpecialization + "'");
        if (doctors.isEmpty()) {
            view.setDoctors(Collections.emptyList());
            return;
        }

        String specFilter = lastSpecialization;
        boolean hasSpecFilter = specFilter != null && !specFilter.isBlank()
                && !specFilter.equalsIgnoreCase("Toate specializarile");

        List<DoctorDTO> filtered = doctors.stream()
                .filter(d -> {
                    String name = d.getFullName() == null ? "" : d.getFullName().toLowerCase();
                    String spec = d.getSpecializationName() == null ? "" : d.getSpecializationName();
                    boolean matchesQuery = lastQuery.isEmpty()
                            || name.contains(lastQuery)
                            || spec.toLowerCase().contains(lastQuery);
                    boolean matchesSpec = !hasSpecFilter || spec.equalsIgnoreCase(specFilter);
                    return matchesQuery && matchesSpec;
                })
                .collect(Collectors.toList());
        view.setDoctors(filtered);
    }

    private boolean validateSearch(String query, String specializationName) {
        String q = query == null ? "" : query.trim();
        String s = specializationName == null ? "" : specializationName.trim();
        if (q.length() > 100) {
            view.setError("Campul de cautare accepta maxim 100 caractere.");
            return false;
        }
        if (s.length() > 80) {
            view.setError("Specializarea selectata este invalida (max 80 caractere).");
            return false;
        }
        return true;
    }

    public void fetchDoctorSchedule(long doctorId, Consumer<List<DoctorScheduleDTO>> onSuccess, Consumer<String> onError) {
        if (!ClientSession.getInstance().ensureConnected()) {
            notifyError(onError, "Nu exista conexiune la server.");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_DOCTOR_SCHEDULE)
                .put("doctorId", doctorId);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() == Response.Status.OK) {
                    List<DoctorScheduleDTO> schedule = castList(response.getData());
                    if (onSuccess != null) {
                        onSuccess.accept(schedule);
                    }
                } else {
                    String message = response.getMessage();
                    if (message == null || message.isBlank()) {
                        message = "Nu pot incarca orarul medicului.";
                    }
                    notifyError(onError, message);
                }
            });
        });
    }

    public void fetchAvailableSlots(long doctorId, LocalDate date, Consumer<List<LocalTime>> onSuccess, Consumer<String> onError) {
        if (date == null) {
            notifyError(onError, "Selecteaza o data valida.");
            return;
        }
        if (!ClientSession.getInstance().ensureConnected()) {
            notifyError(onError, "Nu exista conexiune la server.");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_AVAILABLE_SLOTS)
                .put("doctorId", doctorId)
                .put("date", date);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> {
                if (response.getStatus() == Response.Status.OK) {
                    List<LocalTime> slots = castList(response.getData());
                    if (onSuccess != null) {
                        onSuccess.accept(slots);
                    }
                } else {
                    String message = response.getMessage();
                    if (message == null || message.isBlank()) {
                        message = "Nu pot incarca orele disponibile.";
                    }
                    notifyError(onError, message);
                }
            });
        });
    }

    private void notifyError(Consumer<String> onError, String message) {
        if (onError != null) {
            onError.accept(message);
        } else {
            view.setError(message);
        }
    }

}
