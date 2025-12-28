package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberAddress;
import com.shrimali.modules.member.dto.MemberAddressPayload;
import com.shrimali.modules.member.services.MemberAddressService;
import com.shrimali.repositories.MemberAddressRepository;
import com.shrimali.repositories.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberAddressServiceImpl implements MemberAddressService {
    private final MemberRepository memberRepository;
    private final MemberAddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MemberAddressPayload> list(Principal principal) {
        Member member = getMember(principal);

        return addressRepository.findByMember(member)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    @Override
    public void add(Principal principal, MemberAddressPayload payload) {
        Member member = getMember(principal);

        boolean exists = addressRepository
                .findByMemberAndAddressType(member, payload.getAddressType())
                .isPresent();

        if (exists) {
            throw new BadRequestException(
                    "Address of type " + payload.getAddressType() + " already exists"
            );
        }

        MemberAddress address = MemberAddress.builder()
                .member(member)
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
    public void update(Principal principal, MemberAddressPayload payload) {
        Member member = getMember(principal);

        MemberAddress address = addressRepository
                .findByMemberAndAddressType(member, payload.getAddressType())
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
    public void remove(Principal principal, String type) {
        Member member = getMember(principal);

        if (type.equalsIgnoreCase("CURRENT")) {
            throw new IllegalStateException("Current address cannot be deleted");
        }

        MemberAddress address = addressRepository
                .findByMemberAndAddressType(member, type)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Address of type " + type + " not found"
                        )
                );

        addressRepository.delete(address);
    }

    /* -------------------- HELPERS -------------------- */

    private Member getMember(Principal principal) {
        User userPrincipal = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        assert userPrincipal != null;
        Long memberId = userPrincipal.getMemberId();

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Member not found")
                );
    }

    private MemberAddressPayload toPayload(MemberAddress address) {
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
