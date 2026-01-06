package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberContact;
import com.shrimali.modules.member.dto.ContactPayload;
import com.shrimali.modules.member.services.MemberContactService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.modules.shared.utils.AppConstant;
import com.shrimali.repositories.MemberContactRepository;
import com.shrimali.repositories.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberContactImpl implements MemberContactService {

    private final SecurityUtils securityUtils;

    private final MemberRepository memberRepository;
    private final MemberContactRepository memberContactRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ContactPayload> listContacts(String membershipNumber) {
        Member member = securityUtils.getCurrentMember();

        Member targetMember = resolveTargetMember(membershipNumber, member);

        return memberContactRepository.findByMember_Id(targetMember.getId())
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
    public void addContact(String membershipNumber, ContactPayload payload) {
        Member member = securityUtils.getCurrentMember();

        Member targetMember = resolveTargetMember(membershipNumber, member);

        validateContact(payload);

        // Prevent duplicate
        memberContactRepository
                .findByMember_IdAndTypeAndValue(
                        targetMember.getId(), payload.type(), payload.value()
                )
                .ifPresent(c -> {
                    throw new BadRequestException("Contact already exists");
                });

        // If primary → unset previous primary for this type
        if (Boolean.TRUE.equals(payload.isPrimary())) {
            memberContactRepository.clearPrimaryByType(targetMember.getId(), payload.type());
        }

        MemberContact contact = MemberContact.builder()
                .member(targetMember)
                .type(payload.type())
                .value(payload.value())
                .isPrimary(Boolean.TRUE.equals(payload.isPrimary()))
                .build();

        memberContactRepository.save(contact);
    }

    @Override
    public void updateContact(String membershipNumber, Long id, ContactPayload payload) {
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
    @Transactional
    public void removeContact(String membershipNumber, Long id) {
        Member member = securityUtils.getCurrentMember();

        Member targetMember = resolveTargetMember(membershipNumber, member);

        boolean removed = targetMember.getContacts()
                .removeIf(contact -> contact.getId().equals(id));

        if (!removed) {
            throw new EntityNotFoundException("Contact not found");
        }
    }

    private Member resolveTargetMember(String membershipNumber, Member currentUserMember) {
        if (membershipNumber == null || membershipNumber.equalsIgnoreCase("self")) {
            return currentUserMember;
        }
        return memberRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new BadRequestException("Target member record not found"));
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
