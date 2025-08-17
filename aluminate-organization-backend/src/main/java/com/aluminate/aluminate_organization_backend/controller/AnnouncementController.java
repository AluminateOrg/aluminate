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
@RequestMapping("${api.prefix}")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    // ADMIN ONLY
    @PostMapping("/admin/announcement/multicast-for-all-emails")
    public ResponseEntity<Map<String, Object>> sendEmailAnnouncement(@RequestBody AnnouncementRequest request) {
        int sendCount = 0;
        Map<String, Object> response = new HashMap<>();
        if ("all".equals(request.getRecipients())){
            if (request.isSendEmail()) {
                sendCount = announcementService.sendEmailAnnouncement(request);
                response.put("message", "Email announcement sent successfully");
                response.put("sendCount", sendCount);
            }
            if (request.isSendPush()) {
                sendCount = announcementService.sendNotificationToAllMembers(request);
                response.put("message", "Push notification sent successfully");
                response.put("sendCount", sendCount);
            }
            return ResponseEntity.ok(response);
        } else if ("groups".equals(request.getRecipients())) {
            if (request.isSendEmail()) {
                sendCount = announcementService.sendEmailToSelectedGroups(request);
                response.put("message", "Email announcement sent successfully");
                response.put("sendCount", sendCount);
            }

            if (request.isSendPush()) {
                sendCount = announcementService.sendNotificationToSelectedGroups(request);
                response.put("message", "Push notification sent successfully");
                response.put("sendCount", sendCount);
            }

            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid recipients");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
