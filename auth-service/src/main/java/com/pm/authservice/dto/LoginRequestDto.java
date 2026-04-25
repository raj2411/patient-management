package com.pm.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid email address.")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be 8 charactors long")
    private String password;

    public @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be 8 charactors long") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be 8 charactors long") String password) {
        this.password = password;
    }

    public @NotBlank(message = "Email is required") @Email(message = "Email should be valid email address.") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid email address.") String email) {
        this.email = email;
    }
}
