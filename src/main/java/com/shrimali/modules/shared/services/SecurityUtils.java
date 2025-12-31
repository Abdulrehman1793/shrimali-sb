package com.shrimali.modules.shared.services;

import com.shrimali.dto.AuthenticatedIdentity;
import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import com.shrimali.repositories.MemberRepository;
import com.shrimali.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    /**
     * Gets both User and Member in one call.
     * Useful for updates where you need User for auditing and Member for data.
     */
    public AuthenticatedIdentity getCurrentIdentity() {
        User user = getCurrentUser();

        // Safety check: Ensure the user actually has a memberId assigned
        if (user.getMemberId() == null) {
            throw new BadRequestException("User account exists but no member profile is assigned to it.");
        }

        Member member = memberRepository.findById(user.getMemberId())
                .orElseThrow(() -> new BadRequestException("No member profile linked to this account"));

        return new AuthenticatedIdentity(user, member);
    }

    /**
     * Gets the currently authenticated User entity from the database.
     * Use this in your services to link members to users.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            throw new BadRequestException("No authenticated user found in context");
        }

        // In most Spring Security setups, the 'Name' of the authentication is the Email
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));
    }

    /**
     * Gets the Member profile associated with the currently authenticated User.
     */
    public Member getCurrentMember() {
        User user = getCurrentUser();

        // Safety check: Ensure the user actually has a memberId assigned
        if (user.getMemberId() == null) {
            throw new BadRequestException("User account exists but no member profile is assigned to it.");
        }

        // This assumes your MemberRepository has a method to find by the linked user
        return memberRepository.findById(user.getMemberId())
                .orElseThrow(() -> new BadRequestException("No member profile linked to user: " + user.getEmail()));
    }

    /**
     * Optional: Just get the email string if you don't need the whole User object.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "anonymousUser";
    }
}
