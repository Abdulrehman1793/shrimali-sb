package com.shrimali.repositories;

import com.shrimali.model.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
//    @Query("""
//            select m from Member m
//            """)
//    Page<Member> searchByText(String q, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE " +
            "LOWER(m.firstName) = LOWER(:firstName) AND " +
            "LOWER(m.lastName) = LOWER(:lastName) AND " +
            "m.dob = :dob AND " +
            "m.linkedUser IS NULL")
    List<Member> findUnclaimedMatches(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dob") LocalDate dob
    );
}
