package com.shrimali.repositories.member;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.ClaimStatus;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberClaimRepository extends JpaRepository<MemberClaim, Long> {
    boolean existsByTargetMemberAndStatus(Member targetMember, ClaimStatus status);

    boolean existsByRequesterAndStatus(User requester, ClaimStatus status);

    Optional<MemberClaim> findTopByRequesterAndStatusOrderByCreatedAtDesc(
            User requester, ClaimStatus status);
}
