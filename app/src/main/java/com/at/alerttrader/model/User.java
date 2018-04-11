package com.at.alerttrader.model;

/**
 * Created by lenovo on 21-03-2018.
 */

public class User {

    public static final String STATUS_APPROVED = "A";
    public static final String STATUS_PENDING = "P";
    public static final String STATUS_EXPIRED = "E";

    private String id;
    private String email;
    private String displayName;
    private String status;
    private String lastLogin;
    private String validTill;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getValidTill() {
        return validTill;
    }

    public void setValidTill(String validTill) {
        this.validTill = validTill;
    }

    public User(String id, String email, String displayName, String status, String lastLogin, String validTill) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.status = status;
        this.lastLogin = lastLogin;
        this.validTill = validTill;
    }

    public User() {
        
    }
}
