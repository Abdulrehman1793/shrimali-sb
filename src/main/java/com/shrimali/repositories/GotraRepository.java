package com.shrimali.repositories;

import com.shrimali.model.Gotra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GotraRepository extends JpaRepository<Gotra, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Gotra> findByNameIgnoreCase(String name);

    @Query("""
                select mg.gotra
                from MemberGotra mg
                where mg.member.id = :memberId
            """)
    List<Gotra> findGotrasByMemberId(@Param("memberId") Long memberId);

}
