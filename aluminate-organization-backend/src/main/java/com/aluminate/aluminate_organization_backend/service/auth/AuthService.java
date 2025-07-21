package com.aluminate.aluminate_organization_backend.service.auth;

import com.aluminate.aluminate_organization_backend.config.util.Jwt;
import com.aluminate.aluminate_organization_backend.model.Member;
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

    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwt.generateToken(email);
    }

}
