package com.shrimali.model.enums;

public enum UserStatus {
    /**
     * Account is newly created but email or phone is not yet verified.
     * Login may be restricted depending on your security policy.
     */
    PENDING_VERIFICATION,

    /**
     * Account is healthy and the user can log in freely.
     */
    ACTIVE,

    /**
     * The user has been manually blocked by an Admin (e.g., for community violations).
     */
    BANNED,

    /**
     * Account is temporarily locked due to too many failed login attempts.
     */
    LOCKED,

    /**
     * Account is marked for deletion or deactivated by the user.
     */
    DEACTIVATED,

    /**
     * Used if the account is under review for suspicious activity (bot-like behavior).
     */
    SUSPENDED
}
