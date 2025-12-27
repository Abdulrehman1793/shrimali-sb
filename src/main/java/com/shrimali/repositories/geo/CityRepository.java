package com.shrimali.repositories.geo;

import com.shrimali.model.geo.City;
import com.shrimali.model.geo.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CityRepository extends JpaRepository<City, Long> {
    @Query("""
                SELECT c FROM City c
                WHERE c.state.id = :stateId
                  AND (
                        :q IS NULL OR
                        LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                      )
                ORDER BY c.name
            """)
    List<City> searchByState(@Param("stateId") Long stateId, @Param("q") String q);

    boolean existsByStateAndName(State state, String name);

    @Query("SELECT concat(c.state.id, ':', lower(trim(c.name))) FROM City c")
    Set<String> findAllCityKeys();
}
