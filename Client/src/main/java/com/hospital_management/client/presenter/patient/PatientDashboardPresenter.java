package com.hospital_management.client.presenter.patient;

import com.hospital_management.client.app.SceneNavigator;
import com.hospital_management.client.app.AppScene;
import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.patient.PatientDashboardView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.CommandDTO;
import shared.dto.PatientDashboardDTO;
import shared.dto.UserDTO;

public class PatientDashboardPresenter {

    private final PatientDashboardView view;

    public PatientDashboardPresenter(PatientDashboardView view) {
        this.view = view;
    }

    public void loadDashboard() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator neautentificat.");
            SceneNavigator.navigateTo(AppScene.LOGIN);
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_PATIENT_DASHBOARD, user.getUserId());
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se incarca datele...");

        ClientSession.getInstance().getClient().setOnResponseReceived(this::handleResponse);
        ClientSession.getInstance().getClient().sendRequest(req);
    }

    private void handleResponse(Response response) {
        Platform.runLater(() -> {
            view.setBusy(false);

            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                return;
            }

            PatientDashboardDTO data = (PatientDashboardDTO) response.getPayload();
            view.renderDashboard(data);
        });
    }

    public void onCancelAppointment(long appointmentId) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu există conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.CANCEL_APPOINTMENT)
                .put("appointmentId", appointmentId);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setBusy(true);
        view.setInfo("Se anulează programarea...");

        ClientSession.getInstance().getClient().setOnResponseReceived(response -> {
            Platform.runLater(() -> {
                view.setBusy(false);
                if (response.getStatus() != Response.Status.OK) {
                    view.setError("Eroare anulare: " + response.getMessage());
                } else {
                    view.setInfo("Programarea a fost anulată cu succes.");
                    // reload la dashboard
                    loadDashboard();
                }
            });
        });

        ClientSession.getInstance().getClient().sendRequest(req);
    }

    public void onSendFeedback(long appointmentId, int rating, String comment) {
        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Lipsă conexiune server.");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.SEND_FEEDBACK)
                .put("appointmentId", appointmentId)
                .put("rating", rating)
                .put("comment", comment);

        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se trimite feedback-ul...");

        ClientSession.getInstance().getClient().setOnResponseReceived(response -> {
            Platform.runLater(() -> {
                if (response.getStatus() == Response.Status.OK) {
                    view.setInfo("Mulțumim pentru feedback!");
                    // Opțional: Reîncarci dashboard-ul ca să ascunzi butonul de feedback pentru asta
                } else {
                    view.setError("Eroare: " + response.getMessage());
                }
            });
        });

        ClientSession.getInstance().getClient().sendRequest(req);
    }
}
