package presenter.patient;

import app.ClientSession;
import view.patient.AppointmentBookingView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;
import model.dto.AdminUserDTO;
import model.dto.AppointmentDTO;
import model.dto.CommandDTO;
import model.dto.DoctorDTO;
import model.dto.MedicalServiceDTO;
import model.dto.SpecializationDTO;
import model.dto.UserDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AppointmentBookingPresenter {

    private final AppointmentBookingView view;

    private static final SpecializationDTO ALL_SPECIALIZATIONS = new SpecializationDTO(-1, "Toate specializarile");

    private UserDTO user;
    private RoleMode role = RoleMode.PATIENT;
    private boolean initialLoading = false;

    private List<DoctorDTO> doctors = new ArrayList<>();
    private List<SpecializationDTO> specializations = new ArrayList<>();
    private List<MedicalServiceDTO> services = new ArrayList<>();
    private List<AdminUserDTO> patients = new ArrayList<>();

    private SpecializationDTO selectedSpecialization = ALL_SPECIALIZATIONS;
    private DoctorDTO selectedDoctor;
    private MedicalServiceDTO selectedService;
    private AdminUserDTO selectedPatient;
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private AppointmentDTO editingAppointment;
    private boolean suppressSelectionEvents = false;

    private enum RoleMode {
        PATIENT,
        DOCTOR,
        MANAGER,
        ADMIN;

        static RoleMode from(UserDTO user) {
            if (user == null || user.getRole() == null) {
                return PATIENT;
            }
            return switch (user.getRole().toUpperCase(Locale.ROOT)) {
                case "DOCTOR" -> DOCTOR;
                case "MANAGER" -> MANAGER;
                case "ADMIN" -> ADMIN;
                default -> PATIENT;
            };
        }
    }

    public AppointmentBookingPresenter(AppointmentBookingView view) {
        this.view = view;
    }

    public void loadInitialData() {
        user = ClientSession.getInstance().getLoggedUser();
        role = RoleMode.from(user);
        view.setRole(role.name());
        view.setEditMode(false, "");
        if (ClientSession.getInstance().getAppointmentToEdit() == null) {
            ClientSession.getInstance().clearSelectedAppointment();
        }

        if (user == null) {
            view.setError("Utilizator neautentificat.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        initialLoading = true;
        view.setBusy(true);
        view.setInfo("Se incarca datele...");
        loadDoctors();
    }

    public void onPatientSelected(AdminUserDTO patient) {
        selectedPatient = patient;
    }

    public void onSpecializationSelected(SpecializationDTO specialization) {
        selectedSpecialization = specialization == null ? ALL_SPECIALIZATIONS : specialization;
        if (initialLoading || suppressSelectionEvents) {
            return;
        }
        applyDoctorFilters();
    }

    public void onDoctorSelected(DoctorDTO doctor) {
        selectedDoctor = doctor;
        if (initialLoading || suppressSelectionEvents) {
            return;
        }
        selectedTime = null;
        view.setSelectedTime(null);
        loadAvailableSlots();
    }

    public void onServiceSelected(MedicalServiceDTO service) {
        selectedService = service;
    }

    public void onDateSelected(LocalDate date) {
        selectedDate = date;
        if (initialLoading || suppressSelectionEvents) {
            return;
        }
        selectedTime = null;
        view.setSelectedTime(null);
        loadAvailableSlots();
    }


    public void onTimeSelected(LocalTime time) {
        selectedTime = time;
        view.setSelectedTime(time);
    }

    public void onSave() {
        if (user == null) {
            view.setError("Utilizator neautentificat.");
            return;
        }
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        Long editId = ClientSession.getInstance().getAppointmentToEdit();
        boolean isEdit = editId != null;
        if (role == RoleMode.DOCTOR && selectedDoctor == null) {
            selectedDoctor = resolveDoctorForUser();
        }
        if (!validateAppointmentForm(isEdit)) {
            return;
        }

        Long requesterId = user == null ? null : user.getUserId();

        CommandDTO cmd;
        if (editId != null) {
            cmd = new CommandDTO(CommandDTO.Action.UPDATE_APPOINTMENT, requesterId)
                    .put("appointmentId", editId)
                    .put("doctorId", selectedDoctor.getDoctorId())
                    .put("date", selectedDate)
                    .put("time", selectedTime);
            view.setInfo("Se actualizeaza programarea...");
        } else {
            cmd = new CommandDTO(CommandDTO.Action.BOOK_APPOINTMENT, requesterId)
                    .put("doctorId", selectedDoctor.getDoctorId())
                    .put("date", selectedDate)
                    .put("time", selectedTime);
            if (role == RoleMode.PATIENT && user != null && user.getPatientId() != null) {
                cmd.put("patientId", user.getPatientId());
            } else if (selectedPatient != null) {
                cmd.put("patientUserId", selectedPatient.getUserId());
            }
            view.setInfo("Se creeaza programarea...");
        }

        if (selectedService != null) {
            cmd.put("serviceId", selectedService.getServiceId());
        }

        view.setBusy(true);
        sendRequest(cmd, response -> {
            view.setBusy(false);
            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                return;
            }
            view.setInfo(editId == null ? "Programarea a fost creata." : "Programarea a fost actualizata.");
            clearEditMode();
            resetForm();
        });
    }

    private boolean validateAppointmentForm(boolean editMode) {
        if (!editMode && role != RoleMode.PATIENT && selectedPatient == null) {
            view.setError("Selecteaza pacientul.");
            return false;
        }
        if (!editMode && role == RoleMode.PATIENT && user != null && user.getPatientId() == null) {
            view.setError("Profil pacient lipsa.");
            return false;
        }
        if (selectedDoctor == null) {
            view.setError(role == RoleMode.DOCTOR ? "Profil doctor lipsa." : "Selecteaza medicul.");
            return false;
        }
        if (selectedDate == null) {
            view.setError("Selecteaza data.");
            return false;
        }
        if (selectedTime == null) {
            view.setError("Selecteaza ora.");
            return false;
        }
        return true;
    }

    public void onReset() {
        clearEditMode();
        resetForm();
    }

    public void onClearEdit() {
        clearEditMode();
        resetForm();
    }

    public void onEditAppointment(AppointmentDTO appt) {
        if (appt == null) {
            view.setError("Programarea selectata nu este valida.");
            return;
        }
        editingAppointment = appt;
        ClientSession.getInstance().setSelectedAppointment(appt);
        ClientSession.getInstance().setAppointmentToEdit(appt.getAppointmentId());

        selectedDoctor = findDoctor(appt.getDoctorId());
        selectedService = findService(appt.getServiceName());
        selectedDate = appt.getDate();
        selectedTime = appt.getTime();
        selectedSpecialization = selectedDoctor == null
                ? ALL_SPECIALIZATIONS
                : Objects.requireNonNullElse(findSpecializationByName(selectedDoctor.getSpecializationName()), ALL_SPECIALIZATIONS);

        suppressSelectionEvents = true;
        view.setSelectedSpecialization(selectedSpecialization);
        applyDoctorFilters();
        view.setSelectedDoctor(selectedDoctor);
        view.setSelectedService(selectedService);
        if (selectedDate != null) {
            view.setSelectedDate(selectedDate);
        }
        suppressSelectionEvents = false;
        view.setSelectedTime(selectedTime);
        view.setEditMode(true, buildEditHint(appt));
        loadAvailableSlots();
    }

    private void loadDoctors() {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_DOCTORS);
        sendRequest(cmd, response -> {
            if (response.getStatus() != Response.Status.OK) {
                view.setBusy(false);
                view.setError("Eroare: " + response.getMessage());
                return;
            }
            doctors = castList(response.getData());
            view.setDoctors(doctors);
            loadSpecializations();
        });
    }

    private void loadSpecializations() {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_SPECIALIZATIONS);
        sendRequest(cmd, response -> {
            if (response.getStatus() != Response.Status.OK) {
                view.setBusy(false);
                view.setError("Eroare: " + response.getMessage());
                return;
            }
            List<SpecializationDTO> received = castList(response.getData());
            specializations = received.stream()
                    .sorted(Comparator.comparing(SpecializationDTO::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList();

            List<SpecializationDTO> items = new ArrayList<>();
            items.add(ALL_SPECIALIZATIONS);
            items.addAll(specializations);

            selectedSpecialization = ALL_SPECIALIZATIONS;
            view.setSpecializations(items);
            view.setSelectedSpecialization(selectedSpecialization);
            applyDoctorFilters();
            loadServices();
        });
    }

    private void loadServices() {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_MEDICAL_SERVICES);
        sendRequest(cmd, response -> {
            if (response.getStatus() != Response.Status.OK) {
                view.setBusy(false);
                view.setError("Eroare: " + response.getMessage());
                return;
            }
            services = castList(response.getData());
            view.setServices(services);
            if (role == RoleMode.PATIENT) {
                finishInitialLoad();
            } else {
                loadPatients();
            }
        });
    }

    private void loadPatients() {
        CommandDTO cmd = new CommandDTO(CommandDTO.Action.ADMIN_LIST_USERS);
        sendRequest(cmd, response -> {
            if (response.getStatus() != Response.Status.OK) {
                view.setBusy(false);
                view.setError("Eroare: " + response.getMessage());
                return;
            }
            List<AdminUserDTO> users = castList(response.getData());
            patients = users.stream()
                    .filter(user -> "PATIENT".equalsIgnoreCase(user.getRole()))
                    .sorted(Comparator.comparing(AdminUserDTO::getFullName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            view.setPatients(patients);
            finishInitialLoad();
        });
    }

    private void finishInitialLoad() {
        initialLoading = false;
        view.setBusy(false);
        view.setInfo("");
        applyDefaults();
        applyEditFromSession();
    }

    private void applyDefaults() {
        suppressSelectionEvents = true;
        selectedDate = view.getSelectedDate();
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
            view.setSelectedDate(selectedDate);
        }

        view.setSpecializationFilterVisible(role != RoleMode.DOCTOR);
        if (role == RoleMode.DOCTOR) {
            selectedSpecialization = ALL_SPECIALIZATIONS;
        } else if (selectedSpecialization == null) {
            selectedSpecialization = ALL_SPECIALIZATIONS;
        }
        view.setSelectedSpecialization(selectedSpecialization);
        applyDoctorFilters();

        if (role == RoleMode.DOCTOR && user != null) {
            selectedDoctor = resolveDoctorForUser();
            view.lockDoctor(selectedDoctor == null ? user.getFullName() : selectedDoctor.getFullName());
            if (selectedDoctor != null && doctors.contains(selectedDoctor)) {
                view.setSelectedDoctor(selectedDoctor);
            }
        } else {
            view.unlockDoctor();
        }

        if (role == RoleMode.PATIENT && user != null) {
            view.lockPatient(user.getFullName());
        } else {
            view.unlockPatient();
        }

        suppressSelectionEvents = false;
        loadAvailableSlots();
    }

    private void resetForm() {
        selectedService = null;
        selectedTime = null;
        selectedPatient = null;
        view.clearForm();

        selectedSpecialization = ALL_SPECIALIZATIONS;
        view.setSelectedSpecialization(selectedSpecialization);
        applyDoctorFilters();

        suppressSelectionEvents = true;
        selectedDate = LocalDate.now();
        view.setSelectedDate(selectedDate);

        if (role == RoleMode.DOCTOR && user != null) {
            selectedDoctor = resolveDoctorForUser();
            if (selectedDoctor != null && doctors.contains(selectedDoctor)) {
                view.setSelectedDoctor(selectedDoctor);
            } else {
                view.setSelectedDoctor(null);
            }
        } else {
            selectedDoctor = null;
            view.setSelectedDoctor(null);
        }
        suppressSelectionEvents = false;

        view.setSelectedService(null);
        view.setSelectedPatient(null);
        loadAvailableSlots();
    }

    private void clearEditMode() {
        editingAppointment = null;
        ClientSession.getInstance().clearEditMode();
        ClientSession.getInstance().clearSelectedAppointment();
        view.setEditMode(false, "");
    }

    private void applyEditFromSession() {
        if (editingAppointment != null) {
            return;
        }
        Long editId = ClientSession.getInstance().getAppointmentToEdit();
        if (editId == null) {
            return;
        }
        AppointmentDTO selected = ClientSession.getInstance().getSelectedAppointment();
        if (selected != null && selected.getAppointmentId() == editId) {
            onEditAppointment(selected);
            return;
        }
        view.setError("Nu pot incarca programarea pentru editare. Reincearca din lista programarilor.");
        ClientSession.getInstance().clearEditMode();
    }

    private void loadAvailableSlots() {
        if (selectedDoctor == null || selectedDate == null) {
            view.setAvailableTimes(List.of());
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_AVAILABLE_SLOTS)
                .put("doctorId", selectedDoctor.getDoctorId())
                .put("date", selectedDate);

        view.setBusy(true);
        view.setInfo("Se incarca orele disponibile...");
        sendRequest(cmd, response -> {
            view.setBusy(false);
            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                return;
            }
            if (response.getData() == null || ((List)response.getData()).isEmpty()) {
                view.setError("Nicio programare disponibilă pentru criteriile selectate.");
            }
            List<LocalTime> times = castList(response.getData());
            if (editingAppointment != null && selectedTime != null && !times.contains(selectedTime)) {
                List<LocalTime> merged = new ArrayList<>(times);
                merged.add(selectedTime);
                merged.sort(LocalTime::compareTo);
                times = merged;
            }
            view.setAvailableTimes(times);
            view.setInfo("");
        });
    }

    private void applyDoctorFilters() {
        String selectedSpec = selectedSpecialization == null ? "" : selectedSpecialization.getName();
        boolean hasSpecFilter = selectedSpec != null && !selectedSpec.isBlank()
                && !ALL_SPECIALIZATIONS.getName().equalsIgnoreCase(selectedSpec);

        List<DoctorDTO> filtered = hasSpecFilter
                ? doctors.stream()
                .filter(doctor -> {
                    String spec = doctor.getSpecializationName() == null ? "" : doctor.getSpecializationName();
                    return spec.equalsIgnoreCase(selectedSpec);
                })
                .toList()
                : new ArrayList<>(doctors);

        DoctorDTO previousDoctor = selectedDoctor;
        boolean previousSuppress = suppressSelectionEvents;
        suppressSelectionEvents = true;
        view.setDoctors(filtered);

        if (previousDoctor != null) {
            DoctorDTO match = findDoctorInList(filtered, previousDoctor);
            if (match != null) {
                selectedDoctor = match;
                view.setSelectedDoctor(match);
            } else if (role == RoleMode.DOCTOR) {
                selectedDoctor = previousDoctor;
                view.setSelectedDoctor(null);
            } else {
                selectedDoctor = null;
                selectedTime = null;
                view.setSelectedDoctor(null);
                view.setSelectedTime(null);
                view.setAvailableTimes(List.of());
            }
        } else {
            selectedDoctor = null;
            selectedTime = null;
            view.setSelectedDoctor(null);
            view.setSelectedTime(null);
            view.setAvailableTimes(List.of());
        }
        suppressSelectionEvents = previousSuppress;
    }

    private SpecializationDTO findSpecializationByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return specializations.stream()
                .filter(spec -> name.equalsIgnoreCase(spec.getName()))
                .findFirst()
                .orElse(null);
    }

    private DoctorDTO findDoctor(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        return doctors.stream()
                .filter(doctor -> Objects.equals(doctor.getDoctorId(), doctorId))
                .findFirst()
                .orElse(null);
    }

    private DoctorDTO findDoctorByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String target = normalizeName(name);
        return doctors.stream()
                .filter(doctor -> target.equals(normalizeName(doctor.getFullName())))
                .findFirst()
                .orElse(null);
    }

    private DoctorDTO findDoctorInList(List<DoctorDTO> list, DoctorDTO target) {
        if (target == null || list == null || list.isEmpty()) {
            return null;
        }
        long targetId = target.getDoctorId();
        for (DoctorDTO doctor : list) {
            if (doctor.getDoctorId() == targetId) {
                return doctor;
            }
        }
        String targetName = normalizeName(target.getFullName());
        if (targetName.isBlank()) {
            return null;
        }
        for (DoctorDTO doctor : list) {
            if (targetName.equals(normalizeName(doctor.getFullName()))) {
                return doctor;
            }
        }
        return null;
    }

    private DoctorDTO resolveDoctorForUser() {
        if (user == null) {
            return null;
        }
        DoctorDTO byId = findDoctor(user.getDoctorId());
        if (byId != null) {
            return byId;
        }
        DoctorDTO byName = findDoctorByName(user.getFullName());
        if (byName != null) {
            return byName;
        }
        if (user.getDoctorId() != null) {
            return new DoctorDTO(user.getDoctorId(), user.getFirstName(), user.getLastName(), null);
        }
        return null;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private MedicalServiceDTO findService(String name) {
        if (name == null) {
            return null;
        }
        return services.stream()
                .filter(service -> name.equalsIgnoreCase(service.getName()))
                .findFirst()
                .orElse(null);
    }

    private String buildEditHint(AppointmentDTO appt) {
        String patient = appt.getPatientName() == null ? "-" : appt.getPatientName();
        String doctor = appt.getDoctorName() == null ? "-" : appt.getDoctorName();
        return "Editare programare #" + appt.getAppointmentId() + " | " + patient + " -> " + doctor;
    }

    private void sendRequest(CommandDTO cmd, java.util.function.Consumer<Response> handler) {
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);
        ClientSession.getInstance().getClient().sendRequest(req, response ->
                Platform.runLater(() -> handler.accept(response)));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castList(Object data) {
        if (data instanceof List<?>) {
            return (List<T>) data;
        }
        return List.of();
    }
}
