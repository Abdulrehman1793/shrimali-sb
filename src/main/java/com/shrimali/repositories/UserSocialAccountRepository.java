package com.shrimali.repositories;

import com.shrimali.model.auth.User;
import com.shrimali.model.auth.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Integer> {
    void deleteByUser(User user);
}
