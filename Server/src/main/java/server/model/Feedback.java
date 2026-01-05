package server.model;

import java.time.OffsetDateTime;

public class Feedback {
    private long feedbackId;
    private long appointmentId;
    private int rating; // 1..5
    private String comment;
    private OffsetDateTime createdAt;

    public Feedback() {}

    public Feedback(long feedbackId, long appointmentId, int rating,
                    String comment, OffsetDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(long feedbackId) { this.feedbackId = feedbackId; }

    public long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(long appointmentId) { this.appointmentId = appointmentId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
