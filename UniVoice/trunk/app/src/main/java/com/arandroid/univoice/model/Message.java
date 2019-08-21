package com.arandroid.univoice.model;

import java.io.Serializable;

public class Message implements Serializable, Filterable{
    private String senderUid;
    private String receiverUid;
    private String originalLocale;
    private String message;
    private String date;

    public static final String DATE_KEY = "date";

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getOriginalLocale() {
        return originalLocale;
    }

    public void setOriginalLocale(String originalLocale) {
        this.originalLocale = originalLocale;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean isCompliant(String filter) {
        return true;
    }
}
