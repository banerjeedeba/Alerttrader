package com.at.alerttrader.model;

public class DBMessageModel {

    private String messageText;
    private long messageTimeInMillis;

    public String getMessageText() {
        return messageText;
    }

    public long getMessageTimeInMillis() {
        return messageTimeInMillis;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setMessageTimeInMillis(long messageTimeInMillis) {
        this.messageTimeInMillis = messageTimeInMillis;
    }

    public DBMessageModel(String messageText, long messageTimeInMillis) {
        this.messageText = messageText;
        this.messageTimeInMillis = messageTimeInMillis;
    }

    public DBMessageModel() {
    }
}
