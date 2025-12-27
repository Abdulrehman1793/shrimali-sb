package com.shrimali.modules.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberListItem {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dob;
    private String photoUrl;
    private String notes;

    private String phone;
    private String email;

    private List<String> gotras;
}
