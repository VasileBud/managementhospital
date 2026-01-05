package server.controller;

import shared.common.Response;
import shared.dto.AppointmentDTO;
import shared.dto.CommandDTO;
import shared.dto.PatientDashboardDTO;
import shared.dto.PatientProfileDTO;
import server.repository.AppointmentRepository;
import server.repository.PatientProfileRepository;
import server.repository.PatientRepository;

import java.util.List;

public class PatientDashboardController {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository profileRepository;

    public PatientDashboardController() {
        this.patientRepository = new PatientRepository();
        this.appointmentRepository = new AppointmentRepository();
        this.profileRepository = new PatientProfileRepository();
    }

    public Response getDashboard(CommandDTO command) {
        Long requesterUserId = command.getRequesterUserId();
        if (requesterUserId == null) {
            return Response.error("VALIDATION_ERROR", "Requester user id is required");
        }

        try {
            Long patientId = patientRepository.findPatientIdByUserId(requesterUserId);
            if (patientId == null) {
                return Response.error("NOT_FOUND", "Patient profile not found");
            }

            PatientProfileDTO profile = profileRepository.getProfile(patientId);
            AppointmentDTO next = appointmentRepository.findNextByPatientId(patientId);
            List<AppointmentDTO> history = appointmentRepository.findHistoryByPatientId(patientId, 10);

            return Response.ok(new PatientDashboardDTO(profile, next, history));
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }
}
