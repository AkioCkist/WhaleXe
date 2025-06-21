package com.midterm.mobiledesignfinalterm.admin.models;

public class UserInfo {
    private int accountId;
    private String name;
    private String phoneNumber;
    private String email;
    private String createdAt;

    public UserInfo() {}

    public UserInfo(int accountId, String name, String phoneNumber, String email, String createdAt) {
        this.accountId = accountId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
