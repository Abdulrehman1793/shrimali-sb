package com.shrimali.seeder;

import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.auth.UserRole;
import com.shrimali.model.enums.RoleName;
import com.shrimali.repositories.RoleRepository;
import com.shrimali.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("seed")
@Component
@RequiredArgsConstructor
@Order(2)
@Transactional
public class UserRoleBackfillSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        Role defaultUserRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER must exist before backfilling users"));

        List<User> usersWithoutRoles = userRepository.findUsersWithoutRoles();

        if (usersWithoutRoles.isEmpty()) {
            return;
        }

        for (User user : usersWithoutRoles) {
            UserRole userRole = new UserRole();
            userRole.setRole(defaultUserRole);
            user.getUserRoles().add(userRole);
        }

        userRepository.saveAll(usersWithoutRoles);
    }
}
