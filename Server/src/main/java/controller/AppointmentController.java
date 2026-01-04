package controller;

import common.Response;
import dto.CommandDTO;
import repository.AppointmentRepository;
import model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentController {

    private final AppointmentRepository appointmentRepository;

    public AppointmentController() {
        this.appointmentRepository = new AppointmentRepository();
    }

    public Response bookAppointment(CommandDTO command) {
        Long patientId = command.getLong("patientId");
        Long doctorId = command.getLong("doctorId");
        Long serviceId = command.getLong("serviceId"); // Poate fi null dacă nu e selectat
        LocalDate date = command.getDate("date"); // Asigură-te că CommandDTO știe să extragă LocalDate
        LocalTime time = (LocalTime) command.getData().get("time"); // Sau un helper getTime()

        if (patientId == null || doctorId == null || date == null || time == null) {
            return Response.error("VALIDATION_ERROR", "Missing appointment details");
        }

        try {
            // Verificăm disponibilitatea (opțional, dar recomandat)
            boolean isFree = appointmentRepository.isSlotAvailable(doctorId, date, time);
            if (!isFree) {
                return Response.error("SLOT_TAKEN", "The selected slot is no longer available.");
            }

            // Inserare în DB
            long appointmentId = appointmentRepository.createAppointment(
                    patientId, doctorId, serviceId != null ? serviceId : 0, date, time, AppointmentStatus.PENDING
            );

            return Response.okMessage("Appointment requested successfully. ID: " + appointmentId);

        } catch (Exception e) {
            return Response.error("DB_ERROR", "Booking failed: " + e.getMessage());
        }
    }

    public Response cancelAppointment(CommandDTO command) {
        Long appointmentId = command.getLong("appointmentId");

        try {
            boolean updated = appointmentRepository.updateStatus(appointmentId, AppointmentStatus.CANCELED);
            if (updated) return Response.okMessage("Appointment canceled");
            else return Response.error("NOT_FOUND", "Appointment not found");
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response approveAppointment(CommandDTO command) {
        Long appointmentId = command.getLong("appointmentId");
        try {
            boolean updated = appointmentRepository.updateStatus(appointmentId, AppointmentStatus.CONFIRMED);
            if (updated) return Response.okMessage("Appointment confirmed");
            else return Response.error("NOT_FOUND", "Appointment not found");
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response markAppointmentDone(CommandDTO command) {
        Long appointmentId = command.getLong("appointmentId");
        try {
            boolean updated = appointmentRepository.updateStatus(appointmentId, AppointmentStatus.DONE);
            if (updated) return Response.okMessage("Appointment marked as done");
            else return Response.error("NOT_FOUND", "Appointment not found");
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }
}