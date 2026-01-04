package com.hospital_management.client.controller.auth;

import com.hospital_management.client.network.ClientSession;
import common.Request;
import common.RequestType;
import common.Response;
import dto.CommandDTO;
import dto.LoginResponseDTO;
import dto.UserDTO;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // Trebuie să existe în FXML cu fx:id="errorLabel"

    @FXML
    public void onLoginClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // 1. Validare simplă
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Introduceți email și parola!");
            return;
        }

        // 2. Verificăm conexiunea
        if (!ClientSession.getInstance().isConnected()) {
            errorLabel.setText("Nu există conexiune la server!");
            return;
        }

        // 3. Creăm comanda de Login
        CommandDTO loginCmd = new CommandDTO(CommandDTO.Action.LOGIN)
                .put("email", email)
                .put("password", password);

        Request request = new Request(loginCmd);
        request.setType(RequestType.COMMAND);

        // 4. Setăm ce facem când vine răspunsul (Callback)
        ClientSession.getInstance().getClient().setOnResponseReceived(this::handleServerResponse);

        // 5. Trimitem cererea
        ClientSession.getInstance().getClient().sendRequest(request);
        errorLabel.setText("Se autentifică...");
    }

    private void handleServerResponse(Response response) {
        // IMPORTANT: Modificările de UI se fac doar pe thread-ul JavaFX
        Platform.runLater(() -> {
            if (response.getStatus() == Response.Status.OK) {
                // Login Reușit!
                LoginResponseDTO data = (LoginResponseDTO) response.getData();

                // Salvăm user-ul în sesiune
                UserDTO user = new UserDTO(data.getUserId(), "Nume", "Prenume", "email", data.getRole(), data.getPatientId(), data.getDoctorId());
                ClientSession.getInstance().setLoggedUser(user);

                System.out.println("User logat: " + data.getFullName() + " | Rol: " + data.getRole());
                errorLabel.setText("Succes! Rol: " + data.getRole());
                errorLabel.setStyle("-fx-text-fill: green;");

                // TODO: Aici vei schimba scena către Dashboard (ex: SceneNavigator...)

            } else {
                // Login Eșuat
                errorLabel.setText("Eroare: " + response.getMessage());
                errorLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }
}