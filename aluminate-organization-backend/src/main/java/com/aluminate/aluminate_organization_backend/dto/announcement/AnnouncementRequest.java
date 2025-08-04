package com.aluminate.aluminate_organization_backend.dto.announcement;

import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Getter
@Setter
public class AnnouncementRequest {
    private String title;
    private String message;
    private String recipients;
    private List<Integer> selectedGroups;
    private String priority;
    private boolean sendEmail;
    private boolean sendPush;
}

