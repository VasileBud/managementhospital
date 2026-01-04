package model;

import java.time.LocalDate;

public class Feedback {
    private int idFeedback;
    private Programare programare;
    private int rating;
    private String comentariu;
    private LocalDate dataFeedback;

    public Feedback() {
    }

    public Feedback(int idFeedback, Programare programare, int rating, String comentariu, LocalDate dataFeedback) {
        this.idFeedback = idFeedback;
        this.programare = programare;
        this.rating = rating;
        this.comentariu = comentariu;
        this.dataFeedback = dataFeedback;
    }

    public int getIdFeedback() {
        return idFeedback;
    }

    public void setIdFeedback(int idFeedback) {
        this.idFeedback = idFeedback;
    }

    public Programare getProgramare() {
        return programare;
    }

    public void setProgramare(Programare programare) {
        this.programare = programare;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComentariu() {
        return comentariu;
    }

    public void setComentariu(String comentariu) {
        this.comentariu = comentariu;
    }

    public LocalDate getDataFeedback() {
        return dataFeedback;
    }

    public void setDataFeedback(LocalDate dataFeedback) {
        this.dataFeedback = dataFeedback;
    }
}
