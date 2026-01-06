package com.shrimali.modules.member.services.impl;

import com.shrimali.dto.AuthenticatedIdentity;
import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.Gotra;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.MaritalStatus;
import com.shrimali.model.enums.MembershipStatus;
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
import java.time.Period;
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
    @Transactional
    public MemberProfileResponse getCurrentMemberProfile(String memberShipNumber) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member currentMember = currentIdentity.member();
        User currentUser = currentIdentity.user();

        if (memberShipNumber != null) {
            currentMember = memberRepository.findByMembershipNumber(memberShipNumber)
                    .orElseThrow(() -> new BadRequestException("Member record not found"));
        }

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
    public void updateBasicInfo(String membershipNumber, BasicInfoDTO dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        if (membershipNumber != null && !membershipNumber.equalsIgnoreCase("self")) {
            member = memberRepository.findByMembershipNumber(membershipNumber)
                    .orElseThrow(() -> new BadRequestException("Member record not found"));
        }

        // 1. Perform Age vs Marital Status Validation
        if (dto.getDob() != null && dto.getMaritalStatus() != null) {
            int age = Period.between(dto.getDob(), LocalDate.now()).getYears();

            // Basic check: Cannot be Married, Divorced, or Widowed if under 18
            boolean isMarkedMarried = dto.getMaritalStatus() != MaritalStatus.SINGLE;

            if (age < 18 && isMarkedMarried) {
                throw new BadRequestException("Member must be at least 18 years old to have a marital status other than Single.");
            }
        }

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
    public void updateFatherDetails(MemberPayload dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        Member savedMember;
        if (dto.memberId() != null) {
            savedMember = memberRepository
                    .findById(dto.memberId()).orElseThrow(() -> new BadRequestException("Member not found"));
        } else {
            savedMember = memberRepository.save(
                    Member.builder()
                            .firstName(dto.firstName())
                            .middleName(dto.middleName())
                            .lastName(dto.lastName())
                            .dob(LocalDate.parse(dto.dob()))
                            .owner(currentIdentity.user())
                            .gender(Gender.Male)
                            .maritalStatus(MaritalStatus.MARRIED)
                            .membershipStatus(MembershipStatus.ACTIVE)
                            .paternalVillage(dto.paternalVillage())
                            .naniyalVillage(dto.naniyalVillage())
                            .paternalGotra(member.getPaternalGotra())
                            .deceased(dto.deceased() != null ? dto.deceased() : false)
                            .build());
        }

        member.setFather(savedMember);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMotherDetails(MemberPayload dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        Member savedMember;
        if (dto.memberId() != null) {
            savedMember = memberRepository
                    .findById(dto.memberId()).orElseThrow(() -> new BadRequestException("Member not found"));
        } else {
            savedMember = memberRepository.save(
                    Member.builder()
                            .firstName(dto.firstName())
                            .middleName(dto.middleName())
                            .lastName(dto.lastName())
                            .dob(LocalDate.parse(dto.dob()))
                            .owner(currentIdentity.user())
                            .gender(Gender.Female)
                            .maritalStatus(MaritalStatus.MARRIED)
                            .membershipStatus(MembershipStatus.ACTIVE)
                            .paternalVillage(dto.paternalVillage())
                            .naniyalVillage(dto.naniyalVillage())
                            .paternalGotra(member.getPaternalGotra())
                            .deceased(dto.deceased() != null ? dto.deceased() : false)
                            .build());
        }

        member.setMother(savedMember);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateSpouseDetails(MemberPayload dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member member = currentIdentity.member();

        Member savedMember;
        if (dto.memberId() != null) {
            savedMember = memberRepository
                    .findById(dto.memberId()).orElseThrow(() -> new BadRequestException("Member not found"));
        } else {
            Member newMember = Member.builder()
                    .firstName(dto.firstName())
                    .middleName(dto.middleName())
                    .lastName(dto.lastName())
                    .dob(LocalDate.parse(dto.dob()))
                    .owner(currentIdentity.user())
                    .maritalStatus(MaritalStatus.MARRIED)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .paternalVillage(dto.paternalVillage())
                    .naniyalVillage(dto.naniyalVillage())
                    .paternalGotra(member.getPaternalGotra())
                    .deceased(dto.deceased() != null ? dto.deceased() : false)
                    .build();

            if (member.getGender() == Gender.Female)
                newMember.setGender(Gender.Male);
            else
                newMember.setGender(Gender.Female);

            savedMember = memberRepository.save(newMember);
        }

        member.setSpouse(savedMember);
        memberRepository.save(member);
    }

    @Override
    public void addOrUpdateContact(ContactPayload payload) {

    }
}
