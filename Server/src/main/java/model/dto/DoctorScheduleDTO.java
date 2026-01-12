package model.dto;

import java.io.Serializable;
import java.time.LocalTime;

public class DoctorScheduleDTO implements Serializable {

    private long doctorId;
    private int dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public DoctorScheduleDTO(long doctorId,
                             int dayOfWeek,
                             LocalTime startTime,
                             LocalTime endTime) {
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
