package controller;

import model.Doctor;
import model.DoctorSchedule;
import model.repository.DoctorRepository;
import model.common.Response;
import model.dto.CommandDTO;
import model.dto.DoctorDTO;
import model.dto.DoctorScheduleDTO;

import java.util.List;

public class DoctorController {

    private final DoctorRepository doctorRepository;

    public DoctorController() {
        this.doctorRepository = new DoctorRepository();
    }

    public Response getDoctors() {
        try {
            List<Doctor> doctors = doctorRepository.findAllDoctors();
            return Response.ok(doctors.stream().map(this::toDto).toList());
        } catch (Exception e) {
            return Response.error("DB_ERROR", "Could not fetch doctors: " + e.getMessage());
        }
    }

    public Response getDoctorSchedule(CommandDTO command) {
        Long doctorId = command.getLong("doctorId");

        if (doctorId == null) {
            return Response.error("VALIDATION_ERROR", "Doctor ID required");
        }

        try {
            List<DoctorSchedule> schedule = doctorRepository.findScheduleByDoctorId(doctorId);
            return Response.ok(schedule.stream().map(this::toDto).toList());
        } catch (Exception e) {
            return Response.error("DB_ERROR", "Could not fetch schedule: " + e.getMessage());
        }
    }

    private DoctorDTO toDto(Doctor doctor) {
        return new DoctorDTO(
                doctor.getDoctorId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecializationName()
        );
    }

    private DoctorScheduleDTO toDto(DoctorSchedule schedule) {
        return new DoctorScheduleDTO(
                schedule.getDoctorId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }
}
