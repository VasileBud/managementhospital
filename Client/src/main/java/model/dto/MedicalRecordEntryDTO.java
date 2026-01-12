package model.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class MedicalRecordEntryDTO implements Serializable {
    private long entryId;
    private long patientId;

    private Long appointmentId;
    private String doctorName;

    private String diagnosis;
    private String treatment;
    private String notes;

    private OffsetDateTime entryDate;

    public MedicalRecordEntryDTO(long entryId, long patientId, Long appointmentId, String doctorName,
                                 String diagnosis, String treatment, String notes,
                                 OffsetDateTime entryDate) {
        this.entryId = entryId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.entryDate = entryDate;
    }

    public long getEntryId() { return entryId; }
    public long getPatientId() { return patientId; }
    public Long getAppointmentId() { return appointmentId; }
    public String getDoctorName() { return doctorName; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatment() { return treatment; }
    public String getNotes() { return notes; }
    public OffsetDateTime getEntryDate() { return entryDate; }
}
