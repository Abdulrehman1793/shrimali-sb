package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.ContactPayload;

import java.security.Principal;

import java.util.List;

public interface MemberContactService {
    List<ContactPayload> listContacts(String membershipNumber);

    void addContact(String membershipNumber, ContactPayload contactPayload);

    void updateContact(String membershipNumber, Long id, ContactPayload contactPayload);

    void removeContact(String membershipNumber, Long id);
}
