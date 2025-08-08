package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.group.GroupJoinRequest;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GroupsRepository groupsRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberGroupRepository memberGroupRepository;

    private Groups testGroup;
    private Member testMember;

    @BeforeEach
    void setUp() {
        // Create test member
        testMember = Member.builder()
                .name("Test Member")
                .email("test@example.com")
                .build();
        memberRepository.save(testMember);

        // Create test group
        testGroup = Groups.builder()
                .name("Test Group")
                .description("Test Description")
                .maxMembers(10)
                .currentMembers(0)
                .requiredApproval(false)
                .build();
        groupsRepository.save(testGroup);
    }

    @Test
    void joinGroup_WhenApprovalNotRequired_ShouldSucceed() throws Exception {
        GroupJoinRequest request = new GroupJoinRequest();
        request.setMemberId(testMember.getId());
        request.setGroupId(testGroup.getId());

        mockMvc.perform(post("/api/v1/group/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Join request processed successfully!"))
                .andExpect(jsonPath("$.data.currentMembers").value(1));
    }

    @Test
    void joinGroup_WhenApprovalRequired_ShouldCreatePendingRequest() throws Exception {
        // Update group to require approval
        testGroup.setRequiredApproval(true);
        groupsRepository.save(testGroup);

        GroupJoinRequest request = new GroupJoinRequest();
        request.setMemberId(testMember.getId());
        request.setGroupId(testGroup.getId());

        mockMvc.perform(post("/api/v1/group/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Join request processed successfully!"))
                .andExpect(jsonPath("$.data.currentMembers").value(0));
    }

    @Test
    void approveJoinRequest_ShouldSucceed() throws Exception {
        // Create a pending join request first
        testGroup.setRequiredApproval(true);
        groupsRepository.save(testGroup);

        GroupJoinRequest request = new GroupJoinRequest();
        request.setMemberId(testMember.getId());
        request.setGroupId(testGroup.getId());

        // Create pending request
        mockMvc.perform(post("/api/v1/group/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Approve the request
        mockMvc.perform(put("/api/v1/group/{groupId}/approve/{memberId}",
                        testGroup.getId(), testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Join request approved successfully!"))
                .andExpect(jsonPath("$.data.currentMembers").value(1));
    }

    @Test
    void rejectJoinRequest_ShouldSucceed() throws Exception {
        // Create a pending join request first
        testGroup.setRequiredApproval(true);
        groupsRepository.save(testGroup);

        GroupJoinRequest request = new GroupJoinRequest();
        request.setMemberId(testMember.getId());
        request.setGroupId(testGroup.getId());

        // Create pending request
        mockMvc.perform(post("/api/v1/group/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Reject the request
        mockMvc.perform(put("/api/v1/group/{groupId}/reject/{memberId}",
                        testGroup.getId(), testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Join request rejected successfully!"))
                .andExpect(jsonPath("$.data.currentMembers").value(0));
    }
}
