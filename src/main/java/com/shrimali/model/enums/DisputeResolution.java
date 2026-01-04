package com.shrimali.model.enums;

public enum DisputeResolution {
    RETAIN_OWNER,        // Admin decided the current manager is correct
    TRANSFER_OWNERSHIP,  // Admin verified the requester is the real person
    REJECT_BOTH,         // Both provided fake/insufficient info
    CANCELLED            // One party withdrew the request
}
