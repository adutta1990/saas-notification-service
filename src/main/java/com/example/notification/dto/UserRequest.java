package com.example.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    private String tenantId;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;

    private String name;
}