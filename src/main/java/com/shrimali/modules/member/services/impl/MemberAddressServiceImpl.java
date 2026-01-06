package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberAddress;
import com.shrimali.modules.member.dto.MemberAddressPayload;
import com.shrimali.modules.member.mapper.MemberAddressMapper;
import com.shrimali.modules.member.services.MemberAddressService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.MemberAddressRepository;
import com.shrimali.repositories.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberAddressServiceImpl implements MemberAddressService {
    private final MemberRepository memberRepository;
    private final MemberAddressRepository addressRepository;

    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<MemberAddressPayload> list(String membershipNumber) {
        Member member = securityUtils.getCurrentMember();

        return addressRepository.findByMember(member)
                .stream()
                .map(MemberAddressMapper::toPayload)
                .toList();
    }

    @Override
    public void add(String membershipNumber, MemberAddressPayload payload) {
        Member member = securityUtils.getCurrentMember();

        Member targetMember = resolveTargetMember(membershipNumber, member);

        boolean exists = addressRepository
                .findByMemberAndAddressType(targetMember, payload.getAddressType())
                .isPresent();

        if (exists) {
            throw new BadRequestException(
                    "Address of type " + payload.getAddressType() + " already exists"
            );
        }

        MemberAddress address = MemberAddress.builder()
                .member(targetMember)
                .addressType(payload.getAddressType())
                .line1(payload.getLine1())
                .line2(payload.getLine2())
                .areaLocality(payload.getAreaLocality())
                .city(payload.getCity())
                .district(payload.getDistrict())
                .state(payload.getState())
                .country(payload.getCountry())
                .pincode(payload.getPincode())
                .build();

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void update(String membershipNumber, MemberAddressPayload payload) {
        Member member = securityUtils.getCurrentMember();

        Member targetMember = resolveTargetMember(membershipNumber, member);

        MemberAddress address = addressRepository
                .findByMemberAndAddressType(targetMember, payload.getAddressType())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Address of type " + payload.getAddressType() + " not found"
                        )
                );

        address.setLine1(payload.getLine1());
        address.setLine2(payload.getLine2());
        address.setAreaLocality(payload.getAreaLocality());
        address.setCity(payload.getCity());
        address.setDistrict(payload.getDistrict());
        address.setState(payload.getState());
        address.setCountry(payload.getCountry());
        address.setPincode(payload.getPincode());
    }

    @Override
    @Transactional
    public void remove(String membershipNumber, String type) {
        Member member = securityUtils.getCurrentMember();

        Member targetMember = resolveTargetMember(membershipNumber, member);

        if (type.equalsIgnoreCase("CURRENT")) {
            throw new IllegalStateException("Current address cannot be deleted");
        }

        MemberAddress address = addressRepository
                .findByMemberAndAddressType(targetMember, type)
                .orElseThrow(() -> new EntityNotFoundException("Address type " + type + " not found"));

        targetMember.getAddresses().remove(address);

        address.setMember(null);
    }

    /* -------------------- HELPERS -------------------- */

    private Member resolveTargetMember(String membershipNumber, Member currentUserMember) {
        if (membershipNumber == null || membershipNumber.equalsIgnoreCase("self")) {
            return currentUserMember;
        }
        return memberRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new BadRequestException("Target member record not found"));
    }
}
