package com.shrimali.seeder;

import com.shrimali.model.member.Member;
import com.shrimali.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"data", "member"})
@RequiredArgsConstructor
public class FixedMemberRecord implements ApplicationRunner {
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        memberRepository.findAll().forEach(member -> {
            String memberNumber = Member.generateMemberNumber(member.getFirstName(), member.getMiddleName(), member.getLastName());
            log.info("First Name: {}, Middle Name: {}, Last Name: {}", member.getFirstName(), member.getMiddleName(), member.getLastName());
            log.info("memberNumber: {}", memberNumber);
            member.setMembershipNumber(memberNumber);
            memberRepository.save(member);
        });
    }
}
