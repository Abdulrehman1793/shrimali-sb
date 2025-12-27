package com.shrimali.modules.member.dto;

import java.io.Serializable;

public record MemberAccessPayload(Long memberId, Boolean approved,Boolean remove) implements Serializable {
}
