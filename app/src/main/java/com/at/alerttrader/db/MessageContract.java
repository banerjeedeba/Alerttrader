package com.at.alerttrader.db;

/**
 * Created by lenovo on 22-12-2017.
 */

public class MessageContract {

    public MessageContract(){

    }

    public static class MessagesDS{
        public static final String MESSAGES_TABLE_NAME = "messages";
        public static final String MESSAGE = "message";
        public static final String MESSAGE_TIME = "message_time";
        public static final String MESSAGE_STATUS = "message_status";
        public static final String MESSAGE_USER = "message_user";
    }
}
