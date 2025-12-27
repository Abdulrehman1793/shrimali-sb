package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.GotraDTO;

import java.security.Principal;
import java.util.List;

public interface GotraService {
    List<GotraDTO> list(Principal principal);

    void add(Principal principal, GotraDTO gotraDTO);

    void update(Principal principal, GotraDTO gotraDTO);

    void remove(Principal principal, GotraDTO gotraDTO);
}
