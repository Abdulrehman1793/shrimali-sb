package com.shrimali.seeder;

import com.shrimali.model.auth.Role;
import com.shrimali.model.enums.RoleName;
import com.shrimali.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class RoleStartupSeeder implements ApplicationRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        createRoleIfNotExists(RoleName.ROLE_OUTSIDER, "Outsider role");
        createRoleIfNotExists(RoleName.ROLE_GUEST, "Guest role");
        createRoleIfNotExists(RoleName.ROLE_USER, "User role");
        createRoleIfNotExists(RoleName.ROLE_ADMIN, "Admin role");
        createRoleIfNotExists(RoleName.ROLE_SUPER_ADMIN, "Super Admin role");
    }

    private void createRoleIfNotExists(RoleName roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }
}
