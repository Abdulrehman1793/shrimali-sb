package com.shrimali.dto;

import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;

public record AuthenticatedIdentity(User user, Member member) {}
