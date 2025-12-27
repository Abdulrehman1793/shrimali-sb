package com.shrimali.modules.member.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SpouseDetailsDTO {
    private String spouseFirstName;
    private String spouseMiddleName;
    private String spouseLastName;
    private String spousePaternalVillage;
    private String spouseNaniyalVillage;
    private LocalDate marriageDate;
}
