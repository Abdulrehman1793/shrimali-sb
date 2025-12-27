package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.GotraDTO;
import com.shrimali.modules.member.dto.MemberGotraDTO;

import java.security.Principal;
import java.util.List;

public interface MemberGotraService {
    List<MemberGotraDTO> findByMember(Principal principal);

    MemberGotraDTO addMemberGotra(Principal principal, GotraDTO dto, boolean marriageCase);

    List<MemberGotraDTO> removeMemberGotra(Principal principal, Long gotraId);

    MemberGotraDTO getPrimaryGotra(Principal principal);

    void setPrimaryGotra(Principal principal, Long gotraId);
}
