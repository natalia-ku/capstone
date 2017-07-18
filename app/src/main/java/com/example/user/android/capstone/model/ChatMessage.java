package com.example.user.android.capstone.model;

import java.util.Date;

/**
 * Created by nataliakuleniuk on 7/10/17.
 */

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private String messageEmail;
    private long messageTime;

    public ChatMessage(String messageText, String messageUser, String messageEmail) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageEmail = messageEmail;
        messageTime = new Date().getTime();
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