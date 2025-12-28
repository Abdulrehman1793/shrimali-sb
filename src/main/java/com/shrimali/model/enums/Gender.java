package com.shrimali.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    Male, Female, Other;

//    @JsonCreator
//    public static Gender fromString(String value) {
//        if (value == null) return null;
//        return Gender.valueOf(value.toUpperCase());
//    }
}
