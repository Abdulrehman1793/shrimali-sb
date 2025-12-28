package com.shrimali.repositories;

import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    List<MemberAddress> findByMember(Member member);

    Optional<MemberAddress> findByMemberAndAddressType(Member member, String addressType);

    void deleteByMemberAndAddressType(Member member, String addressType);
}
