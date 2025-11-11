package com.getwork.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserRequest {

    // Getters and Setters
    @NotBlank
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Email(message = "Invalid email")
    private String email;

    private String role;
    private String languagePref;

    public UserRequest() {}

    public UserRequest(String phone, String name, String email, String role, String languagePref) {
        this.phone = phone;
        this.name = name;
        this.email = email;
        this.role = role;
        this.languagePref = languagePref;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setLanguagePref(String languagePref) {
        this.languagePref = languagePref;
    }
}
