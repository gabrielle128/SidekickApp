package com.gingineers.sidekick.calendar;

public class EventModel {

    private String id, name, content, date, time, timestamp;

    public EventModel() {
    }

    public EventModel(String id, String name, String content, String date, String time, String timestamp) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.date = date;
        this.time = time;
        this.timestamp  = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
