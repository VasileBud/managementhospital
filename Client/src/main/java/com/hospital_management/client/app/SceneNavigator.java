package com.hospital_management.client.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class SceneNavigator {

    private static Stage primaryStage;

    private static final Map<AppScene, Parent> rootsCache = new HashMap<>();

    private SceneNavigator() {}

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void clearCache() {
        rootsCache.clear();
    }

    public static void navigateTo(AppScene target) {
        ensureInitialized();
        Parent root = loadRoot(target);

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        applySceneStyles(scene, target);
        primaryStage.show();
    }

    /**
     * Dacă vrei să NU cache-uiești (de ex. ca să se reinitializeze controllerul),
     * apelezi metoda asta.
     */
    public static void navigateToFresh(AppScene target) {
        ensureInitialized();
        Parent root = loadRootFresh(target);
        Scene scene = primaryStage.getScene();

        if (scene == null) {
            scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        applySceneStyles(scene, target);
        primaryStage.show();
    }

    private static Parent loadRoot(AppScene target) {
        Parent cached = rootsCache.get(target);
        if (cached != null) {
            return cached;
        }
        Parent root = loadRootFresh(target);
        rootsCache.put(target, root);
        return root;
    }

    private static Parent loadRootFresh(AppScene target) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(target.getFxmlPath()));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Nu pot încărca FXML: " + target.getFxmlPath(), e);
        }
    }

    private static void ensureInitialized() {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneNavigator.init(stage) nu a fost apelat.");
        }
    }

    private static void applySceneStyles(Scene scene, AppScene target) {
        String loginCss = null;
        String publicCss = null;
        String appointmentCss = null;
        String medicalRecordCss = null;
        String doctorCss = null;
        String doctorConsultationCss = null;
        String managerCss = null;
        String adminCss = null;

        if (SceneNavigator.class.getResource("/styles/login.css") != null) {
            loginCss = SceneNavigator.class.getResource("/styles/login.css").toExternalForm();
        }
        if (SceneNavigator.class.getResource("/styles/public.css") != null) {
            publicCss = SceneNavigator.class.getResource("/styles/public.css").toExternalForm();
        }
        String patientCss = null;
        if (SceneNavigator.class.getResource("/styles/patient_dashboard.css") != null) {
            patientCss = SceneNavigator.class.getResource("/styles/patient_dashboard.css").toExternalForm();
        }
        if (SceneNavigator.class.getResource("/styles/appointment_booking.css") != null) {
            appointmentCss = SceneNavigator.class.getResource("/styles/appointment_booking.css").toExternalForm();
        }
        if (SceneNavigator.class.getResource("/styles/patient_medical_record.css") != null) {
            medicalRecordCss = SceneNavigator.class.getResource("/styles/patient_medical_record.css").toExternalForm();
        }
        if (SceneNavigator.class.getResource("/styles/doctor_dashboard.css") != null) {
            doctorCss = SceneNavigator.class.getResource("/styles/doctor_dashboard.css").toExternalForm();
        }
        if (SceneNavigator.class.getResource("/styles/doctor_consultation.css") != null) {
            doctorConsultationCss = SceneNavigator.class.getResource("/styles/doctor_consultation.css").toExternalForm();
        }
        if (SceneNavigator.class.getResource("/styles/manager_dashboard.css") != null) {
            managerCss = SceneNavigator.class.getResource("/styles/manager_dashboard.css").toExternalForm();
        }
        if (SceneNavigator.class.getResource("/styles/admin_dashboard.css") != null) {
            adminCss = SceneNavigator.class.getResource("/styles/admin_dashboard.css").toExternalForm();
        }

        scene.getStylesheets().removeIf(s ->
                s.endsWith("/styles/login.css")
                        || s.endsWith("/styles/public.css")
                        || s.endsWith("/styles/patient_dashboard.css")
                        || s.endsWith("/styles/appointment_booking.css")
                        || s.endsWith("/styles/patient_medical_record.css")
                        || s.endsWith("/styles/doctor_dashboard.css")
                        || s.endsWith("/styles/doctor_consultation.css")
                        || s.endsWith("/styles/manager_dashboard.css")
                        || s.endsWith("/styles/admin_dashboard.css"));

        if ((target == AppScene.LOGIN || target == AppScene.REGISTER) && loginCss != null) {
            scene.getStylesheets().add(loginCss);
        }
        if (target == AppScene.PUBLIC && publicCss != null) {
            scene.getStylesheets().add(publicCss);
        }
        if (target == AppScene.PATIENT_DASHBOARD && patientCss != null) {
            scene.getStylesheets().add(patientCss);
        }
        if (target == AppScene.APPOINTMENT_BOOKING && appointmentCss != null) {
            scene.getStylesheets().add(appointmentCss);
        }
        if (target == AppScene.PATIENT_MEDICAL_RECORD && medicalRecordCss != null) {
            scene.getStylesheets().add(medicalRecordCss);
        }
        if (target == AppScene.DOCTOR_DASHBOARD && doctorCss != null) {
            scene.getStylesheets().add(doctorCss);
        }
        if (target == AppScene.DOCTOR_CONSULTATION && doctorConsultationCss != null) {
            scene.getStylesheets().add(doctorConsultationCss);
        }
        if (target == AppScene.MANAGER_DASHBOARD && managerCss != null) {
            scene.getStylesheets().add(managerCss);
        }
        if (target == AppScene.ADMIN_DASHBOARD && adminCss != null) {
            scene.getStylesheets().add(adminCss);
        }
    }

}
