package com.shrimali.repositories;

import com.shrimali.model.MemberGotra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberGotraRepository extends JpaRepository<MemberGotra, Long> {
    List<MemberGotra> findByMemberId(Long memberId);

    Optional<MemberGotra> findByMemberIdAndIsPrimaryTrue(Long memberId);

    @Query("""
                select mg
                from MemberGotra mg
                where mg.member.id = :memberId
                order by mg.isPrimary desc, mg.createdAt asc
            """)
    List<MemberGotra> findByMemberIdOrdered(@Param("memberId") Long memberId);

    Optional<MemberGotra> findByMemberIdAndGotraId(Long memberId, Long gotraId);

    boolean existsByMemberIdAndGotraId(Long memberId, Long gotraId);

    boolean existsByGotraId( Long gotraId);

    boolean existsByMemberIdAndIsPrimaryTrue(Long memberId);
}
