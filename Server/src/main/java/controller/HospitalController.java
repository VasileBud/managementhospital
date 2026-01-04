package controller;

import common.Response;
import dto.CommandDTO;
import ocsf.server.ConnectionToClient;

public class HospitalController {

    private final AuthController authController;
    private final DoctorController doctorController;
    private final AppointmentController appointmentController;
    private final MedicalRecordController medicalRecordController;
    private final FeedbackController feedbackController;
    private final AdminController adminController;
    private final StatsController statsController;

    public HospitalController() {
        this.authController = new AuthController();
        this.doctorController = new DoctorController();
        this.appointmentController = new AppointmentController();
        this.medicalRecordController = new MedicalRecordController();
        this.feedbackController = new FeedbackController();
        this.adminController = new AdminController();
        this.statsController = new StatsController();
    }

    /**
     * Main dispatcher for ALL client commands.
     */
    public Response handle(CommandDTO command, ConnectionToClient client) {

        if (command == null || command.getAction() == null) {
            return Response.error("INVALID_COMMAND", "Command or action is null");
        }

        try {
            return switch (command.getAction()) {

                // =====================
                // AUTH
                // =====================
                case LOGIN ->
                        authController.login(command);

                case REGISTER_PATIENT ->
                        authController.registerPatient(command);

                case LOGOUT ->
                        authController.logout(command);

                // =====================
                // PUBLIC / PATIENT
                // =====================
                case GET_DOCTORS ->
                        doctorController.getDoctors();

                case GET_DOCTOR_SCHEDULE ->
                        doctorController.getDoctorSchedule(command);

                case GET_AVAILABLE_SLOTS ->
                        Response.error("NOT_IMPLEMENTED", "GET_AVAILABLE_SLOTS not implemented yet");

                // =====================
                // APPOINTMENTS
                // =====================
                case BOOK_APPOINTMENT ->
                        appointmentController.bookAppointment(command);

                case CANCEL_APPOINTMENT ->
                        appointmentController.cancelAppointment(command);

                case APPROVE_APPOINTMENT ->
                        appointmentController.approveAppointment(command);

                case MARK_APPOINTMENT_DONE ->
                        appointmentController.markAppointmentDone(command);

                case GET_MY_APPOINTMENTS,
                     GET_DOCTOR_APPOINTMENTS ->
                        Response.error("NOT_IMPLEMENTED", "GET_APPOINTMENTS not implemented yet");

                // =====================
                // MEDICAL RECORD
                // =====================
                case ADD_MEDICAL_RECORD_ENTRY ->
                        medicalRecordController.addMedicalRecordEntry(command);

                case GET_MY_MEDICAL_RECORD ->
                        medicalRecordController.getMyMedicalRecord(command);

                case GET_PATIENT_MEDICAL_RECORD ->
                        medicalRecordController.getPatientMedicalRecord(command);

                // =====================
                // FEEDBACK
                // =====================
                case SEND_FEEDBACK ->
                        feedbackController.sendFeedback(command);

                // =====================
                // ADMIN
                // =====================
                case ADMIN_CREATE_USER ->
                        adminController.createUser(command);

                case ADMIN_UPDATE_USER ->
                        adminController.updateUser(command);

                case ADMIN_DELETE_USER ->
                        adminController.deleteUser(command);

                case ADMIN_LIST_USERS ->
                        adminController.listUsers();

                // =====================
                // MANAGER / ADMIN
                // =====================
                case GET_STATS ->
                        statsController.getStats(command);

                // =====================
                // DEFAULT
                // =====================
                default ->
                        Response.error("UNKNOWN_ACTION", "Unknown action: " + command.getAction());
            };

        } catch (Exception e) {
            return Response.error("SERVER_ERROR", e.getMessage());
        }
    }
}
