package com.trackiq.ClientPortalPro.invoices;

public class Document {
    private String title;
    private String date;
    private String url;

    // Firestore requires an empty constructor
    public Document() {}

    public Document(String title, String date, String url) {
        this.title = title;
        this.date = date;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }
}
