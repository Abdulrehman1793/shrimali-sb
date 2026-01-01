package com.shrimali.modules.member.services;

import com.shrimali.dto.PagedResponse;
import com.shrimali.modules.member.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * Service for read-only member exploration and directory browsing.
 * * This service handles complex querying and pagination for the community.
 * Implementation should focus on high-performance searching (e.g., JPA Specifications).
 */
public interface MemberSearchService {
    /**
     * Performs a global keyword search across name, village, and gotra fields.
     * This is the primary engine for the public-facing member directory.
     * * @param query    The search string (firstName, lastName, etc.)
     *
     * @param pageable Pagination and sorting parameters
     * @return A paged list of member summaries
     */
    PagedResponse<MemberListItem> search(String query, Pageable pageable);

    /**
     * Lists all active community members with default filtering.
     * Typically, excludes "Ghost" profiles unless requested.
     * * @param query    Optional filter string
     *
     * @param pageable Pagination and sorting parameters
     * @return A paged list of active members
     */
    PagedResponse<MemberListItem> listMembers(String query, Pageable pageable);

    /**
     * Retrieves the administrative queue of members awaiting verification.
     * Filters for profiles with {@code status = DRAFT} or {@code verified = false}.
     * * @param query    Optional filter to search within the pending queue
     *
     * @param pageable Pagination parameters
     * @return A paged list of members requiring admin action
     */
    PagedResponse<MemberListItem> findPendingMembers(String query, Pageable pageable);

    /**
     * Retrieves specific member details for relationship matching purposes.
     * <p>
     * This is used during the discovery flow to compare a potential relative's
     * data (like Gotra, Village, and Parents) with the target member record.
     * </p>
     *
     * @param memberId The unique database ID of the member to match against.
     * @return A {@link MemberMatchResponse} containing filtered data suitable for comparison.
     */
    MemberResponse getMember(Long memberId);

    DiscoveryResponse discoverExistingMember(DiscoverySearchRequest request);

    PagedResponse<MemberListItem> getManagedMembers(int page, int size);
}
