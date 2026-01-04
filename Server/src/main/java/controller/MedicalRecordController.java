package controller;

import common.Response;
import dto.CommandDTO;
import dto.MedicalRecordEntryDTO;
import repository.MedicalRecordRepository; // Va trebui creat
import repository.PatientRepository;

import java.util.List;

public class MedicalRecordController {

    private final MedicalRecordRepository medicalRepo;
    private final PatientRepository patientRepo;

    public MedicalRecordController() {
        this.medicalRepo = new MedicalRecordRepository();
        this.patientRepo = new PatientRepository();
    }

    public Response getMyMedicalRecord(CommandDTO command) {
        Long requesterUserId = command.getRequesterUserId();
        try {
            // Găsim ID-ul pacientului pe baza user-ului logat
            Long patientId = patientRepo.findPatientIdByUserId(requesterUserId);
            if (patientId == null) {
                return Response.error("NOT_FOUND", "Patient profile not found");
            }

            List<MedicalRecordEntryDTO> entries = medicalRepo.findByPatientId(patientId);
            return Response.ok(entries);

        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response getPatientMedicalRecord(CommandDTO command) {
        // Medicul cere istoricul unui pacient specific
        Long patientId = command.getLong("patientId");
        try {
            List<MedicalRecordEntryDTO> entries = medicalRepo.findByPatientId(patientId);
            return Response.ok(entries);
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response addMedicalRecordEntry(CommandDTO command) {
        Long patientId = command.getLong("patientId");
        Long appointmentId = command.getLong("appointmentId");
        String diagnosis = command.getString("diagnosis");
        String treatment = command.getString("treatment");
        String notes = command.getString("notes");

        try {
            medicalRepo.addEntry(patientId, appointmentId, diagnosis, treatment, notes);
            return Response.okMessage("Medical record updated");
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }
}