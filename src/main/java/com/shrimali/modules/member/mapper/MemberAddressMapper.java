package com.shrimali.modules.member.mapper;

import com.shrimali.model.member.MemberAddress;
import com.shrimali.modules.member.dto.MemberAddressPayload;

public class MemberAddressMapper {
    public static MemberAddressPayload toPayload(MemberAddress address) {
        return MemberAddressPayload.builder()
                .addressType(address.getAddressType())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .areaLocality(address.getAreaLocality())
                .city(address.getCity())
                .district(address.getDistrict())
                .state(address.getState())
                .country(address.getCountry())
                .pincode(address.getPincode())
                .build();
    }
}
