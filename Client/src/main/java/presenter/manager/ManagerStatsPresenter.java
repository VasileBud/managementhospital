package presenter.manager;

import app.ClientSession;
import view.manager.ManagerDashboardView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;
import model.dto.CommandDTO;
import model.dto.StatsDTO;
import model.dto.UserDTO;

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
        try {
            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare Stats: " + response.getMessage());
                return;
            }

            StatsDTO stats = response.getData() instanceof StatsDTO
                    ? (StatsDTO) response.getData()
                    : null;

            if (stats != null) {
                view.renderStats(stats);
            }

        } catch (Exception e) {
            e.printStackTrace();
            view.setError("Eroare afișare statistici.");
        } finally {
            view.setInfo("");
        }
    }
}
