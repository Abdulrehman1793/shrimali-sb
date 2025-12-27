package com.shrimali.modules.auth.service.impl;

import com.shrimali.model.auth.User;
import com.shrimali.modules.auth.dto.UserDTO;
import com.shrimali.modules.auth.service.UserService;
import com.shrimali.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    @Override
    public User findUserByName(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        return null;
    }
}
