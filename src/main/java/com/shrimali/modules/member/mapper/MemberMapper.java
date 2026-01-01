package com.shrimali.modules.member.mapper;

import com.shrimali.dto.PagedResponse;
import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberAddress;
import com.shrimali.model.member.MemberContact;
import com.shrimali.modules.member.dto.BasicInfoDTO;
import com.shrimali.modules.member.dto.DiscoveryResponse;
import com.shrimali.modules.member.dto.MemberListItem;
import com.shrimali.modules.member.dto.MemberResponse;
import com.shrimali.modules.shared.services.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                .thumbnailUrl(m.getThumbnailUrl())
                .notes(m.getNotes())
                .build();
    }

    public BasicInfoDTO mapToBasicInfo(Member m, User currentUser) {
        return BasicInfoDTO.builder()
                .memberId(m.getId())
                .firstName(m.getFirstName())
                .middleName(m.getMiddleName())
                .lastName(m.getLastName())
                .gender(m.getGender())
                .dob(m.getDob())
                .photoUrl(m.getPhotoUrl())
                .thumbnailUrl(m.getThumbnailUrl())
                .maritalStatus(m.getMaritalStatus())
                .profession(m.getProfession())
                .education(m.getEducation())
                .kuldevi(m.getKuldevi())
                .notes(m.getNotes())
                .membershipNumber(m.getMembershipNumber())
                .gotra(m.getPaternalGotra().getId())
                .spokenLanguages(m.getSpokenLanguages())
                .secondaryProfession(m.getSecondaryProfession())
                .owner(Objects.equals(m.getOwner().getId(), currentUser.getId()))
                .bloodGroup(m.getBloodGroup())
                .paternalVillage(m.getPaternalVillage())
                .naniyalVillage(m.getNaniyalVillage())
                .build();
    }

    public DiscoveryResponse convertToResponse(Member member) {
        return new DiscoveryResponse(true, new DiscoveryResponse.MemberSummary(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getPaternalVillage(),
                member.getPaternalGotra() != null ? member.getPaternalGotra().getName() : null
        ));
    }

    public MemberListItem toListItem(Member m) {
        if (m == null) return null;
        return MemberListItem.builder()
                .id(m.getId())
                .firstName(m.getFirstName())
                .middleName(m.getMiddleName())
                .lastName(m.getLastName())
                .gender(m.getGender().toString())
                .dob(m.getDob())
                .photoUrl(m.getPhotoUrl())
                .thumbnailUrl(m.getThumbnailUrl())
                .notes(m.getNotes())
                .build();
    }

    public PagedResponse<MemberListItem> mapToPagedResponse(Page<Member> page) {
        List<MemberListItem> items = page.getContent().stream()
                .map(this::toListItem)
                .toList();

        return new PagedResponse<>(
                items,
                PageRequest.of(
                        page.getPageable().getPageNumber(),
                        page.getPageable().getPageSize(),
                        page.getSort()),
                page.getTotalElements()
        );
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
