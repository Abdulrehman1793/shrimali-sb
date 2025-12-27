package com.shrimali.repositories.geo;

import com.shrimali.model.geo.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    @Query("""
                SELECT c FROM Country c
                WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%'))
                ORDER BY c.name
            """)
    Page<Country> search(@Param("q") String q, Pageable pageable);

    Page<Country> findAllByOrderByNameAsc(Pageable pageable);

    boolean existsByCode(String code);

    Optional<Country> findByCode(String code);
}
