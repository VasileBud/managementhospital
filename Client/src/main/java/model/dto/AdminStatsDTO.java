package model.dto;

import java.io.Serializable;

public class AdminStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long totalDoctors;
    private long totalPatients;
    private long activeAccounts;
    private long newAccountsToday;

    private double totalDoctorsChange;
    private double totalPatientsChange;
    private double activeAccountsChange;
    private double newAccountsChange;

    public AdminStatsDTO(long totalDoctors,
                         long totalPatients,
                         long activeAccounts,
                         long newAccountsToday,
                         double totalDoctorsChange,
                         double totalPatientsChange,
                         double activeAccountsChange,
                         double newAccountsChange) {
        this.totalDoctors = totalDoctors;
        this.totalPatients = totalPatients;
        this.activeAccounts = activeAccounts;
        this.newAccountsToday = newAccountsToday;
        this.totalDoctorsChange = totalDoctorsChange;
        this.totalPatientsChange = totalPatientsChange;
        this.activeAccountsChange = activeAccountsChange;
        this.newAccountsChange = newAccountsChange;
    }

    public long getTotalDoctors() { return totalDoctors; }
    public long getTotalPatients() { return totalPatients; }
    public long getActiveAccounts() { return activeAccounts; }
    public long getNewAccountsToday() { return newAccountsToday; }

    public double getTotalDoctorsChange() { return totalDoctorsChange; }
    public double getTotalPatientsChange() { return totalPatientsChange; }
    public double getActiveAccountsChange() { return activeAccountsChange; }
    public double getNewAccountsChange() { return newAccountsChange; }
}
