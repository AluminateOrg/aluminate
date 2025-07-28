package com.aluminate.aluminate_organization_backend.service.members;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.model.Member;

import java.util.List;

public interface IMemberService {
    Member createMember(MemberRequestDTO request);
    long getMemberCount();

}
