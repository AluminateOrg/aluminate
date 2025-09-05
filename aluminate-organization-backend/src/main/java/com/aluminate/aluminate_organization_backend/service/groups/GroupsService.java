package com.aluminate.aluminate_organization_backend.service.groups;

import com.aluminate.aluminate_organization_backend.dto.group.GroupJoinRequest;
import com.aluminate.aluminate_organization_backend.dto.group.GroupResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.group.PendingRequestDTO;
import com.aluminate.aluminate_organization_backend.model.GroupJoinRequestStatus;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.dto.group.CreateGroupRequest;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupsService implements IGroupsService {
    private final GroupsRepository groupsRepository;
    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;

    public GroupsService(GroupsRepository groupsRepository,
                         MemberGroupRepository memberGroupRepository,
                         MemberRepository memberRepository ) {
        this.groupsRepository = groupsRepository;
        this.memberGroupRepository = memberGroupRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public Groups createGroup(CreateGroupRequest group) {
        if (groupsRepository.existsByName(group.getName())) {
            throw new IllegalArgumentException("Group with this name already exists!");
        }
        Groups newGroup = Groups.builder()
                .name(group.getName())
                .description(group.getDescription())
                .category(group.getCategory())
                .maxMembers(group.getMaxMembers())
                .requiredApproval(group.isRequiredApproval())
                .build();
        try {
            return groupsRepository.save(newGroup);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A group with this name already exists!", e);
        }
    }

    @Override
    public List<GroupResponseDTO> getAllGroups() {
        return groupsRepository.findAll().stream()
                .filter(group -> !group.isDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public long getGroupCount() {
        // Call on the instance, not statically
        return groupsRepository.count();
    }

    private GroupResponseDTO convertToDTO(Groups group) {
        return GroupResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .maxMembers(group.getMaxMembers())
                .currentMembers(group.getCurrentMembers())
                .createdDate(group.getCreatedDate())
                .isActive(group.isActive())
                .isDeleted(group.isDeleted())
                .deletedAt(group.getDeletedAt())
                .category(group.getCategory())
                .requiredApproval(group.isRequiredApproval())
                .build();
    }

    @Override
    public GroupResponseDTO deactivateGroup(Long id) {
        Groups group = groupsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));

        if (!group.isActive()) {
            throw new IllegalStateException("Group is already deactivated");
        }

        group.setActive(false);
        Groups updatedGroup = groupsRepository.save(group);
        return convertToDTO(updatedGroup);
    }

    @Override
    public GroupResponseDTO toggleGroupStatus(Long id) {
        Groups group = groupsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));

        // Toggle the active status
        group.setActive(!group.isActive());
        Groups updatedGroup = groupsRepository.save(group);
        return convertToDTO(updatedGroup);
    }

    @Override
    @Transactional
    public GroupResponseDTO deleteGroup(Long id) {
        Groups group = groupsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));

        // Check if group is already deleted
        if (group.isDeleted()) {
            throw new IllegalStateException("Group is already deleted");
        }

        // Perform soft delete
        group.setDeleted(true);
        group.setActive(false);  // Deactivate the group as well
        group.setDeletedAt(LocalDateTime.now());

        // Save the updated group
        Groups deletedGroup = groupsRepository.save(group);
        return convertToDTO(deletedGroup);
    }

    @Override
    @Transactional
    public GroupResponseDTO joinGroup(GroupJoinRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Groups group = groupsRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Check if member is already in the group
        boolean isMemberInGroup = memberGroupRepository.existsByMemberAndGroup(member, group);
        if (isMemberInGroup) {
            throw new IllegalStateException("Member is already in this group");
        }

        // Check if group is full
        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            throw new IllegalStateException("Group has reached maximum capacity");
        }

        MemberGroup memberGroup = new MemberGroup();
        memberGroup.setMember(member);
        memberGroup.setGroup(group);
        memberGroup.setRole("MEMBER");
        memberGroup.setRequestDate(LocalDateTime.now());

        if (group.isRequiredApproval()) {
            memberGroup.setRequestStatus(GroupJoinRequestStatus.PENDING);
        } else {
            memberGroup.setRequestStatus(GroupJoinRequestStatus.APPROVED);
            memberGroup.setResponseDate(LocalDateTime.now());
            group.setCurrentMembers(group.getCurrentMembers() + 1);
            groupsRepository.save(group);
        }

        memberGroupRepository.save(memberGroup);
        return convertToDTO(group);
    }

    @Override
    @Transactional
    public GroupResponseDTO approveJoinRequest(Long groupId, Long memberId) {
        MemberGroup memberGroup = memberGroupRepository.findByMember_IdAndGroup_Id(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (memberGroup.getRequestStatus() != GroupJoinRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in pending state");
        }

        Groups group = memberGroup.getGroup();
        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            throw new IllegalStateException("Group has reached maximum capacity");
        }

        memberGroup.setRequestStatus(GroupJoinRequestStatus.APPROVED);
        memberGroup.setResponseDate(LocalDateTime.now());
        memberGroupRepository.save(memberGroup);

        group.setCurrentMembers(group.getCurrentMembers() + 1);
        groupsRepository.save(group);

        return convertToDTO(group);
    }

    @Override
    @Transactional
    public GroupResponseDTO rejectJoinRequest(Long groupId, Long memberId) {
        MemberGroup memberGroup = memberGroupRepository.findByMember_IdAndGroup_Id(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (memberGroup.getRequestStatus() != GroupJoinRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in pending state");
        }

        memberGroup.setRequestStatus(GroupJoinRequestStatus.REJECTED);
        memberGroup.setResponseDate(LocalDateTime.now());
        memberGroupRepository.save(memberGroup);

        return convertToDTO(memberGroup.getGroup());
    }

    @Override
    @Transactional
    public GroupResponseDTO leaveGroup(Long groupId, Long memberId) {
        MemberGroup memberGroup = memberGroupRepository.findByMember_IdAndGroup_Id(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Member is not in this group"));

        if (memberGroup.getRole().equals("ADMIN")) {
            throw new IllegalStateException("Admin cannot leave the group");
        }

        if (memberGroup.getRequestStatus() == GroupJoinRequestStatus.APPROVED) {
            Groups group = memberGroup.getGroup();
            group.setCurrentMembers(group.getCurrentMembers() - 1);
            groupsRepository.save(group);
        }

        memberGroupRepository.delete(memberGroup);
        return convertToDTO(memberGroup.getGroup());
    }

    @Override
    public List<PendingRequestDTO> getPendingRequests() {
        return memberGroupRepository.findByRequestStatus(GroupJoinRequestStatus.PENDING)
                .stream()
                .map(memberGroup -> {
                    PendingRequestDTO dto = new PendingRequestDTO();
                    dto.setRequestId(memberGroup.getId());
                    dto.setMemberId(memberGroup.getMember().getId());
                    dto.setGroupId(memberGroup.getGroup().getId());
                    dto.setMemberName(memberGroup.getMember().getName());
                    dto.setGroupName(memberGroup.getGroup().getName());
                    dto.setRequestDate(memberGroup.getRequestDate());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupResponseDTO> getGroupsByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        List<MemberGroup> memberships = memberGroupRepository.findByMember_IdAndRequestStatus(
                memberId, GroupJoinRequestStatus.APPROVED);

        return memberships.stream()
                .map(mg -> convertToDTO(mg.getGroup()))
                .collect(Collectors.toList());
    }

}
