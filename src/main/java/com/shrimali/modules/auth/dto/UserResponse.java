package com.shrimali.modules.auth.dto;

import com.shrimali.model.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String firstName;
    private String middleName;
    private String lastName;

    private String email;
    private Boolean emailVerified;
    private String phone;
    private Boolean phoneVerified;

    private String gender;
    private String dob;

    private RoleName role;

    private String photoUrl;

    private boolean completed;
    private int completionPercentage;
}
