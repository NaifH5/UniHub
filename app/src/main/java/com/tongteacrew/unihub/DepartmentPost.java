package com.tongteacrew.unihub;

import java.io.Serializable;
import java.util.ArrayList;

public class DepartmentPost implements Serializable {

    String posterProfilePicture, posterName, postTime, post, textAccountType;
    boolean adminApproved;
    ArrayList<String> mediaList;

    public DepartmentPost() {}

    public DepartmentPost(String posterProfilePicture, String posterName, String postTime, String post, String textAccountType, ArrayList<String> mediaList, boolean adminApproved) {
        this.posterProfilePicture = posterProfilePicture;
        this.posterName = posterName;
        this.postTime = postTime;
        this.post = post;
        this.textAccountType = textAccountType;
        this.mediaList = mediaList;
        this.adminApproved = adminApproved;
    }

    public String getPosterProfilePicture() {
        return posterProfilePicture;
    }

    public void setPosterProfilePicture(String posterProfilePicture) {
        this.posterProfilePicture = posterProfilePicture;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getTextAccountType() {
        return textAccountType;
    }

    public void setTextAccountType(String textAccountType) {
        this.textAccountType = textAccountType;
    }

    public boolean isAdminApproved() {
        return adminApproved;
    }

    public void setAdminApproved(boolean adminApproved) {
        this.adminApproved = adminApproved;
    }

    public ArrayList<String> getMediaList() {
        return mediaList;
    }

    public void setMediaList(ArrayList<String> mediaList) {
        this.mediaList = mediaList;
    }
}
