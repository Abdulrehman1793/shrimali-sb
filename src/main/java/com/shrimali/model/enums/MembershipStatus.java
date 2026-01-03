package com.shrimali.model.enums;

public enum MembershipStatus {
    // --- Application Lifecycle ---
    PENDING_APPROVAL, // Submitted, awaiting admin review
    REJECTED,         // Application denied

    // --- Genealogy & Ghost Profiles ---
    PROVISIONAL,      // Profile created by a relative (Ghost Profile); not yet "claimed"

    // --- Active Tiers ---
    ACTIVE,           // Standard verified member
    PREMIUM,          // Paid/Tiered membership (added per your request)
    LIFE_MEMBER,      // One-time high-tier contribution
    HONORARY,         // Distinguished elders or invited members

    // --- Administrative & Special States ---
    INACTIVE,         // Not engaged or subscription lapsed
    SUSPENDED,        // Access revoked due to policy violation
    RESIGNED,         // Member requested to leave the community
    DECEASED,         // Permanent status for genealogy records

    // --- Catch-all ---
    GUEST             // Minimal access/External contact
}
