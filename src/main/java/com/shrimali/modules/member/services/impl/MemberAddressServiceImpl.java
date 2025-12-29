package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.User;
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

    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<MemberAddressPayload> list() {
        User currentUser = securityUtils.getCurrentUser();
        Member member = getMember(currentUser);

        return addressRepository.findByMember(member)
                .stream()
                .map(MemberAddressMapper::toPayload)
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
    @Transactional
    public void remove(String type) {
        User currentUser = securityUtils.getCurrentUser();
        Member member = getMember(currentUser);

        if (type.equalsIgnoreCase("CURRENT")) {
            throw new IllegalStateException("Current address cannot be deleted");
        }

        MemberAddress address = addressRepository
                .findByMemberAndAddressType(member, type)
                .orElseThrow(() -> new EntityNotFoundException("Address type " + type + " not found"));

        member.getAddresses().remove(address);

        address.setMember(null);
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

    private Member getMember(User user) {
        return memberRepository.findById(user.getMemberId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Member not found")
                );
    }
}
