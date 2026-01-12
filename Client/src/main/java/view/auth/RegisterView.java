package view.auth;

import presenter.auth.RegisterPresenter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterView {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    private RegisterPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new RegisterPresenter(this);
        setError("");
    }

    @FXML
    public void onRegisterClick() {
        presenter.onRegister(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText()
        );
    }

    @FXML
    public void onBackToLoginClick() {
        presenter.onBackToLogin();
    }


    public void setBusy(boolean busy) {
        if (registerButton != null) registerButton.setDisable(busy);
        firstNameField.setDisable(busy);
        lastNameField.setDisable(busy);
        emailField.setDisable(busy);
        passwordField.setDisable(busy);
        confirmPasswordField.setDisable(busy);
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
