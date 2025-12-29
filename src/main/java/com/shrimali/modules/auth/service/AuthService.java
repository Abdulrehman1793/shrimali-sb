package com.shrimali.modules.auth.service;

import com.shrimali.model.auth.User;
import com.shrimali.modules.auth.dto.*;
import org.springframework.security.core.Authentication;

import java.security.Principal;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);

    RegisterResponse register(RegisterRequest request);

    AuthResponse oauthLogin(User user);

    UserResponse userByEmail(String email);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);

    UserResponse me(Principal principal);

    AuthResponse generateToken(Principal principal);

    void removeAccount();
}
