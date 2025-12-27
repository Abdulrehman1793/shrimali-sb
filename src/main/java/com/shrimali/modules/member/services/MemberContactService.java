package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.ContactPayload;

import java.security.Principal;

import java.util.List;

public interface MemberContactService {
    List<ContactPayload> listContacts(Principal principal);

    void addContact(Principal principal, ContactPayload contactPayload);

    void updateContact(Principal principal, ContactPayload contactPayload);

    void removeContact(Principal principal, ContactPayload contactPayload);
}
