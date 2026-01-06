package com.hospital_management.client.presenter.manager;

import com.hospital_management.client.network.ClientSession;
import com.hospital_management.client.view.manager.ManagerDashboardView;
import javafx.application.Platform;
import shared.common.Request;
import shared.common.RequestType;
import shared.common.Response;
import shared.dto.CommandDTO;
import shared.dto.StatsDTO;
import shared.dto.UserDTO;

public class ManagerStatsPresenter {

    private final ManagerDashboardView view;

    public ManagerStatsPresenter(ManagerDashboardView view) {
        this.view = view;
    }

    public void loadStats() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator neautentificat.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_STATS, user.getUserId());
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se incarca statisticile...");
        ClientSession.getInstance().getClient().sendRequest(req, response -> {
            Platform.runLater(() -> handleResponse(response));
        });
    }

    private void handleResponse(Response response) {
        if (response.getStatus() != Response.Status.OK) {
            view.setError("Eroare: " + response.getMessage());
            return;
        }

        StatsDTO stats = response.getPayload() instanceof StatsDTO
                ? (StatsDTO) response.getPayload()
                : null;

        view.renderStats(stats);
        view.setInfo("");
    }
}
