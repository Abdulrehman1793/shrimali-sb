package com.shrimali.repositories.geo;

import com.shrimali.model.geo.Country;
import com.shrimali.model.geo.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    @Query("""
                SELECT s FROM State s
                WHERE s.country.id = :countryId
                  AND (
                        :q IS NULL OR
                        LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
                        LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%'))
                      )
                ORDER BY s.name
            """)
    List<State> searchByCountry(@Param("countryId") Long countryId, @Param("q") String q);

    boolean existsByCountryAndCode(Country country, String code);

    Optional<State> findByCountryAndCode(Country country, String code);

    @Query("SELECT s FROM State s JOIN FETCH s.country")
    List<State> findAllWithCountry();

    @Query("SELECT concat(s.country.id, ':', s.code) FROM State s")
    List<String> findAllStateKeys();
}
