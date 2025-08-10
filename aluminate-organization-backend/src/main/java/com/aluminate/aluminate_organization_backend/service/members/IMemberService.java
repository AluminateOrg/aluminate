package com.aluminate.aluminate_organization_backend.service.members;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.group.GroupMembershipStatusDTO;
import com.aluminate.aluminate_organization_backend.model.Member;

public interface IMemberService {

    Member createMember(MemberRequestDTO request);

    long getMemberCount();
    List<GroupMembershipStatusDTO> getMemberGroupMembershipStatuses(Long memberId);

    void deactivateMember(Long id);
}
