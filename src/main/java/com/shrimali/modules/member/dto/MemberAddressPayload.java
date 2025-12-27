package com.shrimali.modules.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAddressPayload {

    @NotBlank
    private String addressType; // CURRENT / PERMANENT

    @NotBlank
    private String line1;

    private String line2;

    private String areaLocality;

    @NotBlank
    private String city;

    private String district;

    @NotBlank
    private String state;

    private String country;

    private String pincode;
}

