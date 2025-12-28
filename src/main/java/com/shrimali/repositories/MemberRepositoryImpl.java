package com.shrimali.repositories;

import com.shrimali.model.*;
import com.shrimali.model.auth.*;
import com.shrimali.model.enums.RoleName;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberGotra;
import com.shrimali.modules.member.dto.MemberListItem;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    /* =========================================================
       SIMPLE MEMBER SEARCH (NO GOTRAS)
       ========================================================= */

    @Override
    public Page<Member> searchByText(
            String q,
            Pageable pageable,
            Collection<RoleName> roles
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Member> cq = cb.createQuery(Member.class);
        Root<Member> member = cq.from(Member.class);

        List<Predicate> preds = new ArrayList<>();

        if (StringUtils.hasText(q)) {
            String like = "%" + q.trim().toLowerCase() + "%";

            Predicate nameMatch = cb.or(
                    cb.like(cb.lower(member.get("firstName")), like),
                    cb.like(cb.lower(member.get("middleName")), like),
                    cb.like(cb.lower(member.get("lastName")), like)
            );

            try {
                LocalDate parsed = LocalDate.parse(q.trim());
                preds.add(cb.or(nameMatch, cb.equal(member.get("dob"), parsed)));
            } catch (DateTimeParseException e) {
                preds.add(nameMatch);
            }
        }

        cq.where(preds.toArray(new Predicate[0]));
        applySort(cb, cq, member, pageable);

        TypedQuery<Member> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Member> content = query.getResultList();

        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Member> countRoot = countCq.from(Member.class);
        countCq.select(cb.count(countRoot));

        if (!preds.isEmpty()) {
            countCq.where(preds.toArray(new Predicate[0]));
        }

        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    /* =========================================================
       MEMBER LIST (ROLES + GOTRAS, ALL MEMBERS)
       ========================================================= */

    @Override
    public Page<MemberListItem> searchListItems(
            String q,
            Pageable pageable,
            Collection<RoleName> roles
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        /* =====================================================
           PHASE 1: PAGE MEMBERS ONLY
           ===================================================== */

        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<Member> member = cq.from(Member.class);
        Root<User> user = cq.from(User.class);

        List<Predicate> preds = new ArrayList<>();

        // Mandatory user link
        preds.add(cb.equal(user.get("memberId"), member.get("id")));

        // Role filter
        if (roles != null && !roles.isEmpty()) {
            Join<User, UserRole> ur = user.join("userRoles", JoinType.INNER);
            Join<UserRole, Role> r = ur.join("role", JoinType.INNER);

            CriteriaBuilder.In<RoleName> in = cb.in(r.get("name"));
            roles.forEach(in::value);
            preds.add(in);
        }

        // Search
        if (StringUtils.hasText(q)) {
            String like = "%" + q.trim().toLowerCase() + "%";

            Predicate nameMatch = cb.or(
                    cb.like(cb.lower(member.get("firstName")), like),
                    cb.like(cb.lower(member.get("middleName")), like),
                    cb.like(cb.lower(member.get("lastName")), like)
            );

            Predicate userMatch = cb.or(
                    cb.like(cb.lower(user.get("email")), like),
                    cb.like(cb.lower(user.get("phone")), like)
            );

            List<Predicate> or = new ArrayList<>();
            or.add(nameMatch);
            or.add(userMatch);

            try {
                or.add(cb.equal(member.get("dob"), LocalDate.parse(q.trim())));
            } catch (Exception ignored) {}

            preds.add(cb.or(or.toArray(new Predicate[0])));
        }

        cq.multiselect(
                member.get("id").alias("id"),
                member.get("firstName").alias("firstName"),
                member.get("middleName").alias("middleName"),
                member.get("lastName").alias("lastName"),
                member.get("gender").alias("gender"),
                member.get("dob").alias("dob"),
                member.get("photoUrl").alias("photoUrl"),
                member.get("notes").alias("notes"),
                user.get("email").alias("email"),
                user.get("phone").alias("phone")
        );

        cq.where(cb.and(preds.toArray(new Predicate[0])));
        applySort(cb, cq, member, pageable);

        TypedQuery<Tuple> pageQuery = em.createQuery(cq);
        pageQuery.setFirstResult((int) pageable.getOffset());
        pageQuery.setMaxResults(pageable.getPageSize());

        List<Tuple> rows = pageQuery.getResultList();

        /* =====================================================
           BUILD DTO MAP
           ===================================================== */

        Map<Long, MemberListItem> map = new LinkedHashMap<>();

        for (Tuple t : rows) {
            Long id = t.get("id", Long.class);

            map.put(id,
                    MemberListItem.builder()
                            .id(id)
                            .firstName(t.get("firstName", String.class))
                            .middleName(t.get("middleName", String.class))
                            .lastName(t.get("lastName", String.class))
                            .gender(t.get("gender", String.class))
                            .dob(t.get("dob", LocalDate.class))
                            .photoUrl(t.get("photoUrl", String.class))
                            .notes(t.get("notes", String.class))
                            .email(t.get("email", String.class))
                            .phone(t.get("phone", String.class))
                            .gotras(new ArrayList<>()) // always initialized
                            .build()
            );
        }

        /* =====================================================
           PHASE 2: FETCH GOTRAS FOR PAGE MEMBERS ONLY
           ===================================================== */

        if (!map.isEmpty()) {
            CriteriaQuery<Tuple> gq = cb.createTupleQuery();
            Root<MemberGotra> mg = gq.from(MemberGotra.class);
            Join<MemberGotra, Gotra> g = mg.join("gotra", JoinType.INNER);

            gq.multiselect(
                    mg.get("member").get("id").alias("memberId"),
                    g.get("name").alias("gotraName")
            );

            gq.where(mg.get("member").get("id").in(map.keySet()));

            List<Tuple> gotraRows = em.createQuery(gq).getResultList();

            for (Tuple t : gotraRows) {
                map.get(t.get("memberId", Long.class))
                        .getGotras()
                        .add(t.get("gotraName", String.class));
            }
        }

        /* =====================================================
           COUNT QUERY
           ===================================================== */

        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Member> m = countCq.from(Member.class);
        Root<User> u = countCq.from(User.class);

        List<Predicate> countPreds = new ArrayList<>();
        countPreds.add(cb.equal(u.get("memberId"), m.get("id")));

        if (roles != null && !roles.isEmpty()) {
            Join<User, UserRole> ur = u.join("userRoles", JoinType.INNER);
            Join<UserRole, Role> r = ur.join("role", JoinType.INNER);

            CriteriaBuilder.In<RoleName> in = cb.in(r.get("name"));
            roles.forEach(in::value);
            countPreds.add(in);
        }

        countCq.select(cb.countDistinct(m));
        countCq.where(cb.and(countPreds.toArray(new Predicate[0])));

        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(
                new ArrayList<>(map.values()),
                pageable,
                total
        );
    }

    /* =========================================================
       SORT
       ========================================================= */

    private void applySort(
            CriteriaBuilder cb,
            CriteriaQuery<?> cq,
            Root<Member> member,
            Pageable pageable
    ) {
        if (!pageable.getSort().isSorted()) return;

        List<Order> orders = new ArrayList<>();
        for (Sort.Order o : pageable.getSort()) {
            orders.add(o.isAscending()
                    ? cb.asc(member.get(o.getProperty()))
                    : cb.desc(member.get(o.getProperty())));
        }
        cq.orderBy(orders);
    }
}
