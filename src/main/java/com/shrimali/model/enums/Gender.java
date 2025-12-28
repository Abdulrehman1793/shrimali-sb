package com.shrimali.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    Male, Female, Other;

    public static Gender fromString(String value) {
        if (value == null) return null;
        for (Gender g : Gender.values()) {
            if (g.name().equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + value);
    }

//    @JsonCreator
//    public static Gender fromString(String value) {
//        if (value == null) return null;
//        return Gender.valueOf(value.toUpperCase());
//    }
}
