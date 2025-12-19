package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.config.JwtAuthenticationFilter;
import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.service.members.IMemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ don’t run real security filters
@TestPropertySource(properties = "api.prefix=/api/v1")
class ProfileControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private IMemberService memberService;

    // ✅ mock the filter so Spring doesn’t try to build the real one
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MemberResponseDTO sampleProfile() {
        return MemberResponseDTO.builder()
                .name("Hashir Ahamed")
                .nic("200226702938")
                .isActive(true)
                .phone("+94-77-123-4567")
                .email("hashir.zuhair@gmail.com")
                .regNo("REG-001")
                .address("Colombo 7")
                .photoUrl("https://cdn.example.com/a.png")
                .degree("B.A.")
                .company("WIS")
                .position("English Teacher")
                .linkedinUrl("https://linkedin.com/in/azra")
                .githubUrl("https://github.com/azra")
                .websiteUrl("https://hi.example.com")
                .batch(2020)
                .groupIds(List.of(1L, 2L))
                .password(null)
                .build();
    }

    private MemberResponseDTO sampleProfileAfterPut() {
        return MemberResponseDTO.builder()
                .name("Hashir Ahamed")
                .nic("200226702938")
                .isActive(true)
                .phone("+94-77-123-4567")
                .email("hashir.zuhair@gmail.com")
                .regNo("REG-001")
                .address("Colombo 7")
                .photoUrl("https://cdn.example.com/a.png")
                .degree("B.A.")
                .company("WIS")
                .position("English Teacher")
                .linkedinUrl("https://linkedin.com/in/azra")
                .githubUrl("https://github.com/azra")
                .websiteUrl("https://hi.example.com")
                .batch(2020)
                .groupIds(List.of(1L, 2L))
                .password(null)
                .build();
    }

    private MemberResponseDTO sampleProfileAfterPatch() {
        return MemberResponseDTO.builder()
                .name("Hashir Ahamed")
                .nic("200226702938")
                .isActive(true)
                .phone("+94-77-123-4567")
                .email("hashir.zuhair@gmail.com")
                .regNo("REG-001")
                .address("New Address Only") // ✅ reflect the patch
                .photoUrl("https://cdn.example.com/a.png")
                .degree("B.A.")
                .company("WIS")
                .position("English Teacher")
                .linkedinUrl("https://linkedin.com/in/azra")
                .githubUrl("https://github.com/azra")
                .websiteUrl("https://hi.example.com")
                .batch(2020)
                .groupIds(List.of(1L, 2L))
                .password(null)
                .build();
    }

    private MemberResponseDTO sampleProfileAfterAvatarSet() {
        return MemberResponseDTO.builder()
                .name("Hashir Ahamed")
                .nic("200226702938")
                .isActive(true)
                .phone("+94-77-123-4567")
                .email("hashir.zuhair@gmail.com")
                .regNo("REG-001")
                .address("Colombo 7")
                .photoUrl("https://cdn.example.com/avatar.png") // ✅ reflect new avatar
                .degree("B.A.")
                .company("WIS")
                .position("English Teacher")
                .linkedinUrl("https://linkedin.com/in/azra")
                .githubUrl("https://github.com/azra")
                .websiteUrl("https://hi.example.com")
                .batch(2020)
                .groupIds(List.of(1L, 2L))
                .password(null)
                .build();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMyProfile_ok() throws Exception {
        given(memberService.getMyProfile()).willReturn(sampleProfile());

        mockMvc.perform(get("/api/v1/member/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Profile retrieved")))
                .andExpect(jsonPath("$.data.email", is("hashir.zuhair@gmail.com")))
                .andExpect(jsonPath("$.data._active", is(true)));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void putMyProfile_ok() throws Exception {
        given(memberService.putMyProfile(any())).willReturn(sampleProfileAfterPut());

        String body = """
          {
            "name": "Hashir Ahamed",
            "phone": "+94-77-123-4567",
            "address": "Colombo 7"
          }
        """;

        mockMvc.perform(put("/api/v1/member/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Profile updated")))
                .andExpect(jsonPath("$.data.name", is("Hashir Ahamed")))
                .andExpect(jsonPath("$.data.address", is("Colombo 7")));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void patchMyProfile_ok() throws Exception {
        given(memberService.patchMyProfile(any())).willReturn(sampleProfileAfterPatch());

        String body = """
          {
            "address": "New Address Only"
          }
        """;

        mockMvc.perform(patch("/api/v1/member/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Profile updated")))
                .andExpect(jsonPath("$.data.address", is("New Address Only")));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void setAvatarUrl_ok() throws Exception {
        doNothing().when(memberService).setMyAvatarUrl(anyString());
        given(memberService.getMyProfile()).willReturn(sampleProfileAfterAvatarSet());

        mockMvc.perform(post("/api/v1/member/profile/avatar-url")
                        .param("url", "https://cdn.example.com/avatar.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Avatar updated")))
                .andExpect(jsonPath("$.data.photoUrl", is("https://cdn.example.com/avatar.png")));
    }
}
