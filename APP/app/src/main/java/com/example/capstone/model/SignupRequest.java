package com.example.capstone.model;

public class SignupRequest {
    private String username;
    private String password;
    private String firstName;

    public SignupRequest(String username, String password, String firstName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
}
