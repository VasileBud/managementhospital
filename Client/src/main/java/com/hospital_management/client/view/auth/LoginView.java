package com.hospital_management.client.view.auth;

import com.hospital_management.client.presenter.auth.LoginPresenter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginView {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private LoginPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new LoginPresenter(this);
        setError("");
    }

    @FXML
    public void onLoginClick() {
        presenter.onLogin(
                emailField.getText(),
                passwordField.getText()
        );
    }

    @FXML
    public void onGoToRegisterClick() {
        presenter.onGoToRegister();
    }

    // ===== metode pentru UI pe care Presenter le poate apela =====
    public void setBusy(boolean busy) {
        if (loginButton != null) loginButton.setDisable(busy);
        emailField.setDisable(busy);
        passwordField.setDisable(busy);
    }

    public void setError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    public void setInfo(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #666666;");
    }

    public void setSuccess(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: green;");
    }
}
