package presenter.patient;

import app.ClientSession;
import view.patient.PatientMedicalRecordView;
import javafx.application.Platform;
import model.common.Request;
import model.common.RequestType;
import model.common.Response;
import model.dto.CommandDTO;
import model.dto.MedicalRecordEntryDTO;
import model.dto.UserDTO;

import java.util.Collections;
import java.util.List;

public class PatientMedicalRecordPresenter {

    private final PatientMedicalRecordView view;

    public PatientMedicalRecordPresenter(PatientMedicalRecordView view) {
        this.view = view;
    }

    public void loadMedicalRecord() {
        UserDTO user = ClientSession.getInstance().getLoggedUser();
        if (user == null) {
            view.setError("Utilizator invalid.");
            return;
        }

        if (!ClientSession.getInstance().ensureConnected()) {
            view.setError("Nu exista conexiune la server!");
            return;
        }

        CommandDTO cmd = new CommandDTO(CommandDTO.Action.GET_MY_MEDICAL_RECORD, user.getUserId());
        Request req = new Request(cmd);
        req.setType(RequestType.COMMAND);

        view.setInfo("Se incarca fisa medicala...");
        ClientSession.getInstance().getClient().sendRequest(req, this::handleResponse);
    }

    private void handleResponse(Response response) {
        Platform.runLater(() -> {
            if (response.getStatus() != Response.Status.OK) {
                view.setError("Eroare: " + response.getMessage());
                view.renderEntries(Collections.emptyList());
                return;
            }

            @SuppressWarnings("unchecked")
            List<MedicalRecordEntryDTO> entries = response.getData() instanceof List<?>
                    ? (List<MedicalRecordEntryDTO>) response.getData()
                    : Collections.emptyList();

            view.renderEntries(entries);
            view.setInfo("");
        });
    }
}
