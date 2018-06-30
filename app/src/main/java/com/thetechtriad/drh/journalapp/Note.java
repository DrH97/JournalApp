package com.thetechtriad.drh.journalapp;

public class Note {
    private String noteId;
    private String userId;
    private String title;
    private String content;
    private String date;
    private Boolean favourite;
    private Boolean deleted;

    public Note() {
    }

    Note(String noteId, String userId, String title, String content, String date, Boolean favourite, Boolean deleted) {
        this.noteId = noteId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.favourite = favourite;
        this.deleted = deleted;
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

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
