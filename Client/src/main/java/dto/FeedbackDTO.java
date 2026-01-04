package dto;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class FeedbackDTO implements Serializable {
    private long appointmentId;
    private int rating; // 1..5
    private String comment;
    private OffsetDateTime createdAt;

    public FeedbackDTO(long appointmentId, int rating, String comment, OffsetDateTime createdAt) {
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public long getAppointmentId() { return appointmentId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
