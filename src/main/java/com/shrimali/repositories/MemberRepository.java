package com.shrimali.repositories;

import com.shrimali.model.enums.Gender;
import com.shrimali.model.member.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    // Core discovery query: matches by Names, DOB, and Gender
    Optional<Member> findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDobAndGender(
            String firstName,
            String lastName,
            LocalDate dob,
            Gender gender
    );

    // Optional: Broader search if you want to include Gotra/Village to narrow down duplicates
    @Query("SELECT m FROM Member m WHERE LOWER(m.firstName) = LOWER(:fn) " +
            "AND LOWER(m.lastName) = LOWER(:ln) AND m.dob = :dob " +
            "AND (:village IS NULL OR LOWER(m.paternalVillage) = LOWER(:village))")
    Optional<Member> findWithOptionalFilters(
            @Param("fn") String fn,
            @Param("ln") String ln,
            @Param("dob") LocalDate dob,
            @Param("village") String village);

    List<Member> findByOwnerId(Long ownerUserId);

    @EntityGraph(attributePaths = {"addresses", "contacts"})
    Optional<Member> findById(Long id);
}
