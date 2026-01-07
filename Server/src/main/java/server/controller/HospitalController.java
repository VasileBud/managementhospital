package server.controller;

import shared.common.Response;
import shared.dto.CommandDTO;

public class HospitalController {

    private final AuthController authController;
    private final DoctorController doctorController;
    private final AppointmentController appointmentController;
    private final MedicalRecordController medicalRecordController;
    private final FeedbackController feedbackController;
    private final AdminController adminController;
    private final StatsController statsController;
    private final PublicController publicController;
    private final PatientDashboardController patientDashboardController;
    private final PatientController patientController;

    public HospitalController() {
        this.authController = new AuthController();
        this.doctorController = new DoctorController();
        this.appointmentController = new AppointmentController();
        this.medicalRecordController = new MedicalRecordController();
        this.feedbackController = new FeedbackController();
        this.adminController = new AdminController();
        this.statsController = new StatsController();
        this.publicController = new PublicController();
        this.patientDashboardController = new PatientDashboardController();
        this.patientController = new PatientController();
    }

    public Response handle(CommandDTO command) {

        if (command == null || command.getAction() == null) {
            return Response.error("INVALID_COMMAND", "Command or action is null");
        }

        try {
            return switch (command.getAction()) {

                case LOGIN ->
                        authController.login(command);

                case REGISTER ->
                        authController.registerPatient(command);

                case LOGOUT ->
                        authController.logout(command);

                case GET_DOCTORS ->
                        doctorController.getDoctors();

                case GET_DOCTOR_SCHEDULE ->
                        doctorController.getDoctorSchedule(command);

                case GET_AVAILABLE_SLOTS ->
                        appointmentController.getAvailableSlots(command);

                case GET_SPECIALIZATIONS ->
                        publicController.getSpecializations();

                case GET_MEDICAL_SERVICES ->
                        publicController.getMedicalServices();

                case GET_PATIENT_DASHBOARD ->
                        patientDashboardController.getDashboard(command);

                case BOOK_APPOINTMENT ->
                        appointmentController.bookAppointment(command);

                case UPDATE_APPOINTMENT ->
                        appointmentController.updateAppointment(command);

                case CANCEL_APPOINTMENT ->
                        appointmentController.cancelAppointment(command);

                case APPROVE_APPOINTMENT ->
                        appointmentController.approveAppointment(command);

                case MARK_APPOINTMENT_DONE ->
                        appointmentController.markAppointmentDone(command);

                case GET_MY_APPOINTMENTS ->
                        appointmentController.getMyAppointments(command);

                case GET_ALL_APPOINTMENTS ->
                        appointmentController.getAllAppointments(command);

                case GET_DOCTOR_APPOINTMENTS ->
                        appointmentController.getDoctorAppointments(command);

                case GET_PATIENT_DETAILS ->
                        patientController.getPatientDetails(command);

                case ADD_MEDICAL_RECORD_ENTRY ->
                        medicalRecordController.addMedicalRecordEntry(command);

                case GET_MY_MEDICAL_RECORD ->
                        medicalRecordController.getMyMedicalRecord(command);

                case GET_PATIENT_MEDICAL_RECORD ->
                        medicalRecordController.getPatientMedicalRecord(command);

                case SEND_FEEDBACK ->
                        feedbackController.sendFeedback(command);

                case ADMIN_CREATE_USER ->
                        adminController.createUser(command);

                case ADMIN_UPDATE_USER ->
                        adminController.updateUser(command);

                case ADMIN_DELETE_USER ->
                        adminController.deleteUser(command);

                case ADMIN_LIST_USERS ->
                        adminController.listUsers();

                case ADMIN_GET_STATS ->
                        adminController.getAdminStats();

                case GET_STATS ->
                        statsController.getStats(command);

                default ->
                        Response.error("UNKNOWN_ACTION", "Unknown action: " + command.getAction());
            };

        } catch (Exception e) {
            return Response.error("SERVER_ERROR", e.getMessage());
        }
    }
}
