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

            PatientDashboardDTO data = (PatientDashboardDTO) response.getData();
            view.renderDashboard(data);
        });
    }
}
