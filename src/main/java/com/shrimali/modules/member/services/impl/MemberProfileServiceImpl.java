package com.shrimali.modules.member.services.impl;

import com.shrimali.dto.AuthenticatedIdentity;
import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.Gotra;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.Gender;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberGotra;
import com.shrimali.modules.member.dto.*;
import com.shrimali.modules.member.mapper.MemberAddressMapper;
import com.shrimali.modules.member.mapper.MemberMapper;
import com.shrimali.modules.member.services.MemberCommonService;
import com.shrimali.modules.member.services.MemberProfileService;
import com.shrimali.modules.shared.services.AppUtils;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.GotraRepository;
import com.shrimali.repositories.MemberGotraRepository;
import com.shrimali.repositories.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberProfileServiceImpl implements MemberProfileService {

    private final SecurityUtils securityUtils;
    private final MemberCommonService commonService;

    private final GotraRepository gotraRepository;
    private final MemberRepository memberRepository;
    private final MemberGotraRepository memberGotraRepository;

    private final MemberMapper memberMapper;

    @Override
    public MemberResponse me() {
        return memberMapper.toDto(securityUtils.getCurrentMember());
    }

    @Override
    public MemberProfileResponse getCurrentMemberProfile() {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member currentMember = currentIdentity.member();
        User currentUser = currentIdentity.user();

        String gotra = "";
        List<MemberGotra> gotras = memberGotraRepository.findByMemberId(currentMember.getId());
        if (!gotras.isEmpty()) {
            gotra = gotras.getFirst().getGotra().getName();
        }

        MemberProfileResponse memberProfileResponse = MemberProfileResponse.builder()
                .basicInfo(memberMapper.mapToBasicInfo(currentMember, currentUser))
                .build();
        if (currentMember.getFather() != null)
            memberProfileResponse.setFather(memberMapper.mapToBasicInfo(currentMember.getFather(), currentUser));

        if (currentMember.getMother() != null)
            memberProfileResponse.setMother(memberMapper.mapToBasicInfo(currentMember.getMother(), currentUser));

        if (currentMember.getSpouse() != null)
            memberProfileResponse.setSpouse(memberMapper.mapToBasicInfo(currentMember.getSpouse(), currentUser));

        if (currentMember.getAddresses() != null) {
            memberProfileResponse.setAddresses(
                    currentMember.getAddresses().stream()
                            .map(MemberAddressMapper::toPayload)
                            .toList()
            );
        }

        if (currentMember.getContacts() != null) {
            memberProfileResponse.setContacts(
                    currentMember.getContacts().stream()
                            .map(c ->
                                    new ContactPayload(c.getId(), c.getType(), c.getIsPrimary(), c.getValue())
                            )
                            .toList()
            );
        }

        memberProfileResponse.setMetadata(
                ProfileMetadataDTO.builder()
                        .membershipStatus(currentMember.getMembershipStatus() == null ? null : currentMember.getMembershipStatus())
                        .completionPercentage(AppUtils.calculateCompletion(currentMember))
                        .gotra(gotra)
                        .build()
        );
        return memberProfileResponse;
    }

    @Override
    @Transactional
    public void updateBasicInfo(BasicInfoDTO dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        member.setDob(dto.getDob());
        member.setFirstName(dto.getFirstName());
        member.setMiddleName(dto.getMiddleName());
        member.setLastName(dto.getLastName());
        member.setGender(dto.getGender());
        member.setMaritalStatus(dto.getMaritalStatus());
        member.setProfession(dto.getProfession());
        member.setEducation(dto.getEducation());
        member.setNotes(dto.getNotes());
        member.setKuldevi(dto.getKuldevi());
        member.setSpokenLanguages(dto.getSpokenLanguages());
        member.setSecondaryProfession(dto.getSecondaryProfession());
        member.setBloodGroup(dto.getBloodGroup());
        member.setPaternalVillage(dto.getPaternalVillage());
        member.setNaniyalVillage(dto.getNaniyalVillage());

        if (dto.getGotra() != null) {
            Gotra gotra = gotraRepository.findById(dto.getGotra())
                    .orElseThrow(() -> new BadRequestException("Gotra not found"));

            member.setPaternalGotra(gotra);
        }

        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateFatherDetails(DiscoverySearchRequest dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        Member newMember = Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(LocalDate.parse(dto.dob()))
                .owner(currentIdentity.user())
                .gender(Gender.Male)
                .maritalStatus("married")
                .membershipStatus("APPROVED")
                .paternalVillage(dto.paternalVillage())
                .naniyalVillage(dto.naniyalVillage())
                .paternalGotra(member.getPaternalGotra())
                .deceased(dto.deceased() != null ? dto.deceased() : false)
                .build();

        Member savedNewMember = memberRepository.save(newMember);

        member.setFather(savedNewMember);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMotherDetails(DiscoverySearchRequest dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        Member newMember = Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(LocalDate.parse(dto.dob()))
                .owner(currentIdentity.user())
                .gender(Gender.Female)
                .maritalStatus("married")
                .membershipStatus("APPROVED")
                .paternalVillage(dto.paternalVillage())
                .naniyalVillage(dto.naniyalVillage())
                .paternalGotra(member.getPaternalGotra())
                .deceased(dto.deceased() != null ? dto.deceased() : false)
                .build();

        Member savedNewMember = memberRepository.save(newMember);

        member.setMother(savedNewMember);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateSpouseDetails(DiscoverySearchRequest dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        Member newMember = Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(LocalDate.parse(dto.dob()))
                .owner(currentIdentity.user())
                .maritalStatus("married")
                .membershipStatus("APPROVED")
                .paternalVillage(dto.paternalVillage())
                .naniyalVillage(dto.naniyalVillage())
                .paternalGotra(member.getPaternalGotra())
                .deceased(dto.deceased() != null ? dto.deceased() : false)
                .build();

        if (member.getGender() == Gender.Female)
            newMember.setGender(Gender.Male);
        else
            newMember.setGender(Gender.Female);

        Member savedNewMember = memberRepository.save(newMember);

        member.setSpouse(savedNewMember);
        memberRepository.save(member);
    }

    @Override
    public void addOrUpdateContact(ContactPayload payload) {

    }
}
