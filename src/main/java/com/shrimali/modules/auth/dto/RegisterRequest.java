package com.shrimali.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String firstName;
    private String middleName;
    private String lastName;

    private String username;
    private String phone;

    @NotBlank
    @Email
    private String email;

    @Size(min = 5, message = "Password must be at least 10 characters")
    private String password;

    private String gender;
    private String dob;
}
