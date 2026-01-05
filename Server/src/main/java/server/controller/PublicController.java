package server.controller;

import shared.common.Response;
import shared.dto.MedicalServiceDTO;
import shared.dto.SpecializationDTO;
import server.repository.ConsultationRepository;
import server.repository.SpecializationRepository;

import java.util.List;

public class PublicController {

    private final SpecializationRepository specializationRepository;
    private final ConsultationRepository consultationRepository;

    public PublicController() {
        this.specializationRepository = new SpecializationRepository();
        this.consultationRepository = new ConsultationRepository();
    }

    public Response getSpecializations() {
        try {
            List<SpecializationDTO> list = specializationRepository.findAll();
            return Response.ok(list);
        } catch (Exception e) {
            return Response.error("DB_ERROR", "Could not fetch specializations: " + e.getMessage());
        }
    }

    public Response getMedicalServices() {
        try {
            List<MedicalServiceDTO> list = consultationRepository.findAllServices();
            return Response.ok(list);
        } catch (Exception e) {
            return Response.error("DB_ERROR", "Could not fetch services: " + e.getMessage());
        }
    }
}
