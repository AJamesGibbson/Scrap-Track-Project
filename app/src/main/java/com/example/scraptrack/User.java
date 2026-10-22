package com.example.scraptrack;

public class User {
    public String name;
    public String phoneNumber;
    public String email;
    public String sessionId;

    public User(String name, String phoneNumber, String email, String sessionId) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.sessionId = sessionId;
    }
}
