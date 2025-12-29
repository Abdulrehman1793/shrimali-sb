package com.shrimali.modules.shared.services;


import com.shrimali.model.member.Member;

public class AppUtils {
    public static int calculateCompletion(Member m) {
        if (m == null) return 0;

        int filled = 0;
        int total = 0;

        // 1. Basic Identity (Core)
        total++; if (isNotBlank(m.getFirstName())) filled++;
        total++; if (isNotBlank(m.getLastName())) filled++;
        total++; if (m.getGender() != null) filled++;
        total++; if (m.getDob() != null) filled++;

        // 2. Social & Ancestral
        total++; if (m.getPaternalGotra() != null) filled++;
        total++; if (isNotBlank(m.getPaternalVillage())) filled++;
        total++; if (isNotBlank(m.getNaniyalVillage())) filled++;
        total++; if (m.getMaternalGotra() != null) filled++;

        // 3. Professional
        total++; if (isNotBlank(m.getEducation())) filled++;
        total++; if (isNotBlank(m.getProfession())) filled++;

        // 4. Contact & Location (Checking collections)
        var addresses = m.getAddresses();
        if (addresses != null && !addresses.isEmpty()) {
            filled++;
        }

        var contacts = m.getContacts();
        if (contacts != null && !contacts.isEmpty()) {
            filled++;
        }

        // 5. Conditional Logic for Married Members
        // If married, we expect Spouse and Marriage Date to be filled
        if ("Married".equalsIgnoreCase(m.getMaritalStatus())) {
            total++; if (m.getSpouse() != null) filled++;
            total++; if (m.getMarriageDate() != null) filled++;
        } else {
            // If not married, marital status itself still counts as a data point
            total++; if (isNotBlank(m.getMaritalStatus())) filled++;
        }

        // 6. Media
        total++; if (isNotBlank(m.getPhotoUrl())) filled++;

        return (filled * 100) / total;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
