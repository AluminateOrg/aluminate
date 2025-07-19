package com.aluminate.aluminate_organization_backend.service;

import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public int saveValidMembers(List<MemberRowDTO> validRows) {
        int count = 0;
        for (MemberRowDTO dto :  validRows) {
            if (!memberRepository.existsByNic(dto.getNic()) && !memberRepository.existsByEmail(dto.getEmail())) {
                String rawPassword = dto.getNic();
                Member member = Member.builder()
                        .name(dto.getName())
                        .nic(dto.getNic())
                        .email(dto.getEmail())
                        .phone(dto.getPhone())
                        .regNo(dto.getRegNo())
                        .batch(dto.getBatch())
                        .password(passwordEncoder.encode(rawPassword))
                        .build();
                memberRepository.save(member);
                count++;
                emailService.sendWelcomeEmail(dto.getEmail(), dto.getName(), dto.getNic());
            }
        }
        return count;
    }
}
