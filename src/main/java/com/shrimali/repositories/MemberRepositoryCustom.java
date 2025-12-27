package com.shrimali.repositories;

import com.shrimali.model.Member;
import com.shrimali.model.enums.RoleName;
import com.shrimali.modules.member.dto.MemberListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

public interface MemberRepositoryCustom {
    /**
     * Free-text search on members (no role filter).
     */
    Page<Member> searchByText(String q, Pageable pageable, Collection<RoleName> roles);

    /**
     * Role-based member list search (supports one or multiple roles).
     *
     * @param q        search text (name, email, phone, dob)
     * @param pageable paging + sorting
     * @param roles    allowed user roles (ROLE_USER, ROLE_ADMIN, etc.)
     */
    Page<MemberListItem> searchListItems(
            String q,
            Pageable pageable,
            Collection<RoleName> roles
    );
}
