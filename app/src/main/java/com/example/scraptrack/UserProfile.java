package com.example.scraptrack; // or com.example.scraptrack.models if you created a models package

public class UserProfile {
    private String email;
    private String password;
    private String profilePicUrl;

    // No-argument constructor for Firebase
    public UserProfile() { }

    // Constructor with fields
    public UserProfile(String email, String password, String profilePicUrl) {
        this.email = email;
        this.password = password;
        this.profilePicUrl = profilePicUrl;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
