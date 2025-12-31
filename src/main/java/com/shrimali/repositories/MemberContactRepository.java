package com.shrimali.repositories;

import com.shrimali.model.member.MemberContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MemberContactRepository extends JpaRepository<MemberContact, Long> {
    List<MemberContact> findByMember_Id(Long memberId);

    Optional<MemberContact> findByMember_IdAndTypeAndValue(
            Long memberId, String type, String value
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE MemberContact c
           SET c.isPrimary = false
         WHERE c.member.id = :memberId
           AND c.type = :type
    """)
    void clearPrimaryByType(
            @Param("memberId") Long memberId,
            @Param("type") String type
    );
}
