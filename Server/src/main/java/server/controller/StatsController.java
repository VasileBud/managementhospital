package server.controller;

import shared.common.Response;
import shared.dto.ChartPointDTO;
import shared.dto.CommandDTO;
import shared.dto.StatsDTO;
import server.repository.StatsRepository;

import java.time.LocalDate;
import java.util.List;

public class StatsController {

    private final StatsRepository statsRepo;

    public StatsController() {
        this.statsRepo = new StatsRepository();
    }

    public Response getStats(CommandDTO command) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            long totalPatients = statsRepo.countTotalPatients();
            long totalConsultations = statsRepo.countTotalConsultations();

            long patientsToday = statsRepo.countPatientsOnDate(today);
            long patientsYesterday = statsRepo.countPatientsOnDate(yesterday);

            long activeConsultations = statsRepo.countActiveConsultations(today);
            long activeConsultationsYesterday = statsRepo.countActiveConsultations(yesterday);

            long totalDoctors = statsRepo.countTotalDoctors();
            long busyDoctors = statsRepo.countDoctorsWithAppointments(today);
            long busyDoctorsYesterday = statsRepo.countDoctorsWithAppointments(yesterday);

            double doctorOccupancy = totalDoctors == 0 ? 0.0 : (busyDoctors * 100.0) / totalDoctors;
            double doctorOccupancyYesterday = totalDoctors == 0 ? 0.0 : (busyDoctorsYesterday * 100.0) / totalDoctors;

            double revenueEstimate = statsRepo.sumRevenueForMonth(today);
            double revenueLastMonth = statsRepo.sumRevenueForMonth(today.minusMonths(1));

            double patientsChange = percentChange(patientsToday, patientsYesterday);
            double activeChange = percentChange(activeConsultations, activeConsultationsYesterday);
            double occupancyChange = percentChange(doctorOccupancy, doctorOccupancyYesterday);
            double revenueChange = percentChange(revenueEstimate, revenueLastMonth);

            List<ChartPointDTO> admissionsSeries =
                    statsRepo.getAppointmentSeries(today.minusDays(6), today, false);
            List<ChartPointDTO> dischargesSeries =
                    statsRepo.getAppointmentSeries(today.minusDays(6), today, true);

            List<ChartPointDTO> occupancyCounts = statsRepo.getAppointmentsBySpecialization();
            List<ChartPointDTO> occupancyBySpecialization = statsRepo.toPercentSeries(occupancyCounts);

            StatsDTO stats = new StatsDTO(
                    totalPatients,
                    totalConsultations,
                    patientsToday,
                    activeConsultations,
                    doctorOccupancy,
                    revenueEstimate,
                    patientsChange,
                    activeChange,
                    occupancyChange,
                    revenueChange,
                    admissionsSeries,
                    dischargesSeries,
                    occupancyBySpecialization,
                    statsRepo.findRecentAppointments(5)
            );
            return Response.ok(stats);

        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    private double percentChange(double current, double previous) {
        if (previous == 0) {
            return current == 0 ? 0.0 : 100.0;
        }
        return ((current - previous) / previous) * 100.0;
    }
}
