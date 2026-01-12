package controller;

import model.common.Response;
import model.dto.AppointmentDTO;
import model.dto.CommandDTO;
import model.Appointment;
import model.DoctorSchedule;
import model.repository.AppointmentRepository;
import model.repository.DoctorRepository;
import model.repository.PatientRepository;
import model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private static final int SLOT_MINUTES = 30;

    public AppointmentController() {
        this.appointmentRepository = new AppointmentRepository();
        this.doctorRepository = new DoctorRepository();
        this.patientRepository = new PatientRepository();
    }

    public Response bookAppointment(CommandDTO command) {
        Long patientId = command.getLong("patientId");
        Long patientUserId = command.getLong("patientUserId");
        Long doctorId = command.getLong("doctorId");
        Long serviceId = command.getLong("serviceId");
        LocalDate date = command.getDate("date");
        LocalTime time = (LocalTime) command.getData().get("time");

        if (doctorId == null || date == null || time == null) {
            return Response.error("VALIDATION_ERROR", "Missing appointment details");
        }

        try {
            if (patientId == null && patientUserId != null) {
                patientId = patientRepository.findPatientIdByUserId(patientUserId);
            }
            if (patientId == null) {
                return Response.error("VALIDATION_ERROR", "Missing patient details");
            }

            boolean isFree = appointmentRepository.isSlotAvailable(doctorId, date, time);
            if (!isFree) {
                return Response.error("SLOT_TAKEN", "The selected slot is no longer available.");
            }

            long appointmentId = appointmentRepository.createAppointment(
                    patientId, doctorId, serviceId != null ? serviceId : 0, date, time, AppointmentStatus.PENDING
            );

            return Response.okMessage("Appointment requested successfully. ID: " + appointmentId);

        } catch (Exception e) {
            return Response.error("DB_ERROR", "Booking failed: " + e.getMessage());
        }
    }

    public Response updateAppointment(CommandDTO command) {
        try {
            Long appointmentId = command.getLong("appointmentId");
            if (appointmentId == null) {
                return Response.error("VALIDATION_ERROR", "Appointment ID is required");
            }

            String statusStr = command.getString("status");
            Long doctorId = command.getLong("doctorId");

            if (statusStr != null && doctorId == null) {
                try {
                    AppointmentStatus status = AppointmentStatus.valueOf(statusStr);
                    boolean updated = appointmentRepository.updateStatus(appointmentId, status);
                    if (updated) {
                        return Response.ok("OK");
                    } else {
                        return Response.error("DB_ERROR", "Failed to update status");
                    }
                } catch (IllegalArgumentException e) {
                    return Response.error("VALIDATION_ERROR", "Invalid status: " + statusStr);
                }
            }

            if (doctorId == null) {
                return Response.error("VALIDATION_ERROR", "Doctor ID is required for full update");
            }

            LocalDate date = command.getDate("date");
            Long serviceId = command.getLong("serviceId");

            LocalTime time = command.getTime("time");

            if (date == null || time == null) {
                return Response.error("VALIDATION_ERROR", "Date and Time are required");
            }

            boolean success = appointmentRepository.updateAppointment(
                    appointmentId,
                    doctorId,
                    serviceId,
                    date,
                    time
            );

            if (success) {
                return Response.ok("OK");
            } else {
                return Response.error("DB_ERROR", "Failed to update appointment details");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("SERVER_ERROR", e.getMessage());
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

    public Response getAvailableSlots(CommandDTO command) {
        Long doctorId = command.getLong("doctorId");
        LocalDate date = command.getDate("date");

        if (doctorId == null || date == null) {
            return Response.error("VALIDATION_ERROR", "Doctor ID and date are required");
        }

        try {
            int dayOfWeek = date.getDayOfWeek().getValue();
            List<DoctorSchedule> schedule = doctorRepository.findScheduleByDoctorId(doctorId);
            if (schedule.isEmpty()) {
                return Response.ok(List.of());
            }

            Set<LocalTime> booked = new HashSet<>(appointmentRepository.findBookedTimes(doctorId, date));
            List<LocalTime> available = new ArrayList<>();
            LocalTime now = LocalDate.now().equals(date) ? LocalTime.now().withSecond(0).withNano(0) : null;

            for (DoctorSchedule slot : schedule) {
                if (slot.getDayOfWeek() != dayOfWeek) {
                    continue;
                }
                LocalTime start = slot.getStartTime();
                LocalTime end = slot.getEndTime();
                if (start == null || end == null) {
                    continue;
                }
                LocalTime time = start;
                while (!time.plusMinutes(SLOT_MINUTES).isAfter(end)) {
                    if (!booked.contains(time)) {
                        if (now == null || time.isAfter(now)) {
                            available.add(time);
                        }
                    }
                    time = time.plusMinutes(SLOT_MINUTES);
                }
            }

            available.sort(LocalTime::compareTo);
            return Response.ok(available);
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response getDoctorAppointments(CommandDTO command) {
        Long doctorId = command.getLong("doctorId");
        LocalDate date = command.getDate("date");

        if (doctorId == null) {
            Long requesterUserId = command.getRequesterUserId();
            if (requesterUserId != null) {
                try {
                    doctorId = doctorRepository.findDoctorIdByUserId(requesterUserId);
                } catch (Exception e) {
                    return Response.error("DB_ERROR", e.getMessage());
                }
            }
        }

        try {
            List<Appointment> appointments;
            if (date == null) {
                if (doctorId == null) {
                    return Response.error("VALIDATION_ERROR", "Doctor ID is required when date is not provided");
                }
                appointments = appointmentRepository.findByDoctorId(doctorId);
            } else {
                appointments = doctorId == null
                        ? appointmentRepository.findByDate(date)
                        : appointmentRepository.findByDoctorId(doctorId, date);
            }
            return Response.ok(toDtos(appointments));
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response getMyAppointments(CommandDTO command) {
        Long patientId = command.getLong("patientId");
        if (patientId == null) {
            Long requesterUserId = command.getRequesterUserId();
            if (requesterUserId != null) {
                try {
                    patientId = patientRepository.findPatientIdByUserId(requesterUserId);
                } catch (Exception e) {
                    return Response.error("DB_ERROR", e.getMessage());
                }
            }
        }

        if (patientId == null) {
            return Response.error("VALIDATION_ERROR", "Patient ID is required");
        }

        try {
            List<Appointment> appointments =
                    appointmentRepository.findByPatientId(patientId);
            return Response.ok(toDtos(appointments));
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response getAllAppointments(CommandDTO command) {
        try {
            LocalDate date = command.getDate("date");
            String serviceName = command.getString("service");

            List<Appointment> allAppointments = appointmentRepository.findWithFilters(null, serviceName);

            if (date != null) {
                allAppointments = allAppointments.stream()
                        .filter(a -> date.equals(a.getAppointmentDate()))
                        .toList();
            }

            return Response.ok(toDtos(allAppointments));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response getPatientAppointments(CommandDTO command) {
        Long patientId = command.getLong("patientId");

        if (patientId == null) {
            return Response.error("VALIDATION_ERROR", "Patient ID is required");
        }

        try {
            List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
            return Response.ok(toDtos(appointments));
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

    private List<AppointmentDTO> toDtos(List<Appointment> appointments) {
        return appointments.stream()
                .map(this::toDto)
                .toList();
    }
}
