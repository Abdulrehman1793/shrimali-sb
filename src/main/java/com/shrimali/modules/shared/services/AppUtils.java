package com.shrimali.modules.shared.services;

import com.shrimali.model.member.Member;

public class AppUtils {
    public static int calculateCompletion(com.shrimali.model.member.Member m) {
        int totalFields = 6;
        int filledFields = 0;

        if (isNotBlank(m.getFirstName())) filledFields++;
        if (isNotBlank(m.getMiddleName())) filledFields++;
        if (isNotBlank(m.getLastName())) filledFields++;
        if (isNotBlank(m.getProfession())) filledFields++;
        if (isNotBlank(m.getEducation())) filledFields++;

        if (m.getDob() != null) filledFields++;
        if (m.getGender() != null) filledFields++;

        if (isNotBlank(m.getPaternalVillage())) filledFields++;
        totalFields += 1;

        if (isNotBlank(m.getNaniyalVillage())) filledFields++;
        totalFields += 1;

        if ("married".equalsIgnoreCase(m.getMaritalStatus())) {
            totalFields += 3;
        }

        return (filledFields * 100) / totalFields;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
