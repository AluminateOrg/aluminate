package com.aluminate.aluminate_organization_backend.service.groups;

import com.aluminate.aluminate_organization_backend.dto.group.GroupJoinRequest;
import com.aluminate.aluminate_organization_backend.dto.group.GroupResponseDTO;
import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupsServiceTest {

    @Mock
    private GroupsRepository groupsRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberGroupRepository memberGroupRepository;

    @InjectMocks
    private GroupsService groupsService;

    private Member testMember;
    private Groups testGroup;
    private GroupJoinRequest joinRequest;

    @BeforeEach
    void setUp() {
        // Set up test member
        testMember = Member.builder()
                .id(1L)
                .name("Test Member")
                .email("test@example.com")
                .build();

        // Set up test group
        testGroup = Groups.builder()
                .id(1L)
                .name("Test Group")
                .maxMembers(10)
                .currentMembers(5)
                .requiredApproval(false)
                .build();

        // Set up join request
        joinRequest = new GroupJoinRequest();
        joinRequest.setMemberId(1L);
        joinRequest.setGroupId(1L);
    }

    @Test
    void joinGroup_WhenApprovalNotRequired_ShouldJoinImmediately() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(groupsRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(memberGroupRepository.existsByMemberAndGroup(testMember, testGroup)).thenReturn(false);
        when(groupsRepository.save(any(Groups.class))).thenReturn(testGroup);
        when(memberGroupRepository.save(any(MemberGroup.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        GroupResponseDTO result = groupsService.joinGroup(joinRequest);

        // Assert
        assertNotNull(result);
        assertEquals(6, testGroup.getCurrentMembers());
        verify(memberGroupRepository).save(argThat(memberGroup ->
                memberGroup.getRequestStatus() == GroupJoinRequestStatus.APPROVED));
    }

    @Test
    void joinGroup_WhenApprovalRequired_ShouldCreatePendingRequest() {
        // Arrange
        testGroup.setRequiredApproval(true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(groupsRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(memberGroupRepository.existsByMemberAndGroup(testMember, testGroup)).thenReturn(false);
        when(memberGroupRepository.save(any(MemberGroup.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        GroupResponseDTO result = groupsService.joinGroup(joinRequest);

        // Assert
        assertNotNull(result);
        assertEquals(5, testGroup.getCurrentMembers()); // Should not increment
        verify(memberGroupRepository).save(argThat(memberGroup ->
                memberGroup.getRequestStatus() == GroupJoinRequestStatus.PENDING));
    }

    @Test
    void joinGroup_WhenGroupIsFull_ShouldThrowException() {
        // Arrange
        testGroup.setCurrentMembers(testGroup.getMaxMembers());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(groupsRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(memberGroupRepository.existsByMemberAndGroup(testMember, testGroup)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> groupsService.joinGroup(joinRequest));
    }

    @Test
    void approveJoinRequest_ShouldApproveAndIncrementMembers() {
        // Arrange
        MemberGroup memberGroup = new MemberGroup();
        memberGroup.setMember(testMember);
        memberGroup.setGroup(testGroup);
        memberGroup.setRequestStatus(GroupJoinRequestStatus.PENDING);

        when(memberGroupRepository.findByMember_IdAndGroup_Id(1L, 1L))
                .thenReturn(Optional.of(memberGroup));
        when(groupsRepository.save(any(Groups.class))).thenReturn(testGroup);
        when(memberGroupRepository.save(any(MemberGroup.class))).thenReturn(memberGroup);

        // Act
        GroupResponseDTO result = groupsService.approveJoinRequest(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(6, testGroup.getCurrentMembers());
        assertEquals(GroupJoinRequestStatus.APPROVED, memberGroup.getRequestStatus());
    }

    @Test
    void rejectJoinRequest_ShouldRejectWithoutIncrementingMembers() {
        // Arrange
        MemberGroup memberGroup = new MemberGroup();
        memberGroup.setMember(testMember);
        memberGroup.setGroup(testGroup);
        memberGroup.setRequestStatus(GroupJoinRequestStatus.PENDING);

        when(memberGroupRepository.findByMember_IdAndGroup_Id(1L, 1L))
                .thenReturn(Optional.of(memberGroup));
        when(memberGroupRepository.save(any(MemberGroup.class))).thenReturn(memberGroup);

        // Act
        GroupResponseDTO result = groupsService.rejectJoinRequest(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(5, testGroup.getCurrentMembers()); // Should not increment
        assertEquals(GroupJoinRequestStatus.REJECTED, memberGroup.getRequestStatus());
    }
}
