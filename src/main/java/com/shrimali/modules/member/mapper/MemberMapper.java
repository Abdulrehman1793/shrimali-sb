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
import java.util.Collections;
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

    public DiscoveryResponse convertToResponse(List<Member> members) {
        return convertToResponse(members, null);
    }

    public DiscoveryResponse convertToResponse(List<Member> members, Long requestedGotraId) {
        if (members == null || members.isEmpty()) {
            return DiscoveryResponse.builder()
                    .exists(false)
                    .member(Collections.emptyList())
                    .build();
        }

        List<DiscoveryResponse.DiscoveredMemberSummary> summaries = members.stream()
                .map(m -> {
                    // Determine if Gotra matches the user's input
                    boolean isGotraMatch = requestedGotraId != null &&
                            m.getPaternalGotra() != null &&
                            m.getPaternalGotra().getId().equals(requestedGotraId);

                    return DiscoveryResponse.DiscoveredMemberSummary.builder()
                            .id(m.getId())
                            .firstName(m.getFirstName())
                            .middleName(m.getMiddleName()) // Now handled correctly by Lombok
                            .lastName(m.getLastName())
                            .paternalVillage(m.getPaternalVillage())
                            .gotra(m.getPaternalGotra() != null ? m.getPaternalGotra().getName() : null)
                            .matched(isGotraMatch)
                            .build();
                })
                .toList();

        return DiscoveryResponse.builder()
                .exists(true)
                .member(summaries)
                .build();
    }

    public MemberListItem toListItem(Member m) {
        if (m == null) return null;
        MemberListItem listItem = MemberListItem.builder()
                .id(m.getId())
                .firstName(m.getFirstName())
                .middleName(m.getMiddleName())
                .lastName(m.getLastName())
                .gender(m.getGender().toString())
                .dob(m.getDob())
                .photoUrl(m.getPhotoUrl())
                .thumbnailUrl(m.getThumbnailUrl())
                .membershipNumber(m.getMembershipNumber())
                .notes(m.getNotes())
                .email(m.getOwner() != null ? m.getOwner().getEmail() : null)
                .build();


        return listItem;
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
