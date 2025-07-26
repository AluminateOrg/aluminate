package com.aluminate.aluminate_organization_backend.service.groups;

import com.aluminate.aluminate_organization_backend.dto.group.GroupResponseDTO;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.dto.group.CreateGroupRequest;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupsService implements IGroupsService {
    private final GroupsRepository groupsRepository;

    public GroupsService(GroupsRepository groupsRepository) {
        this.groupsRepository = groupsRepository;
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


}
