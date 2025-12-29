package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.MemberAddressPayload;

import java.security.Principal;
import java.util.List;

public interface MemberAddressService {
    List<MemberAddressPayload> list();

    void add(Principal principal, MemberAddressPayload payload);

    void update(Principal principal, MemberAddressPayload payload);

    void remove(String type);
}
