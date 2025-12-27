package com.shrimali.modules.auth.dto;

import com.shrimali.model.enums.AuthProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDto {
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private String password;
    private String gender;
    private String photoUrl;
    private LocalDate dob;
    private boolean emailVerified;
    private AuthProviderType authProvider;
}
