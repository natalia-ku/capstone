package com.example.user.android.capstone.model;

import java.util.Date;

/**
 * Created by nataliakuleniuk on 7/10/17.
 */

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private String messageEmail;
    private String eventId;
    private long messageTime;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public ChatMessage(String messageText, String messageUser, String messageEmail) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageEmail = messageEmail;
        messageTime = new Date().getTime();
    }
    public ChatMessage(String messageText, String messageUser,  long messageTime, String messageEmail, String eventId) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageEmail = messageEmail;
        this.eventId = eventId;
        this.messageTime = messageTime;
    }

    public ChatMessage(){

    }

    public String getMessageEmail() {
        return messageEmail;
    }

    public void setMessageEmail(String messageEmail) {
        this.messageEmail = messageEmail;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}