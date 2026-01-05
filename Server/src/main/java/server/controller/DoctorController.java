package server.controller;

import shared.common.Response;
import shared.dto.CommandDTO;
import shared.dto.DoctorDTO;
import shared.dto.DoctorScheduleDTO;
import server.repository.DoctorRepository;

import java.util.List;

public class DoctorController {

    private final DoctorRepository doctorRepository;

    public DoctorController() {
        this.doctorRepository = new DoctorRepository();
    }

    public Response getDoctors() {
        try {
            List<DoctorDTO> doctors = doctorRepository.findAllDoctors();
            return Response.ok(doctors);
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
            List<DoctorScheduleDTO> schedule = doctorRepository.findScheduleByDoctorId(doctorId);
            return Response.ok(schedule);
        } catch (Exception e) {
            return Response.error("DB_ERROR", "Could not fetch schedule: " + e.getMessage());
        }
    }
}