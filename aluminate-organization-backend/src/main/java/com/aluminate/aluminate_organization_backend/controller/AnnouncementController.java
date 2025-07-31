package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.announcement.AnnouncementRequest;
import com.aluminate.aluminate_organization_backend.service.announcement.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/announcement")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/multicast-for-all-emails")
    public ResponseEntity<Map<String, Object>> sendEmailAnnouncement(@RequestBody AnnouncementRequest request) {
        int sendCount = announcementService.sendEmailAnnouncement(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Email announcement sent successfully");
        response.put("sendCount", sendCount);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/send-for-groups")
//    public ResponseEntity<Map<String, Object>> sendForGroupAnnouncement(@RequestBody AnnouncementRequest request) {
//
//    }
}
