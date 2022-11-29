package com.gingineers.sidekick.admin;

public class FeedbackModel {

    String email, feedback, date, timestamp, id;

    public FeedbackModel() {
    }

    public FeedbackModel(String email, String feedback, String date, String timestamp, String id) {
        this.email = email;
        this.feedback = feedback;
        this.date = date;
        this.timestamp = timestamp;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
