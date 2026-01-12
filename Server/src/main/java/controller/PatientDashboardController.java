package controller;

import model.Appointment;
import model.Patient;
import model.repository.AppointmentRepository;
import model.repository.PatientRepository;
import model.common.Response;
import model.dto.AppointmentDTO;
import model.dto.CommandDTO;
import model.dto.PatientDashboardDTO;
import model.dto.PatientProfileDTO;

import java.util.List;

public class PatientDashboardController {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientDashboardController() {
        this.patientRepository = new PatientRepository();
        this.appointmentRepository = new AppointmentRepository();
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

            Patient patient = patientRepository.findDetailsByPatientId(patientId);
            if (patient == null) {
                return Response.error("NOT_FOUND", "Patient profile not found");
            }
            PatientProfileDTO profile = toProfileDto(patient);
            Appointment next = appointmentRepository.findNextByPatientId(patientId);
            List<Appointment> history = appointmentRepository.findHistoryByPatientId(patientId, 10);

            return Response.ok(new PatientDashboardDTO(
                    profile,
                    toDto(next),
                    history.stream().map(this::toDto).toList()
            ));
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    private AppointmentDTO toDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new AppointmentDTO(
                appointment.getAppointmentId(),
                appointment.getPatientId(),
                appointment.getPatientName(),
                appointment.getDoctorId(),
                appointment.getDoctorName(),
                appointment.getServiceName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus() == null ? null : appointment.getStatus().name()
        );
    }

    private PatientProfileDTO toProfileDto(Patient patient) {
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
