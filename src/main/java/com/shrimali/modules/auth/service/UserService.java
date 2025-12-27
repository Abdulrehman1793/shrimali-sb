package com.shrimali.modules.auth.service;

import com.shrimali.model.auth.User;
import com.shrimali.modules.auth.dto.UserDTO;

public interface UserService {
    User findUserByName(String username);

    UserDTO createUser(UserDTO userDTO);
}
