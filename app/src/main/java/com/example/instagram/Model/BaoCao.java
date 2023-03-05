package com.example.instagram.Model;

public class BaoCao {
    private String id;
    private String baocao;
    private String publisher;
    private String username;

    public BaoCao(String id, String baocao, String publisher, String username) {
        this.id = id;
        this.baocao = baocao;
        this.publisher = publisher;
        this.username = username;
    }
    public BaoCao(){
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaocao() {
        return baocao;
    }

    public void setBaocao(String baocao) {
        this.baocao = baocao;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
