package com.shrimali.model.enums;

public enum OwnershipTransitionReason {
    INITIAL_CREATION,      // First person to create the profile
    CLAIM_APPROVED,        // Owner voluntarily gave up the profile
    DISPUTE_RESOLUTION,    // Admin forced a transfer after a dispute
    ADMIN_INTERVENTION,    // Manual change by support for other reasons
    PROFILE_MERGE          // Result of merging two duplicate profiles
}
