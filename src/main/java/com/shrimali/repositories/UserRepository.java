package com.shrimali.repositories;

import com.shrimali.model.auth.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //    @Query("Select se from User se Where se.email = ?1")
    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role"
    })
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByMemberId(Long memberId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findAllByMemberIdIn(List<Long> ids);

    @Query("""
                SELECT u
                FROM User u
                LEFT JOIN u.userRoles ur
                WHERE ur IS NULL
            """)
    List<User> findUsersWithoutRoles();
}
