package controller;

import model.Patient;
import model.repository.PatientRepository;
import model.common.Response;
import model.dto.CommandDTO;
import model.dto.PatientProfileDTO;

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
            Patient patient = patientRepository.findDetailsByPatientId(patientId);
            if (patient == null) {
                return Response.error("NOT_FOUND", "Patient not found");
            }
            return Response.ok(toProfile(patient));
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    private PatientProfileDTO toProfile(Patient patient) {
        return new PatientProfileDTO(
                patient.getPatientId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getNationalId(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getBloodType(),
                patient.getWeightKg(),
                patient.getHeightCm(),
                patient.getAllergies(),
                patient.getConditions()
        );
    }
}
