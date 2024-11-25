package com.tongteacrew.unihub;

import java.io.Serializable;

public class Replies implements Serializable {

    String replierProfilePicture, replierName, reply;

    public Replies(){};

    public Replies(String replierProfilePicture, String replierName, String reply) {
        this.replierProfilePicture = replierProfilePicture;
        this.replierName = replierName;
        this.reply = reply;
    }

    public String getReplierProfilePicture() {
        return replierProfilePicture;
    }

    public void setReplierProfilePicture(String replierProfilePicture) {
        this.replierProfilePicture = replierProfilePicture;
    }

    public String getReplierName() {
        return replierName;
    }

    public void setReplierName(String replierName) {
        this.replierName = replierName;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
