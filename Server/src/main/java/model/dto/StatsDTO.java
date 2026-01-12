package model.dto;

import java.io.Serializable;
import java.util.List;

public class StatsDTO implements Serializable {

    private long totalPatients;
    private long totalConsultations;

    private long patientsToday;
    private long activeConsultations;
    private double doctorOccupancyPercent;
    private double revenueEstimate;

    private double patientsTodayChange;
    private double activeConsultationsChange;
    private double doctorOccupancyChange;
    private double revenueChange;

    private List<ChartPointDTO> series;
    private List<ChartPointDTO> admissionsSeries;
    private List<ChartPointDTO> dischargesSeries;
    private List<ChartPointDTO> occupancyBySpecialization;

    private List<AppointmentDTO> recentAppointments;

    public StatsDTO(long totalPatients, long totalConsultations, List<ChartPointDTO> series) {
        this.totalPatients = totalPatients;
        this.totalConsultations = totalConsultations;
        this.series = series;
    }

    public StatsDTO(long totalPatients,
                    long totalConsultations,
                    long patientsToday,
                    long activeConsultations,
                    double doctorOccupancyPercent,
                    double revenueEstimate,
                    double patientsTodayChange,
                    double activeConsultationsChange,
                    double doctorOccupancyChange,
                    double revenueChange,
                    List<ChartPointDTO> admissionsSeries,
                    List<ChartPointDTO> dischargesSeries,
                    List<ChartPointDTO> occupancyBySpecialization,
                    List<AppointmentDTO> recentAppointments) {

        this.totalPatients = totalPatients;
        this.totalConsultations = totalConsultations;
        this.patientsToday = patientsToday;
        this.activeConsultations = activeConsultations;
        this.doctorOccupancyPercent = doctorOccupancyPercent;
        this.revenueEstimate = revenueEstimate;
        this.patientsTodayChange = patientsTodayChange;
        this.activeConsultationsChange = activeConsultationsChange;
        this.doctorOccupancyChange = doctorOccupancyChange;
        this.revenueChange = revenueChange;

        this.admissionsSeries = admissionsSeries;
        this.dischargesSeries = dischargesSeries;
        this.occupancyBySpecialization = occupancyBySpecialization;
        this.recentAppointments = recentAppointments;

        this.series = admissionsSeries;
    }

    public long getTotalPatients() { return totalPatients; }
    public long getTotalConsultations() { return totalConsultations; }
    public List<ChartPointDTO> getSeries() { return series; }

    public long getPatientsToday() { return patientsToday; }
    public long getActiveConsultations() { return activeConsultations; }
    public double getDoctorOccupancyPercent() { return doctorOccupancyPercent; }
    public double getRevenueEstimate() { return revenueEstimate; }

    public double getPatientsTodayChange() { return patientsTodayChange; }
    public double getActiveConsultationsChange() { return activeConsultationsChange; }
    public double getDoctorOccupancyChange() { return doctorOccupancyChange; }
    public double getRevenueChange() { return revenueChange; }

    public List<ChartPointDTO> getAdmissionsSeries() { return admissionsSeries; }
    public List<ChartPointDTO> getDischargesSeries() { return dischargesSeries; }
    public List<ChartPointDTO> getOccupancyBySpecialization() { return occupancyBySpecialization; }

    public List<AppointmentDTO> getRecentAppointments() { return recentAppointments; }
}
