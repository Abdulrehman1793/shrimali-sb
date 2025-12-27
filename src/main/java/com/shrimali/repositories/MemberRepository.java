package com.shrimali.repositories;

import com.shrimali.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> , MemberRepositoryCustom{
//    @Query("""
//            select m from Member m
//            """)
//    Page<Member> searchByText(String q, Pageable pageable);
}
