package model;

import java.time.OffsetDateTime;

public class PatientMedicalFile {
    private long fileId;
    private long patientId;
    private OffsetDateTime createdAt;

    public PatientMedicalFile() {}

    public PatientMedicalFile(long fileId, long patientId, OffsetDateTime createdAt) {
        this.fileId = fileId;
        this.patientId = patientId;
        this.createdAt = createdAt;
    }

    public long getFileId() { return fileId; }
    public void setFileId(long fileId) { this.fileId = fileId; }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
