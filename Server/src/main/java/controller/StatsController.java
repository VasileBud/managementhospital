package controller;

import model.ChartPoint;
import model.repository.StatsRepository;
import model.common.Response;
import model.dto.ChartPointDTO;
import model.dto.CommandDTO;
import model.dto.StatsDTO;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsController {

    private final StatsRepository statsRepository;

    public StatsController() {
        this.statsRepository = new StatsRepository();
    }

    public Response getStats(CommandDTO command) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            long patientsToday = statsRepository.getAppointmentsCountByDate(today);
            long patientsYesterday = statsRepository.getAppointmentsCountByDate(yesterday);
            double patientsChange = calculatePercentageChange(patientsToday, patientsYesterday);

            long activeToday = statsRepository.getActiveConsultationsCount(today);
            long activeYesterday = statsRepository.getActiveConsultationsCount(yesterday);
            double activeChange = calculatePercentageChange(activeToday, activeYesterday);

            long totalDoctors = statsRepository.getTotalDoctors();
            long maxCapacity = (totalDoctors == 0 ? 1 : totalDoctors) * 8;
            double occupancyPercent = ((double) patientsToday / maxCapacity) * 100.0;
            double occupancyPercentYesterday = ((double) patientsYesterday / maxCapacity) * 100.0;
            double occupancyChange = occupancyPercent - occupancyPercentYesterday;

            Map<LocalDate, Long> rawFlowData = statsRepository.getLast7DaysStats();
            List<ChartPointDTO> flowSeries = new ArrayList<>();

            LocalDate current = LocalDate.now().minusDays(6);

            for (int i = 0; i < 7; i++) {
                long count = rawFlowData.getOrDefault(current, 0L);

                String label = current.getDayOfWeek()
                        .getDisplayName(TextStyle.SHORT, new Locale("ro", "RO"));
                label = label.substring(0, 1).toUpperCase() + label.substring(1);

                flowSeries.add(new ChartPointDTO(label, (double) count));

                current = current.plusDays(1);
            }

            List<ChartPoint> occupancyBySpecModels = statsRepository.getAppointmentsBySpecialization();
            List<ChartPointDTO> occupancyBySpec = new ArrayList<>();
            for (ChartPoint point : occupancyBySpecModels) {
                occupancyBySpec.add(new ChartPointDTO(point.getLabel(), point.getValue()));
            }

            StatsDTO stats = new StatsDTO(
                    0, 0,
                    patientsToday, activeToday, occupancyPercent, 0.0,
                    patientsChange, activeChange, occupancyChange, 0.0,

                    flowSeries,
                    null,
                    occupancyBySpec,
                    null
            );

            return Response.ok(stats);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("SERVER_ERROR", e.getMessage());
        }
    }

    private double calculatePercentageChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }
}
