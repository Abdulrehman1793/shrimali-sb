package com.shrimali.modules.member.services;

import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.auth.UserRole;
import com.shrimali.model.enums.RoleName;
import org.springframework.stereotype.Service;

@Service
public class MemberCommonService {
    public boolean hasRole(User user, RoleName roleName) {
        return user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == roleName);
    }

    public void removeRole(User user, RoleName roleName) {
        user.getUserRoles().removeIf(
                ur -> ur.getRole().getName() == roleName
        );
    }

    public void addRoleIfMissing(User user, Role role) {
        boolean exists = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == role.getName());

        if (!exists) {
            UserRole userRole = new UserRole();
            userRole.setRole(role);
            user.getUserRoles().add(userRole);
        }
    }
}
