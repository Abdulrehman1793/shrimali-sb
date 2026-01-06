package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.MemberAddressPayload;

import java.security.Principal;
import java.util.List;

public interface MemberAddressService {
    List<MemberAddressPayload> list(String membershipNumber);

    void add(String membershipNumber, MemberAddressPayload payload);

    void update(String membershipNumber, MemberAddressPayload payload);

    void remove(String membershipNumber, String type);
}
