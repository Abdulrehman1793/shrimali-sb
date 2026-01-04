package com.shrimali.modules.member.dto;

import com.shrimali.model.enums.MaritalStatus;
import com.shrimali.model.enums.MemberStatus;
import jakarta.validation.constraints.Pattern;

public record MemberFilterRequest(
//        @Pattern(
//                regexp = "^[a-zA-Z0-9\\s]*$",
//                message = "Search query must contain only English characters"
//        )
        String q,
        String village,
        String gotra,
        MaritalStatus maritalStatus,
        MemberStatus status
) {}
