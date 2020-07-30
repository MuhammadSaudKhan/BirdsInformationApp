package com.material.recipe.model;

public class FcmNotif {

    public String title, content;

    public FcmNotif() {
    }

    public FcmNotif(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
