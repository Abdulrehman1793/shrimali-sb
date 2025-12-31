package com.shrimali.modules.member.services.impl;

import com.shrimali.dto.PagedResponse;
import com.shrimali.model.enums.Gender;
import com.shrimali.modules.member.dto.DiscoveryResponse;
import com.shrimali.modules.member.dto.DiscoverySearchRequest;
import com.shrimali.modules.member.dto.MemberListItem;
import com.shrimali.modules.member.dto.MemberResponse;
import com.shrimali.modules.member.mapper.MemberMapper;
import com.shrimali.modules.member.services.MemberSearchService;
import com.shrimali.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MemberSearchServiceImpl implements MemberSearchService {

    private final MemberRepository memberRepository;

    private final MemberMapper memberMapper;

    @Override
    public PagedResponse<MemberListItem> findPendingMembers(String query, Pageable pageable) {
        return null;
    }

    @Override
    public PagedResponse<MemberListItem> listMembers(String query, Pageable pageable) {
        return null;
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
        return memberRepository.findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDobAndGender(
                        request.firstName(),
                        request.lastName(),
                        birthDate,
                        searchGender
                )
                .map(memberMapper::convertToResponse)
                .orElse(new DiscoveryResponse(false, null));
    }
}
