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
    public void updateFatherDetails(String membershipNumber, MemberPayload dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member currentUserMember = currentIdentity.member();

        // 1. Identify which member we are updating (Self or Managed Member)
        Member targetMember = resolveTargetMember(membershipNumber, currentUserMember);

        // 2. Resolve or Create the Father entity
        Member fatherEntity = resolveOrCreateFather(dto, currentIdentity);

        // 3. Prevent Circular Reference (A member cannot be their own father)
        if (fatherEntity.getId() != null && fatherEntity.getId().equals(targetMember.getId())) {
            throw new BadRequestException("A member cannot be assigned as their own father.");
        }

        if (fatherEntity.getId() == null) {
            fatherEntity = memberRepository.save(fatherEntity);
        }

        // 4. Establish the relationship and save
        targetMember.setFather(fatherEntity);
        memberRepository.save(targetMember);
    }

    @Override
    @Transactional
    public void updateMotherDetails(String membershipNumber, MemberPayload dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member currentUserMember = currentIdentity.member();

        // 1. Resolve which member is being updated (Self or Managed Member)
        Member targetMember = resolveTargetMember(membershipNumber, currentUserMember);

        // 2. Resolve or Create the Mother entity
        Member motherEntity = resolveOrCreateMother(dto, currentIdentity);

        // 3. Fix the Transient Error: Save motherEntity if it's new
        if (motherEntity.getId() == null) {
            motherEntity = memberRepository.save(motherEntity);
        }

        // 4. Prevent Circular Reference
        if (motherEntity.getId() != null && motherEntity.getId().equals(targetMember.getId())) {
            throw new BadRequestException("A member cannot be assigned as their own mother.");
        }

        // 5. Establish relationship and save the target member
        targetMember.setMother(motherEntity);
        memberRepository.save(targetMember);
    }

    @Override
    @Transactional
    public void updateSpouseDetails(String membershipNumber, MemberPayload dto) {
        AuthenticatedIdentity currentIdentity = securityUtils.getCurrentIdentity();
        Member currentUserMember = currentIdentity.member();

        // 1. Resolve which member is being updated (Self or Managed Member)
        Member targetMember = resolveTargetMember(membershipNumber, currentUserMember);

        // 2. Resolve or Create the Spouse entity
        Member spouseEntity = resolveOrCreateSpouse(dto, targetMember, currentIdentity);

        // 3. Fix Transient Error: Save spouseEntity if it's new
        if (spouseEntity.getId() == null) {
            spouseEntity = memberRepository.save(spouseEntity);
        }

        // 4. Prevent Circular Reference (Cannot marry yourself)
        if (spouseEntity.getId() != null && spouseEntity.getId().equals(targetMember.getId())) {
            throw new BadRequestException("A member cannot be assigned as their own spouse.");
        }

        // 5. Establish Bidirectional Relationship
        targetMember.setSpouse(spouseEntity);
        targetMember.setMaritalStatus(MaritalStatus.MARRIED);

        // Ensure the spouse also points back to the target member
        spouseEntity.setSpouse(targetMember);
        spouseEntity.setMaritalStatus(MaritalStatus.MARRIED);

        // 6. Save both sides of the relationship
        memberRepository.save(spouseEntity);
        memberRepository.save(targetMember);
    }

    @Override
    public void addOrUpdateContact(ContactPayload payload) {

    }

    private Member resolveTargetMember(String membershipNumber, Member currentUserMember) {
        if (membershipNumber == null || membershipNumber.equalsIgnoreCase("self")) {
            return currentUserMember;
        }
        return memberRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new BadRequestException("Target member record not found"));
    }

    private Member resolveOrCreateFather(MemberPayload dto, AuthenticatedIdentity identity) {
        if (dto.memberId() != null) {
            return memberRepository.findById(dto.memberId())
                    .orElseThrow(() -> new BadRequestException("Specified father record not found"));
        }

        // Create a new Father record if not linked to an existing member
        return Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(dto.dob() != null ? LocalDate.parse(dto.dob()) : null)
                .owner(identity.user())
                .gender(Gender.Male)
                .maritalStatus(MaritalStatus.MARRIED)
                .membershipStatus(MembershipStatus.ACTIVE)
                .paternalVillage(dto.paternalVillage())
                .naniyalVillage(dto.naniyalVillage())
                // Note: Father's paternal gotra is the same as the son's
                .paternalGotra(identity.member().getPaternalGotra())
                .deceased(Boolean.TRUE.equals(dto.deceased()))
                .build();
    }

    private Member resolveOrCreateMother(MemberPayload dto, AuthenticatedIdentity identity) {
        if (dto.memberId() != null) {
            return memberRepository.findById(dto.memberId())
                    .orElseThrow(() -> new BadRequestException("Specified mother record not found"));
        }

        // Create a new Mother record
        return Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(dto.dob() != null ? LocalDate.parse(dto.dob()) : null)
                .owner(identity.user())
                .gender(Gender.Female)
                .maritalStatus(MaritalStatus.MARRIED)
                .membershipStatus(MembershipStatus.ACTIVE)
                .paternalVillage(dto.paternalVillage())
                .naniyalVillage(dto.naniyalVillage())
                // Cultural logic: Mother's paternal gotra comes from the DTO/Form,
                // not the son's paternal gotra.
                .paternalGotra(getGotra(dto.gotra()))
                .deceased(Boolean.TRUE.equals(dto.deceased()))
                .build();
    }

    private Member resolveOrCreateSpouse(MemberPayload dto, Member targetMember, AuthenticatedIdentity identity) {
        if (dto.memberId() != null) {
            return memberRepository.findById(dto.memberId())
                    .orElseThrow(() -> new BadRequestException("Specified spouse record not found"));
        }

        // Determine Gender automatically based on the target member
        Gender spouseGender = (targetMember.getGender() == Gender.Female) ? Gender.Male : Gender.Female;

        return Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(dto.dob() != null ? LocalDate.parse(dto.dob()) : null)
                .owner(identity.user())
                .gender(spouseGender)
                .maritalStatus(MaritalStatus.MARRIED)
                .membershipStatus(MembershipStatus.ACTIVE)
                .paternalVillage(dto.paternalVillage())
                .naniyalVillage(dto.naniyalVillage())
                // Cultural Logic: Paternal Gotra of spouse should come from the Form (paternalGotra)
                .paternalGotra(getGotra(dto.gotra()))
                .deceased(Boolean.TRUE.equals(dto.deceased()))
                .build();
    }

    private Gotra getGotra(Long gotraId) {
        if (gotraId == null) {
            return null;
        }
        return gotraRepository.findById(gotraId)
                .orElseThrow(() -> new BadRequestException("Gotra record not found"));
    }
}
