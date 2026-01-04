package dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class AvailableSlotDTO implements Serializable {
    private long doctorId;
    private LocalDate date;
    private LocalTime time;

    public AvailableSlotDTO(long doctorId, LocalDate date, LocalTime time) {
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
    }

    public long getDoctorId() { return doctorId; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
}
