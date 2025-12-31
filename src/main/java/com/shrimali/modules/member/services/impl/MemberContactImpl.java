package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberContact;
import com.shrimali.modules.member.dto.ContactPayload;
import com.shrimali.modules.member.services.MemberContactService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.modules.shared.utils.AppConstant;
import com.shrimali.repositories.MemberContactRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberContactImpl implements MemberContactService {

    private final SecurityUtils securityUtils;

    private final MemberContactRepository memberContactRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ContactPayload> listContacts(Principal principal) {
        Long memberId = getMemberId(principal);

        return memberContactRepository.findByMember_Id(memberId)
                .stream()
                .map(c -> new ContactPayload(
                        c.getId(),
                        c.getType(),
                        c.getIsPrimary(),
                        c.getValue()
                ))
                .toList();
    }

    @Transactional
    @Override
    public void addContact(Principal principal, ContactPayload payload) {
        Long memberId = getMemberId(principal);

        validateContact(payload);

        // Prevent duplicate
        memberContactRepository
                .findByMember_IdAndTypeAndValue(
                        memberId, payload.type(), payload.value()
                )
                .ifPresent(c -> {
                    throw new BadRequestException("Contact already exists");
                });

        // If primary → unset previous primary for this type
        if (Boolean.TRUE.equals(payload.isPrimary())) {
            memberContactRepository.clearPrimaryByType(memberId, payload.type());
        }

        MemberContact contact = MemberContact.builder()
                .member(Member.builder().id(memberId).build())
                .type(payload.type())
                .value(payload.value())
                .isPrimary(Boolean.TRUE.equals(payload.isPrimary()))
                .build();

        memberContactRepository.save(contact);
    }

    @Override
    public void updateContact(Long id, ContactPayload payload) {
        validateContact(payload);

        MemberContact contact = memberContactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contact not found"));

        if (Boolean.TRUE.equals(payload.isPrimary())) {
            memberContactRepository.clearPrimaryByType(id, payload.type());
        }

        contact.setValue(payload.value());
        contact.setIsPrimary(Boolean.TRUE.equals(payload.isPrimary()));

        memberContactRepository.save(contact);
    }

    @Override
    public void removeContact(Principal principal, ContactPayload payload) {
        Long memberId = getMemberId(principal);

        MemberContact contact = memberContactRepository
                .findByMember_IdAndTypeAndValue(
                        memberId, payload.type(), payload.value()
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Contact not found")
                );

        memberContactRepository.delete(contact);
    }

    private Long getMemberId(Principal principal) {
        User userPrincipal = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        assert userPrincipal != null;
        Long memberId = userPrincipal.getMemberId();

        if (userPrincipal.getMemberId() == null) {
            throw new AccessDeniedException("Invalid user principal");
        }

        return userPrincipal.getMemberId();
    }

    private void validateContact(ContactPayload payload) {
        if (payload.type() == null || payload.value() == null) {
            throw new BadRequestException("Contact type and value are required");
        }

        String type = payload.type().toLowerCase();
        String value = payload.value().trim();

        switch (type) {
            case "mobile", "whatsapp" -> {
                if (!value.matches(String.valueOf(AppConstant.PHONE))) {
                    throw new BadRequestException("Invalid phone number format");
                }
            }

            case "email" -> {
                if (!value.matches(String.valueOf(AppConstant.EMAIL))) {
                    throw new BadRequestException("Invalid email address");
                }
            }

            case "social" -> {
                if (!value.matches(String.valueOf(AppConstant.SOCIAL))) {
                    throw new BadRequestException("Invalid social profile");
                }
            }

            default -> throw new BadRequestException(
                    "Unsupported contact type"
            );
        }
    }

}
