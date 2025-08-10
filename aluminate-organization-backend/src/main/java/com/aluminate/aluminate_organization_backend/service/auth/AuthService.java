package com.aluminate.aluminate_organization_backend.service.auth;

import com.aluminate.aluminate_organization_backend.config.util.Jwt;
import com.aluminate.aluminate_organization_backend.dto.MemberDTO;
import com.aluminate.aluminate_organization_backend.dto.login.LoginResponse;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwt jwt;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, Jwt jwt) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    public LoginResponse login(String email, String password,String role) {
        //check if role is 'member' or 'admin'
        if (!role.equals("member") && !role.equals("admin")) {
            throw new RuntimeException("Invalid role");
        }
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        //return token & member

        MemberDTO memberDTO = new MemberDTO(
                member.getId(),
                member.getName(),
                member.getNic(),
                member.getPhone(),
                member.getEmail(),
                member.getRegNo(),
                member.getAddress(),
                member.getPhotoUrl(),
                member.getDegree(),
                member.getCompany(),
                member.getPosition(),
                member.getLinkedinUrl(),
                member.getGithubUrl(),
                member.getWebsiteUrl(),
                member.getBatch()
        );

        return new LoginResponse(
                jwt.generateToken(email),
                memberDTO
        );
    }

}
