package com.gingineers.sidekick.todo;

public class Model {

    private String title, description, id, date, timestamp;

    public Model(){
    }

    public Model(String title, String description, String id, String date, String timestamp) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
