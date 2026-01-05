package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.*;

/**
 * Service for authenticated members to manage their own data.
 * Implementation relies on SecurityUtils to identify the current user.
 */
public interface MemberProfileService {

    /**
     * Gets a lightweight summary of the current user.
     */
    MemberResponse me();

    /**
     * Gets the full profile details for the dashboard.
     */
    MemberProfileResponse getCurrentMemberProfile(String memberShipNumber);

    /**
     * Updates basic identity info (Name, DOB, Gender).
     * Should be blocked if profile is verified or locked.
     */
    void updateBasicInfo(BasicInfoDTO dto);

    /**
     * Specialized updates for family tree links.
     */
    void updateFatherDetails(MemberPayload dto);
    void updateMotherDetails(MemberPayload dto);
    void updateSpouseDetails(MemberPayload dto);

    /**
     * Adds or updates contact information (Phone, Email, Social).
     */
    void addOrUpdateContact(ContactPayload payload);
}
