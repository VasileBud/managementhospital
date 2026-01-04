package model;

import java.time.OffsetDateTime;

public class MedicalRecordEntry {
    private long entryId;
    private long fileId;
    private Long appointmentId; // nullable
    private String diagnosis;
    private String treatment;
    private String notes;
    private OffsetDateTime entryDate;

    public MedicalRecordEntry() {}

    public MedicalRecordEntry(long entryId, long fileId, Long appointmentId,
                              String diagnosis, String treatment, String notes, OffsetDateTime entryDate) {
        this.entryId = entryId;
        this.fileId = fileId;
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.entryDate = entryDate;
    }

    public long getEntryId() { return entryId; }
    public void setEntryId(long entryId) { this.entryId = entryId; }

    public long getFileId() { return fileId; }
    public void setFileId(long fileId) { this.fileId = fileId; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(OffsetDateTime entryDate) { this.entryDate = entryDate; }
}
