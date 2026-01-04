package com.shrimali.model.enums;

public enum ClaimStatus {
    PENDING,    // Waiting for current owner
    APPROVED,   // Transfer complete
    REJECTED,   // Owner said no
    DISPUTED,    // Sent to Admin for ID check
    WITHDRAWN
}
