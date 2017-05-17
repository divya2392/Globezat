package com.codexgirl.globezat.message;

/**
 * Created by divya gupta on 5/13/2017.
 */

public class FriendlyMessage {
    private String message;
    private String language;
    private String email;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String message, String language, String email) {
        this.message = message;
        this.language = language;
        this.email = email;
    }

    public FriendlyMessage(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
