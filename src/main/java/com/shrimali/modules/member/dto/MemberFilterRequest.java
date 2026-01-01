package com.shrimali.modules.member.dto;

public record MemberFilterRequest(
        String q,
        String village,
        String gotra,
        String maritalStatus
) {}
