package com.example.instagram.Model;

public class Notification {
    private String idNotification;
    private String useridanh; //tài khoản người dùng thích ảnh
    private String userid; // tài khoản người dùng ảnh
    private String text;
    private String postid;
    private boolean ispost;

    public Notification(){
    }

    public Notification(String idNotification, String useridanh, String userid, String text, String postid, boolean ispost) {
        this.idNotification = idNotification;
        this.useridanh = useridanh;
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.ispost = ispost;
    }

    public String getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(String idNotification) {
        this.idNotification = idNotification;
    }

    public String getUseridanh() {
        return useridanh;
    }

    public void setUseridanh(String useridanh) {
        this.useridanh = useridanh;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }
}
