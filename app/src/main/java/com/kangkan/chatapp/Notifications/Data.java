package com.kangkan.chatapp.Notifications;

public class Data {
    private int icon;
    private String user,body,title,sender;


    public Data() {
    }

    public Data(int icon, String user, String body, String title, String sender) {
        this.icon = icon;
        this.user = user;
        this.body = body;
        this.title = title;
        this.sender = sender;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getsender() {
        return sender;
    }

    public void setsender(String sender) {
        this.sender = sender;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
