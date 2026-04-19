package com.trackiq.ClientPortalPro.dashboard;

public class Milestone {
    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_COMPLETED = 2;

    private String title;
    private int status;

    public Milestone() {
        // Empty constructor needed for Firestore
    }

    public Milestone(String title, int status) {
        this.title = title;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }
}
