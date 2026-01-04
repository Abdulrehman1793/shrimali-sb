package com.shrimali.modules.member.dto;

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
        String maritalStatus,
        MemberStatus status
) {}
