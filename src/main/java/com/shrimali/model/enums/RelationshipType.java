package com.shrimali.model.enums;

public enum RelationshipType {
    // --- PATERNAL EXTENDED (Father's Side) ---
    BADE_PAPA,          // Father's Elder Brother (Tau)
    BADI_MUMMY,         // Father's Elder Brother's Wife (Tai)
    CHACHA,             // Father's Younger Brother
    CHACHI,             // Father's Younger Brother's Wife
    BUA,                // Father's Sister
    PHUPHA,             // Father's Sister's Husband
    DADA,               // Paternal Grandfather
    DADI,               // Paternal Grandmother

    // --- MATERNAL EXTENDED (Mother's Side) ---
    MAMA,               // Mother's Brother
    MAMI,               // Mother's Brother's Wife
    MASI,               // Mother's Sister
    MAUSA,              // Mother's Sister's Husband
    NANA,               // Maternal Grandfather
    NANI,               // Maternal Grandmother

    // --- IN-LAWS (Sasural) ---
    SASUR,              // Father-in-law
    SAAS,               // Mother-in-law
    JETH,               // Husband's Elder Brother
    JETHANI,            // Husband's Elder Brother's Wife
    DEVAR,              // Husband's Younger Brother
    DEV_RANI,           // Husband's Younger Brother's Wife
    NANAD,              // Husband's Sister
    NANDOI,             // Husband's Sister's Husband
    SALA,               // Wife's Brother
    SALI,               // Wife's Sister
    SADHU_BHAI,         // Wife's Sister's Husband

    // --- DESCENDANTS & SPOUSES ---
    DAMAD,              // Daughter's Husband
    BAHU,               // Son's Wife
    POTA,               // Son's Son
    POTI,               // Son's Daughter
    NATI,               // Daughter's Son
    NATNI,              // Daughter's Daughter

    // --- LEGAL & SOCIAL ---
    SANKHARYA,          // Guardian
    MUKHIYA,            // Head of Extended Family
    PUL_GURU,           // Family Priest/Guru
    DHARAM_BHAI,        // Sworn Brother
    DHARAM_BEHEN,       // Sworn Sister

    OTHER
}
