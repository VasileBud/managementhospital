package com.hospital_management.client.presenter.auth;

import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.auth.LoginView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.CommandDTO;
import shared.dto.LoginResponseDTO;
import shared.dto.UserDTO;

public class LoginPresenter {

    private final LoginView view;

    public LoginPresenter(LoginView view) {
        this.view = view;
    }

    public void onLogin(String emailRaw, String passwordRaw) {
        String email = safe(emailRaw);
        String password = passwordRaw == null ? "" : passwordRaw;

        if (!validateLogin(email, password)) {
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu există conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.LOGIN)
                .put("email", email)
                .put("password", password);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se autentifică...");

        ClientSession.getInstance().getClient().sendRequest(req, this::handleResponse);
    }

    private void handleResponse(Response response) {
        Platform.runLater(() -> {
            view.setBusy(false);

            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                return;
            }

            LoginResponseDTO data = (LoginResponseDTO) response.getData();

            UserDTO user = new UserDTO(
                    data.getUserId(),
                    data.getFirstName(),
                    data.getLastName(),
                    data.getEmail(),
                    data.getRole(),
                    data.getPatientId(),
                    data.getDoctorId()
            );
            System.out.println(user);
            ClientSession.getInstance().clearSelectedAppointment();
            ClientSession.getInstance().clearEditMode();
            ClientSession.getInstance().setLoggedUser(user);
            SceneNavigator.clearCache();

            // Navigare după rol
            switch (data.getRole()) {
                case "ADMIN" -> SceneNavigator.navigateToFresh(AppScene.ADMIN_DASHBOARD);
                case "DOCTOR" -> SceneNavigator.navigateToFresh(AppScene.DOCTOR_DASHBOARD);
                case "MANAGER" -> SceneNavigator.navigateToFresh(AppScene.MANAGER_DASHBOARD);
                case "PATIENT" -> SceneNavigator.navigateToFresh(AppScene.PATIENT_DASHBOARD);
                default -> SceneNavigator.navigateTo(AppScene.PUBLIC);
            }
        });
    }

    public void onGoToRegister() {
        SceneNavigator.navigateTo(AppScene.REGISTER);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private boolean validateLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.setError("Introduceti email si parola!");
            return false;
        }
        if (email.length() > 255 || password.length() > 128) {
            view.setError("Datele introduse sunt prea lungi.");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            view.setError("Email invalid!");
            return false;
        }
        return true;
    }
}


