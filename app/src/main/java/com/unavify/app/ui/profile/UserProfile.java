package com.unavify.app.ui.profile;

public class UserProfile {
    private String userId;
    private String username;
    private String phoneNumber;
    private String email;
    private String profileImageUrl;
    
    // Default constructor for Firestore
    public UserProfile() {}
    
    public UserProfile(String userId, String username, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }
    
    public UserProfile(String userId, String username, String phoneNumber, String email, String profileImageUrl) {
        this.userId = userId;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getProfileImageUrl() { return profileImageUrl; }
    
    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
} 