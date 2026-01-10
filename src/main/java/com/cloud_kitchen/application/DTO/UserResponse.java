package com.cloud_kitchen.application.DTO;

import com.cloud_kitchen.application.Entity.Role;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;
    private Boolean verified;
    
    public UserResponse() {
    }

    public UserResponse(Long id, String email, String name, String phoneNumber, String role, Boolean active, LocalDateTime createdAt, Boolean verified) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.verified = verified;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}
