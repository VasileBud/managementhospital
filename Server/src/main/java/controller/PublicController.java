package controller;

import model.MedicalService;
import model.Specialization;
import model.repository.ConsultationRepository;
import model.repository.SpecializationRepository;
import model.common.Response;
import model.dto.MedicalServiceDTO;
import model.dto.SpecializationDTO;

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
            List<Specialization> list = specializationRepository.findAll();
            return Response.ok(list.stream().map(this::toDto).toList());
        } catch (Exception e) {
            return Response.error("DB_ERROR", "Could not fetch specializations: " + e.getMessage());
        }
    }

    public Response getMedicalServices() {
        try {
            List<MedicalService> list = consultationRepository.findAllServices();
            return Response.ok(list.stream().map(this::toDto).toList());
        } catch (Exception e) {
            return Response.error("DB_ERROR", "Could not fetch services: " + e.getMessage());
        }
    }

    private SpecializationDTO toDto(Specialization specialization) {
        return new SpecializationDTO(
                specialization.getSpecializationId(),
                specialization.getName()
        );
    }

    private MedicalServiceDTO toDto(MedicalService service) {
        return new MedicalServiceDTO(
                service.getServiceId(),
                service.getName(),
                service.getPrice()
        );
    }
}
