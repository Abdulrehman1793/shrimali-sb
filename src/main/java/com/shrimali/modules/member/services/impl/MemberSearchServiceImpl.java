package com.shrimali.modules.member.services.impl;

import com.shrimali.dto.PagedResponse;
import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.MemberStatus;
import com.shrimali.model.enums.MembershipStatus;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberContact;
import com.shrimali.modules.member.dto.*;
import com.shrimali.modules.member.mapper.MemberMapper;
import com.shrimali.modules.member.services.MemberSearchService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.MemberRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberSearchServiceImpl implements MemberSearchService {

    private final MemberRepository memberRepository;

    private final SecurityUtils securityUtils;
    private final MemberMapper memberMapper;

    @Override
    public PagedResponse<MemberListItem> findPendingMembers(String query, Pageable pageable) {
        return null;
    }

    @Override
    public PagedResponse<MemberListItem> listMembers(MemberFilterRequest filters, Pageable pageable) {
        // 1. Check if status is provided; default to APPROVED if null for safety
        String statusStr = filters.status() != null ? filters.status().toString() : "APPROVED";
        boolean isApprovedStatus = "APPROVED".equalsIgnoreCase(statusStr);
        boolean isGuestStatus = MemberStatus.GUEST.toString()
                .equalsIgnoreCase(filters.status().toString());

        // 2. Logic: If status is APPROVED, check if at least one other filter exists
        boolean hasOtherFilters = StringUtils.hasText(filters.q()) ||
                StringUtils.hasText(filters.village()) ||
                StringUtils.hasText(filters.gotra()) ||
                (filters.maritalStatus() != null);

        // 3. Strict Requirement: Approved members cannot be listed without a search/filter
        if (isApprovedStatus && !hasOtherFilters) {
            return memberMapper.mapToPagedResponse(Page.empty(pageable));
        }

        // 4. Always filter by status first
        Specification<Member> spec = Specification.where((root, query, cb) ->
                cb.conjunction());

        // 5. Apply Global Search (q)
        if (StringUtils.hasText(filters.q())) {
            String pattern = "%" + filters.q().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("membershipNumber")), pattern)
            ));
        }

        // 6. Filter by Village
        if (StringUtils.hasText(filters.village())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("village"), filters.village()));
        }

        // 7. Filter by Paternal Gotra (Handling the ManyToOne relationship)
        if (StringUtils.hasText(filters.gotra())) {
            try {
                Long gotraId = Long.parseLong(filters.gotra());
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("paternalGotra").get("id"), gotraId)
                );
            } catch (NumberFormatException e) {
                // Log error or ignore if the ID is not a valid number
            }
        }

        // 8. Filter by Marital Status
        if (filters.maritalStatus() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("maritalStatus"), filters.maritalStatus()));
        }

        if (isGuestStatus) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("membershipStatus"), MembershipStatus.PENDING_APPROVAL.toString()));
        }

        if (isApprovedStatus) {
            spec = spec.and((root, query, cb) ->
                    cb.and(
                            // Exclude Pending
                            cb.notEqual(root.get("membershipStatus"), MembershipStatus.PENDING_APPROVAL),
                            // Exclude Rejected
                            cb.notEqual(root.get("membershipStatus"), MembershipStatus.REJECTED)
                    )
            );
        }

        Page<Member> memberPage = memberRepository.findAll(spec, pageable);
        return memberMapper.mapToPagedResponse(memberPage);
    }

    @Override
    public PagedResponse<MemberListItem> search(String query, Pageable pageable) {
        return null;
    }

    @Override
    public MemberDetailResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BadRequestException("Member not found"));
        return null;
    }

    @Override
    public DiscoveryResponse discoverExistingMember(DiscoverySearchRequest request) {
        Specification<Member> spec = Specification.where((root, query, cb) -> cb.conjunction());

        // 1. Mandatory Gender Filter
        if (request.gender() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("gender"), Gender.fromString(request.gender())));
        }

        // 2. Name Matching (Flexible)
        if (StringUtils.hasText(request.firstName())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("firstName")), request.firstName().toLowerCase()));
        }

        if (StringUtils.hasText(request.middleName())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("middleName")), request.middleName().toLowerCase()));
        }

        if (StringUtils.hasText(request.lastName())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("lastName")), request.lastName().toLowerCase()));
        }

        // 3. Date of Birth Matching
        if (StringUtils.hasText(request.dob())) {
            LocalDate birthDate = LocalDate.parse(request.dob());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("dob"), birthDate));
        }

        // 4. Optional Filters (Village/Gotra) to narrow down matches
        if (StringUtils.hasText(request.paternalVillage())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("paternalVillage")), request.paternalVillage().toLowerCase()));
        }

//        if (request.gotra() != null) {
//            spec = spec.and((root, query, cb) ->
//                    cb.equal(root.get("paternalGotra").get("id"), request.gotra()));
//        }
        List<Member> foundMembers = memberRepository.findAll(spec);
        if (request.gotra() != null) {
            return memberMapper.convertToResponse(foundMembers, request.gotra());
        } else {
            return memberMapper.convertToResponse(foundMembers);
        }
    }

    @Override
    @Transactional()
    public PagedResponse<MemberListItem> getManagedMembers(int page, int size) {
        Member currentUser = securityUtils.getCurrentMember();

        // 2. Setup pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. Query the repository for owned profiles
        // This assumes your repository has findByOwnerId
        Page<Member> managedMembers = memberRepository.findByOwnerId(currentUser.getId(), pageable);

        // 4. Map entities to List DTOs
        return new PagedResponse<>(
                managedMembers.toList()
                        .stream().map(memberMapper::toListItem).toList(),
                PageRequest.of(
                        managedMembers.getPageable().getPageNumber(),
                        managedMembers.getPageable().getPageSize(),
                        managedMembers.getSort()),
                managedMembers.getTotalElements()
        );
    }
}
