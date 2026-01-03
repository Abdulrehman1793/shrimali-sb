package com.shrimali.modules.member.services.impl;

import com.shrimali.dto.PagedResponse;
import com.shrimali.model.enums.Gender;
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
        Specification<Member> spec = Specification.where((root, query, cb) -> cb.conjunction());

        // 1. Handle Global Search (q) - matches firstName, lastName, or email
        if (StringUtils.hasText(filters.q())) {
            String pattern = "%" + filters.q().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                return cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("membershipNumber")), pattern)
                );
            });
        }

        // 2. Filter by Village (Exact match)
        if (StringUtils.hasText(filters.village())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("village"), filters.village()));
        }

        // 3. Filter by Gotra (Exact match)
        if (StringUtils.hasText(filters.gotra())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("gotra"), filters.gotra()));
        }

        // 4. Filter by Marital Status (Exact match)
        if (StringUtils.hasText(filters.maritalStatus()) && !"none".equalsIgnoreCase(filters.maritalStatus())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("maritalStatus"), filters.maritalStatus()));
        }

        // Execute query with specification and pagination
        Page<Member> memberPage = memberRepository.findAll(spec, pageable);

        // Convert Page<Member> to PagedResponse<MemberListItem>
        return memberMapper.mapToPagedResponse(memberPage);
    }

    @Override
    public PagedResponse<MemberListItem> search(String query, Pageable pageable) {
        return null;
    }

    @Override
    public MemberResponse getMember(Long memberId) {
        return null;
    }

    @Override
    public DiscoveryResponse discoverExistingMember(DiscoverySearchRequest request) {
        Gender searchGender = Gender.fromString(request.gender());

        // 2. Parse Date
        LocalDate birthDate = LocalDate.parse(request.dob());

        // 3. Execute Search
        List<Member> foundMembers = memberRepository
                .findByFirstNameIgnoreCaseOrMiddleNameIgnoreCaseAndLastNameIgnoreCaseAndDobAndGender(
                        request.firstName(),
                        request.middleName(),
                        request.lastName(),
                        birthDate,
                        searchGender
                );

        return memberMapper.convertToResponse(foundMembers);
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
