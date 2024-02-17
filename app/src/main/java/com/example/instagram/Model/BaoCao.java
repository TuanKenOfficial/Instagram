package com.example.instagram.Model;

public class BaoCao {
    private String postid;
    private String uid;
    private String reason;
    private String time;
    private String date;

    public BaoCao(String postid, String uid, String reason, String time, String date) {
        this.postid = postid;
        this.uid = uid;
        this.reason = reason;
        this.time = time;
        this.date = date;
    }

    public BaoCao(){
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
