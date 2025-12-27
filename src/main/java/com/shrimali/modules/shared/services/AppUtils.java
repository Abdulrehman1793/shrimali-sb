package com.shrimali.modules.shared.services;

import com.shrimali.model.Member;

public class AppUtils {
    public static int calculateCompletion(Member m) {
        int totalFields = 6;
        int filledFields = 0;

        if (isNotBlank(m.getFirstName())) filledFields++;
        if (isNotBlank(m.getMiddleName())) filledFields++;
        if (isNotBlank(m.getLastName())) filledFields++;
        if (isNotBlank(m.getProfession())) filledFields++;
        if (isNotBlank(m.getEducation())) filledFields++;

        if (m.getDob() != null) filledFields++;
        if (isNotBlank(m.getGender())) filledFields++;

        if (isNotBlank(m.getFatherFirstName())) filledFields++;
        if (isNotBlank(m.getFatherMiddleName())) filledFields++;
        if (isNotBlank(m.getFatherLastName())) filledFields++;
        if (isNotBlank(m.getPaternalVillage())) filledFields++;
        totalFields += 4;

        if (isNotBlank(m.getMotherFirstName())) filledFields++;
        if (isNotBlank(m.getMotherMiddleName())) filledFields++;
        if (isNotBlank(m.getMotherLastName())) filledFields++;
        if (isNotBlank(m.getNaniyalVillage())) filledFields++;
        totalFields += 4;

        if ("married".equalsIgnoreCase(m.getMaritalStatus())) {
            if (isNotBlank(m.getSpouseFirstName())) filledFields++;
            if (isNotBlank(m.getSpouseMiddleName())) filledFields++;
            if (isNotBlank(m.getSpouseLastName())) filledFields++;
            if (isNotBlank(m.getSpousePaternalVillage())) filledFields++;
            if (isNotBlank(m.getSpouseNaniyalVillage())) filledFields++;
            totalFields += 5;
        }

        return (filledFields * 100) / totalFields;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
