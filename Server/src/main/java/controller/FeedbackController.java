package controller;

import common.Response;
import dto.CommandDTO;
import repository.FeedbackRepository; // Va trebui creat

public class FeedbackController {

    private final FeedbackRepository feedbackRepo;

    public FeedbackController() {
        this.feedbackRepo = new FeedbackRepository();
    }

    public Response sendFeedback(CommandDTO command) {
        Long appointmentId = command.getLong("appointmentId");
        Integer rating = command.getInt("rating");
        String comment = command.getString("comment");

        if (rating == null || rating < 1 || rating > 5) {
            return Response.error("VALIDATION_ERROR", "Rating must be between 1 and 5");
        }

        try {
            feedbackRepo.createFeedback(appointmentId, rating, comment);
            return Response.okMessage("Feedback sent. Thank you!");
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }
}