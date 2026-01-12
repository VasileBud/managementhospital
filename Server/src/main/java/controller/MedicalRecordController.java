package controller;

import model.MedicalRecordEntry;
import model.repository.MedicalRecordRepository;
import model.repository.PatientRepository;
import model.common.Response;
import model.dto.CommandDTO;
import model.dto.MedicalRecordEntryDTO;

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
            Long patientId = patientRepo.findPatientIdByUserId(requesterUserId);
            if (patientId == null) {
                return Response.error("NOT_FOUND", "Patient profile not found");
            }

            List<MedicalRecordEntry> entries = medicalRepo.findByPatientId(patientId);
            return Response.ok(entries.stream().map(this::toDto).toList());

        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response getPatientMedicalRecord(CommandDTO command) {
        Long patientId = command.getLong("patientId");
        try {
            List<MedicalRecordEntry> entries = medicalRepo.findByPatientId(patientId);
            return Response.ok(entries.stream().map(this::toDto).toList());
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

    private MedicalRecordEntryDTO toDto(MedicalRecordEntry entry) {
        return new MedicalRecordEntryDTO(
                entry.getEntryId(),
                entry.getPatientId(),
                entry.getAppointmentId(),
                entry.getDoctorName(),
                entry.getDiagnosis(),
                entry.getTreatment(),
                entry.getNotes(),
                entry.getEntryDate()
        );
    }
}
