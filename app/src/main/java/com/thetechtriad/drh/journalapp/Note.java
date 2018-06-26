package com.thetechtriad.drh.journalapp;

public class Note {
    private String Title;
    private String content;
    private String date;
    private Boolean isFavourite;

    Note(String title, String content, String date, Boolean isFavourite) {
        Title = title;
        this.content = content;
        this.date = date;
        this.isFavourite = isFavourite;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favourite) {
        isFavourite = favourite;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
