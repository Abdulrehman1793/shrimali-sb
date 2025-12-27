package com.shrimali.modules.shared.utils;

import java.util.regex.Pattern;

public interface AppConstant {
    Pattern PHONE = Pattern.compile("^\\+?[0-9]{10,15}$");

    Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    Pattern SOCIAL = Pattern.compile("^(https?://).+|^[A-Za-z0-9_.-]{3,50}$");

}
