package com.shrimali.modules.member.mapper;

import com.shrimali.model.Member;
import com.shrimali.model.MemberAddress;
import com.shrimali.modules.member.dto.MemberResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MemberMapper {
    public MemberResponse toDto(Member m) {
        if (m == null) return null;
        return MemberResponse.builder()
                .id(m.getId())
                .membershipNumber(m.getMembershipNumber())
                .firstName(m.getFirstName())
                .middleName(m.getMiddleName())
                .lastName(m.getLastName())
                .gender(m.getGender())
                .dob(m.getDob() != null ? m.getDob().toString() : null)
                .membershipType(m.getMembershipType())
                .membershipStatus(m.getMembershipStatus())
                .education((m.getEducation() != null) ? m.getEducation() : null)
                .profession(m.getProfession())
                .maritalStatus(m.getMaritalStatus())
//                .city(extractCityFromAddresses(m)) // optional helper
                .photoUrl(m.getPhotoUrl())
                .notes(m.getNotes())
                .build();
    }

    private String extractCityFromAddresses(Member m) {
        if (m.getAddresses() == null) return null;

        return new ArrayList<>(m.getAddresses()).stream()
                .map(MemberAddress::getCity)
                .filter(city -> city != null && !city.isBlank())
                .findFirst()
                .orElse(null);
    }
}
