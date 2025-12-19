package com.aluminate.aluminate_organization_backend.service.members;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplSelfProfileTest {

    @Mock MemberRepository memberRepository;
    @Mock MemberGroupRepository memberGroupRepository;
    @Mock GroupsRepository groupsRepository;
    @Mock EmailService emailService;

    @InjectMocks MemberServiceImpl service;

    private Member member;

    @BeforeEach
    void setUp() {
        // Fake authentication with email as username
        User principal = new User("hashir.zuhair@gmail.com", "Hashir123",
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities()));
        SecurityContextHolder.setContext(context);

        // Sample member entity from DB
        member = Member.builder()
                .id(1L)
                .name("Hashir")
                .nic("200226702938")
                .phone("+94-77-000-0000")
                .email("hashir.zuhair@gmail.com")
                .regNo("REG-001")
                .address("Colombo")
                .photoUrl("https://cdn.example.com/old.png")
                .degree("B.A.")
                .company("WIS")
                .position("Teacher")
                .linkedinUrl("https://linkedin.com/in/hashir")
                .githubUrl("https://github.com/hashir")
                .websiteUrl("https://hi.example.com")
                .batch(2020)
                .isActive(true)
                .build();

        given(memberRepository.findByEmail("hashir.zuhair@gmail.com")).willReturn(Optional.of(member));
        given(memberGroupRepository.findByMember(member)).willReturn(List.of()); // no groups for simplicity
        given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyProfile_mapsFields() {
        MemberResponseDTO dto = service.getMyProfile();

        assertThat(dto.getEmail()).isEqualTo("hashir.zuhair@gmail.com");
        assertThat(dto.getName()).isEqualTo("Hashir");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getPhotoUrl()).isEqualTo("https://cdn.example.com/old.png");
    }

    @Test
    void putMyProfile_updatesEditableFields() {
        MemberRequestDTO req = new MemberRequestDTO();
        // Assuming your MemberRequestDTO has setters (Lombok @Data typical):
        req.setName("Hashir Ahamed");
        req.setPhone("+94-77-123-4567");
        req.setAddress("Colombo 7");

        MemberResponseDTO updated = service.putMyProfile(req);

        assertThat(member.getName()).isEqualTo("Hashir Ahamed");
        assertThat(member.getPhone()).isEqualTo("+94-77-123-4567");
        assertThat(member.getAddress()).isEqualTo("Colombo 7");
        assertThat(updated.getName()).isEqualTo("Hashir Ahamed");
        verify(memberRepository).save(member);
    }

    @Test
    void patchMyProfile_updatesOnlyProvidedFields() {
        MemberRequestDTO req = new MemberRequestDTO();
        req.setAddress("New Address");

        MemberResponseDTO updated = service.patchMyProfile(req);

        assertThat(member.getName()).isEqualTo("Hashir"); // unchanged
        assertThat(member.getAddress()).isEqualTo("New Address");
        assertThat(updated.getAddress()).isEqualTo("Colombo"); // mapper returns original? (depends)
        // NOTE: If your mapper returns post-save state, adjust expectation accordingly.
        verify(memberRepository).save(member);
    }

    @Test
    void setMyAvatarUrl_setsPhotoUrl() {
        service.setMyAvatarUrl("https://cdn.example.com/new.png");
        assertThat(member.getPhotoUrl()).isEqualTo("https://cdn.example.com/new.png");
        verify(memberRepository).save(member);
    }

    @Test
    void getMyProfile_throwsWhenNoAuth() {
        SecurityContextHolder.clearContext();
        assertThrows(ResourceNotFoundException.class, () -> service.getMyProfile());
    }
}
