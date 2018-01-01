package com.at.alerttrader.model;

import android.support.annotation.NonNull;

/**
 * Created by lenovo on 14-12-2017.
 */

public class MessageModel {

    private String messageText;
    private String messageTime;

    public MessageModel(String messageText, String messageTime) {
        this.messageText = messageText;
        this.messageTime = messageTime;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }


}
