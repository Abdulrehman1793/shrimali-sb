package com.shrimali.modules.member.services.impl;

import com.shrimali.dto.PagedResponse;
import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.Gotra;
import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.auth.UserRole;
import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.RoleName;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberGotra;
import com.shrimali.modules.member.dto.*;
import com.shrimali.modules.member.mapper.MemberAddressMapper;
import com.shrimali.modules.member.mapper.MemberMapper;
import com.shrimali.modules.member.services.MemberService;
import com.shrimali.modules.shared.services.AppUtils;
import com.shrimali.modules.shared.services.EmailService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberGotraRepository memberGotraRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final EmailService emailService;
    private final MemberMapper memberMapper;
    private final GotraRepository gotraRepository;
    private final SecurityUtils securityUtils;

    @Override
    public PagedResponse<MemberListItem> findAll(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();

        Page<MemberListItem> page = memberRepository.searchListItems(query, pageable, List.of(RoleName.ROLE_GUEST, RoleName.ROLE_USER));

        return new PagedResponse<>(
                page.toList(),
                PageRequest.of(
                        page.getPageable().getPageNumber(),
                        page.getPageable().getPageSize(),
                        page.getSort()),
                page.getTotalElements()
        );
    }

    @Override
    public PagedResponse<MemberListItem> listMembers(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<MemberListItem> page = memberRepository.searchListItems(query, pageable, List.of(RoleName.ROLE_USER, RoleName.ROLE_ADMIN));

        return new PagedResponse<>(
                page.toList(),
                PageRequest.of(
                        page.getPageable().getPageNumber(),
                        page.getPageable().getPageSize(),
                        page.getSort()),
                page.getTotalElements()
        );
    }

    @Override
    public PagedResponse<MemberListItem> guests(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<MemberListItem> page = memberRepository.searchListItems(query, pageable, List.of(RoleName.ROLE_GUEST));

        return new PagedResponse<>(
                page.toList(),
                PageRequest.of(
                        page.getPageable().getPageNumber(),
                        page.getPageable().getPageSize(),
                        page.getSort()),
                page.getTotalElements()
        );
    }

    @Override
    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Member not found with id: " + id));
    }

    @Transactional()
    @Override
    public MemberResponse me(Principal principal) {
        assert ((UsernamePasswordAuthenticationToken) principal).getPrincipal() != null;
        User userPrincipal = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long memberId = userPrincipal.getMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found with id: " + memberId));

        return memberMapper.toDto(member);
    }

    @Transactional
    @Override
    public MemberResponse completeProfile(Principal principal, MemberPayload payload) {
        User currentUser = getCurrentUser(principal);
        Long memberId = currentUser.getMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));

        member.setFirstName(payload.getFirstName());
        member.setMiddleName(payload.getMiddleName());
        member.setLastName(payload.getLastName());
        member.setGender(payload.getGender());

        if (payload.getDob() != null && !payload.getDob().isBlank()) {
            try {
                member.setDob(LocalDate.parse(payload.getDob()));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(
                        "Invalid date format for dob. Expected yyyy-MM-dd"
                );
            }
        }

        Member savedMember = memberRepository.save(member);

        emailService.sendWelcomeEmail(currentUser.getEmail());

        return MemberResponse.builder()
                .firstName(savedMember.getFirstName())
                .middleName(savedMember.getMiddleName())
                .lastName(savedMember.getLastName())
                .dob(savedMember.getDob() != null ? savedMember.getDob().toString() : null)
                .gender(savedMember.getGender())
                .photoUrl(savedMember.getPhotoUrl())
                .completed(true)
                .build();
    }

    @Override
    @Transactional
    public void updateUserProfile(Principal principal, UserProfile profile) {

        User currentUser = getCurrentUser(principal);
        Long memberId = currentUser.getMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));

        if (profile.getDob() != null) {
            member.setDob(profile.getDob());
        }

        member.setFirstName(profile.getFirstName());
        member.setMiddleName(profile.getMiddleName());
        member.setLastName(profile.getLastName());
        member.setGender(profile.getGender());
        member.setMaritalStatus(profile.getMaritalStatus());
        member.setProfession(profile.getProfession());
        member.setEducation(profile.getEducation());
        member.setNotes(profile.getNotes());

        memberRepository.save(member);
    }

    @Transactional
    @Override
    public void approveGuestUser(Principal principal, Long memberId) {

        User approvedBy = getCurrentUser(principal);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));

        User guestUser = userRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found for member id: " + memberId));

        if (!hasRole(guestUser, RoleName.ROLE_GUEST)) {
            throw new IllegalStateException("Only guest users can be approved");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER not found"));

        // Role transition
        removeRole(guestUser, RoleName.ROLE_GUEST);
        addRoleIfMissing(guestUser, userRole);

        // Member update
        member.setApprovedBy(approvedBy);
        member.setApprovedAt(OffsetDateTime.now());
        member.setMembershipStatus("APPROVED");

        userRepository.save(guestUser);
        memberRepository.save(member);
    }

    @Transactional
    @Override
    public void updateMemberAccess(Principal principal, MemberAccessPayload payload) {

        User currentUser = getCurrentUser(principal);
        Long memberId = payload.memberId();

        if (Boolean.TRUE.equals(payload.approved()) == Boolean.TRUE.equals(payload.remove())) {
            throw new IllegalArgumentException("Exactly one action must be specified");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));

        User targetUser = userRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found for member id: " + memberId));

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You cannot modify your own role");
        }

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_ADMIN not found"));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER not found"));

        boolean isAdmin = hasRole(targetUser, RoleName.ROLE_ADMIN);

    /* ============================
       ASSIGN ADMIN
       ============================ */
        if (Boolean.TRUE.equals(payload.approved())) {

            if (isAdmin) {
                throw new IllegalStateException("User already has admin privileges");
            }

            removeRole(targetUser, RoleName.ROLE_USER);
            addRoleIfMissing(targetUser, adminRole);
        }

    /* ============================
       REVOKE ADMIN → USER
       ============================ */
        if (Boolean.TRUE.equals(payload.remove())) {

            if (!isAdmin) {
                throw new IllegalStateException("User is not an admin");
            }

            removeRole(targetUser, RoleName.ROLE_ADMIN);
            addRoleIfMissing(targetUser, userRole);
        }

        userRepository.save(targetUser);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeUserFromCommunity(Principal principal, Long memberId) {
        User actionBy = getCurrentUser(principal);

        // 1️⃣ Fetch member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));

        // 2️⃣ Fetch linked user
        User targetUser = userRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found for member id: " + memberId));

        // 3️⃣ Validate community membership
        boolean isCommunityUser =
                hasRole(targetUser, RoleName.ROLE_USER) ||
                        hasRole(targetUser, RoleName.ROLE_GUEST);

        if (!isCommunityUser) {
            throw new IllegalStateException("User does not belong to the community");
        }

        // 4️⃣ Fetch OUTSIDER role
        Role outsiderRole = roleRepository.findByName(RoleName.ROLE_OUTSIDER)
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_OUTSIDER not found"));

        // 5️⃣ Remove community roles
        removeRole(targetUser, RoleName.ROLE_USER);
        removeRole(targetUser, RoleName.ROLE_GUEST);

        // 6️⃣ Assign OUTSIDER role if missing
        addRoleIfMissing(targetUser, outsiderRole);

        // 7️⃣ Update member status
        member.setApprovedBy(actionBy);
        member.setMembershipStatus("REJECTED");
        // member.setApprovedAt(OffsetDateTime.now()); // optional audit

        // 8️⃣ Persist
        userRepository.save(targetUser);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public MemberProfileResponse getCurrentMemberProfile(Principal principal) {
        Member member = getMember(principal);

        String gotra = "";
        List<MemberGotra> gotras = memberGotraRepository.findByMemberId(member.getId());
        if (!gotras.isEmpty()) {
            gotra = gotras.getFirst().getGotra().getName();
        }

        MemberProfileResponse memberProfileResponse = MemberProfileResponse.builder()
                .basicInfo(mapToBasicInfo(member))
                .build();
        if (member.getFather() != null)
            memberProfileResponse.setFather(mapToBasicInfo(member.getFather()));

        if (member.getMother() != null)
            memberProfileResponse.setMother(mapToBasicInfo(member.getMother()));

        if (member.getSpouse() != null)
            memberProfileResponse.setSpouse(mapToBasicInfo(member.getSpouse()));

        if (member.getAddresses() != null) {
            memberProfileResponse.setAddresses(
                    member.getAddresses().stream()
                            .map(MemberAddressMapper::toPayload)
                            .toList()
            );
        }

        if (member.getContacts() != null) {
            memberProfileResponse.setContacts(
                    member.getContacts().stream()
                            .map(c ->
                                    new ContactPayload(c.getType(), c.getIsPrimary(), c.getValue())
                            )
                            .toList()
            );
        }

        memberProfileResponse.setMetadata(
                ProfileMetadataDTO.builder()
                        .membershipStatus(member.getMembershipStatus() == null ? null : member.getMembershipStatus())
                        .completionPercentage(AppUtils.calculateCompletion(member))
                        .gotra(gotra)
                        .build()
        );
        return memberProfileResponse;
    }

    @Override
    @Transactional
    public void updateBasicInfo(Principal principal, BasicInfoDTO basicInfo) {
        Member member = getMember(principal);

        Gotra gotra = gotraRepository.findById(basicInfo.getGotra())
                .orElseThrow(() -> new BadRequestException("Gotra not found"));


        member.setDob(basicInfo.getDob());
        member.setFirstName(basicInfo.getFirstName());
        member.setMiddleName(basicInfo.getMiddleName());
        member.setLastName(basicInfo.getLastName());
        member.setGender(basicInfo.getGender());
        member.setMaritalStatus(basicInfo.getMaritalStatus());
        member.setProfession(basicInfo.getProfession());
        member.setEducation(basicInfo.getEducation());
        member.setNotes(basicInfo.getNotes());
        member.setKuldevi(basicInfo.getKuldevi());
        member.setPaternalGotra(gotra);
        member.setSpokenLanguages(basicInfo.getSpokenLanguages());
        member.setSecondaryProfession(basicInfo.getSecondaryProfession());
        member.setBloodGroup(basicInfo.getBloodGroup());
        member.setPaternalVillage(basicInfo.getPaternalVillage());
        member.setNaniyalVillage(basicInfo.getNaniyalVillage());

        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateFatherDetails(DiscoverySearchRequest dto) {
        User currentUser = securityUtils.getCurrentUser();

        Member member = getMember(currentUser);

        Member newMember = Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(LocalDate.parse(dto.dob()))
                .owner(currentUser)
                .gender(Gender.Male)
                .maritalStatus("married")
                .membershipStatus("PENDING_APPROVAL")
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
    public void updateSpouseDetails(DiscoverySearchRequest dto) {
        User currentUser = securityUtils.getCurrentUser();

        Member member = getMember(currentUser);

        Member newMember = Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(LocalDate.parse(dto.dob()))
                .owner(currentUser)
                .gender(Gender.Female)
                .maritalStatus("married")
                .membershipStatus("PENDING_APPROVAL")
                .paternalVillage(dto.paternalVillage())
                .naniyalVillage(dto.naniyalVillage())
                .paternalGotra(member.getPaternalGotra())
                .deceased(dto.deceased() != null ? dto.deceased() : false)
                .build();

        Member savedNewMember = memberRepository.save(newMember);

        member.setSpouse(savedNewMember);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMotherDetails(DiscoverySearchRequest dto) {
        User currentUser = securityUtils.getCurrentUser();

        Member member = getMember(currentUser);

        Member newMember = Member.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .dob(LocalDate.parse(dto.dob()))
                .owner(currentUser)
                .gender(Gender.Female)
                .maritalStatus("married")
                .membershipStatus("PENDING_APPROVAL")
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
    public DiscoveryResponse discoverExistingMember(Principal principal, DiscoverySearchRequest request) {
        // 1. Convert gender string to Enum
        Gender searchGender = Gender.fromString(request.gender());

        // 2. Parse Date
        LocalDate birthDate = LocalDate.parse(request.dob());

        // 3. Execute Search
        return memberRepository.findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDobAndGender(
                        request.firstName(),
                        request.lastName(),
                        birthDate,
                        searchGender
                )
                .map(this::convertToResponse)
                .orElse(new DiscoveryResponse(false, null));
    }

    private DiscoveryResponse convertToResponse(Member member) {
        return new DiscoveryResponse(true, new DiscoveryResponse.MemberSummary(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getPaternalVillage(),
                member.getPaternalGotra() != null ? member.getPaternalGotra().getName() : null
        ));
    }

    private User getUser(Principal principal) {
        return (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }

    private Member getMember(Principal principal) {
        User currentUser = getCurrentUser(principal);
        Long memberId = currentUser.getMemberId();

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));
    }

    private Member getMember(User currentUser) {
        Long memberId = currentUser.getMemberId();

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));
    }

    private User getCurrentUser(Principal principal) {
        return (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == roleName);
    }

    private void removeRole(User user, RoleName roleName) {
        user.getUserRoles().removeIf(
                ur -> ur.getRole().getName() == roleName
        );
    }

    private void addRoleIfMissing(User user, Role role) {
        boolean exists = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == role.getName());

        if (!exists) {
            UserRole userRole = new UserRole();
            userRole.setRole(role);
            user.getUserRoles().add(userRole);
        }
    }

    private MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .firstName(member.getFirstName())
                .middleName(member.getMiddleName())
                .lastName(member.getLastName())
                .dob(member.getDob() != null ? member.getDob().toString() : null)
                .gender(member.getGender())
                .photoUrl(member.getPhotoUrl())
                .completed(true)

                .build();
    }

    // --- Mapper Methods ---

    private BasicInfoDTO mapToBasicInfo(Member m) {
        return BasicInfoDTO.builder()
                .memberId(m.getId())
                .firstName(m.getFirstName())
                .middleName(m.getMiddleName())
                .lastName(m.getLastName())
                .gender(m.getGender())
                .dob(m.getDob())
                .photoUrl(m.getPhotoUrl())
                .maritalStatus(m.getMaritalStatus())
                .profession(m.getProfession())
                .education(m.getEducation())
                .kuldevi(m.getKuldevi())
                .notes(m.getNotes())
                .membershipNumber(m.getMembershipNumber())
                .gotra(m.getPaternalGotra().getId())
                .spokenLanguages(m.getSpokenLanguages())
                .secondaryProfession(m.getSecondaryProfession())
                .owner(Objects.equals(m.getOwner().getId(), securityUtils.getCurrentUser().getId()))
                .bloodGroup(m.getBloodGroup())
                .paternalVillage(m.getPaternalVillage())
                .naniyalVillage(m.getNaniyalVillage())
                .build();
    }

    private MotherDetailsDTO mapToMother(Member m) {
        MotherDetailsDTO dto = new MotherDetailsDTO();
        dto.setNaniyalVillage(m.getNaniyalVillage());
        return dto;
    }

    private SpouseDetailsDTO mapToSpouse(Member m) {
        SpouseDetailsDTO dto = new SpouseDetailsDTO();
        dto.setMarriageDate(m.getMarriageDate());
        return dto;
    }
}
