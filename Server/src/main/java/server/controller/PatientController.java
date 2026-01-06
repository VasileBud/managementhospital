package server.controller;

import shared.common.Response;
import shared.dto.CommandDTO;
import shared.dto.PatientDetailsDTO;
import server.repository.PatientRepository;

public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController() {
        this.patientRepository = new PatientRepository();
    }

    public Response getPatientDetails(CommandDTO command) {
        Long patientId = command.getLong("patientId");
        if (patientId == null) {
            return Response.error("VALIDATION_ERROR", "Patient ID is required");
        }

        try {
            PatientDetailsDTO details = patientRepository.findDetailsByPatientId(patientId);
            if (details == null) {
                return Response.error("NOT_FOUND", "Patient not found");
            }
            return Response.ok(details);
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }
}
