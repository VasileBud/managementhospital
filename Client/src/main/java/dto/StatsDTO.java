package dto;

import java.io.Serializable;
import java.util.List;

public class StatsDTO implements Serializable {

    private long totalPatients;
    private long totalConsultations;

    // Generic chart-friendly series:
    // - group by day/week/specialization/doctor
    // - used for bar/line/radial charts
    private List<ChartPointDTO> series;

    public StatsDTO(long totalPatients, long totalConsultations, List<ChartPointDTO> series) {
        this.totalPatients = totalPatients;
        this.totalConsultations = totalConsultations;
        this.series = series;
    }

    public long getTotalPatients() { return totalPatients; }
    public long getTotalConsultations() { return totalConsultations; }
    public List<ChartPointDTO> getSeries() { return series; }
}
