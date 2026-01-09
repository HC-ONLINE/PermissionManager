package com.hconline.permissionmanager.dto;

public class UpdateUserRequest {
    private String email;
    private String password;
    
    public UpdateUserRequest() {}
    
    public UpdateUserRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
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
}
