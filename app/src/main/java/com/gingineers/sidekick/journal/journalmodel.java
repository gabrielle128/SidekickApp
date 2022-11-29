package com.gingineers.sidekick.journal;

public class journalmodel {

    private String title;
    private String content;
    private String dateTime;
    private String timestamp;
    private String color;

    public journalmodel(){
    }

    public journalmodel (String title, String content, String dateTime, String timestamp, String color){
        this.title = title;
        this.content = content;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
