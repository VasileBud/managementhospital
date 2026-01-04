package controller;

import common.Response;
import dto.ChartPointDTO;
import dto.CommandDTO;
import dto.StatsDTO;
import repository.StatsRepository; // Recomand să faci un repo separat pentru statistici complexe

import java.util.ArrayList;
import java.util.List;

public class StatsController {

    private final StatsRepository statsRepo;

    public StatsController() {
        this.statsRepo = new StatsRepository();
    }

    public Response getStats(CommandDTO command) {
        try {
            // Exemplu: Colectăm datele
            long totalPatients = statsRepo.countTotalPatients();
            long totalConsultations = statsRepo.countTotalConsultations();

            // Exemplu: Grafic programări pe specializări
            List<ChartPointDTO> series = statsRepo.getAppointmentsBySpecialization();

            StatsDTO stats = new StatsDTO(totalPatients, totalConsultations, series);
            return Response.ok(stats);

        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }
}