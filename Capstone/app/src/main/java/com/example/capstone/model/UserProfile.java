package com.example.capstone.model;

public class UserProfile {
    private Long id;
    private String username;
    private String firstName;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
}
