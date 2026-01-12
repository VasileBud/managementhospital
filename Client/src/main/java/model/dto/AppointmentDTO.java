package model.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentDTO implements Serializable {
    private long appointmentId;

    private long patientId;
    private String patientName;

    private long doctorId;
    private String doctorName;

    private String serviceName;

    private LocalDate date;
    private LocalTime time;

    private String status;

    public AppointmentDTO(long appointmentId,
                          long patientId, String patientName,
                          long doctorId, String doctorName,
                          String serviceName,
                          LocalDate date, LocalTime time,
                          String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.serviceName = serviceName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public long getAppointmentId() {
        return appointmentId;
    }

    public long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
