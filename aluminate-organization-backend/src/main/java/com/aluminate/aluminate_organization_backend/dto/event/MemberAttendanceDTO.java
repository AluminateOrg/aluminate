package com.aluminate.aluminate_organization_backend.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberAttendanceDTO {
    private Long memberId;
    private boolean attending;
    private boolean setRSVP;
}