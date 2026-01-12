package presenter.auth;

import app.AppScene;
import app.SceneNavigator;
import app.ClientSession;
import view.auth.RegisterView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;
import model.dto.CommandDTO;

public class RegisterPresenter {

    private final RegisterView view;

    public RegisterPresenter(RegisterView view) {
        this.view = view;
    }

    public void onRegister(
            String firstNameRaw,
            String lastNameRaw,
            String emailRaw,
            String passwordRaw,
            String confirmPasswordRaw
    ) {
        String firstName = safe(firstNameRaw);
        String lastName = safe(lastNameRaw);
        String email = safe(emailRaw);
        String password = passwordRaw == null ? "" : passwordRaw;
        String confirm = confirmPasswordRaw == null ? "" : confirmPasswordRaw;

        if (!validateRegister(firstName, lastName, email, password, confirm)) {
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu există conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.REGISTER)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("email", email)
                .put("password", password);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se creează contul...");

        ClientSession.getInstance().getClient().sendRequest(req, this::handleResponse);

    }

    private void handleResponse(Response response) {
        Platform.runLater(() -> {
            view.setBusy(false);

            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                return;
            }

            view.setSuccess("Cont creat cu succes! Te poți autentifica.");

            SceneNavigator.navigateTo(AppScene.LOGIN);
        });
    }

    public void onBackToLogin() {
        SceneNavigator.navigateTo(AppScene.LOGIN);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private boolean validateRegister(String firstName, String lastName, String email,
                                     String password, String confirm) {
        if (firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            view.setError("Completeaza toate campurile!");
            return false;
        }
        if (firstName.length() > 60 || lastName.length() > 60) {
            view.setError("Numele este prea lung.");
            return false;
        }
        if (email.length() > 255 || !email.contains("@") || !email.contains(".")) {
            view.setError("Email invalid!");
            return false;
        }
        if (password.length() < 6 || password.length() > 128) {
            view.setError("Parola trebuie sa aiba intre 6 si 128 caractere.");
            return false;
        }
        if (!password.equals(confirm)) {
            view.setError("Parolele nu coincid!");
            return false;
        }
        return true;
    }
}




