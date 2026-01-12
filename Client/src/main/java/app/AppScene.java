package app;

public enum AppScene {
    PUBLIC("/fxml/patient/public.fxml"),
    LOGIN("/fxml/auth/login.fxml"),
    REGISTER("/fxml/auth/registre.fxml"),

    ADMIN_DASHBOARD("/fxml/admin/admin_dashboard.fxml"),
    ADMIN_PATIENT_HISTORY("fxml/admin/admin_patient_history.fxml"),
    DOCTOR_DASHBOARD("/fxml/doctor/doctor_dashboard.fxml"),
    DOCTOR_CONSULTATION("/fxml/doctor/doctor_consultation.fxml"),
    MANAGER_DASHBOARD("/fxml/manager/manager_dashboard.fxml"),
    MANAGER_PATIENT_HISTORY("/fxml/manager/manager_patient_history.fxml"),
    PATIENT_DASHBOARD("/fxml/patient/patient_dashboard.fxml"),
    APPOINTMENT_BOOKING("/fxml/patient/appointment_booking.fxml"),
    PATIENT_MEDICAL_RECORD("/fxml/patient/patient_medical_record.fxml");

    private final String fxmlPath;

    AppScene(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
