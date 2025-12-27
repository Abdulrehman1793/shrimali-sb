package com.shrimali.modules.auth.service;

import com.shrimali.model.auth.Otp;
import com.shrimali.model.auth.User;

public interface OtpService {
    Otp generateOtp(User user, String purpose);

    Otp validateOtp(String purpose, String code);

    void consumeOtp(Otp otp);
}
