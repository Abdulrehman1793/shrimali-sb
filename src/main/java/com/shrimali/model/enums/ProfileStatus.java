package com.shrimali.model.enums;

public enum ProfileStatus {
    DRAFT,      // Editable by owner
    VERIFIED,   // Identity frozen, non-identity editable
    LOCKED,     // Fully read-only (Deceased or Admin action)
    REJECTED,   // Hidden/Invalid
    DECEASED
}
