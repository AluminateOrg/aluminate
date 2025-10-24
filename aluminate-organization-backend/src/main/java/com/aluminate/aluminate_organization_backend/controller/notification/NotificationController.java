package com.aluminate.aluminate_organization_backend.controller.notification;

import com.aluminate.aluminate_organization_backend.model.InAppNotification;
import com.aluminate.aluminate_organization_backend.repository.InAppNotificationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
public class NotificationController {
    private final InAppNotificationRepository repo;
    public NotificationController(InAppNotificationRepository repo){ this.repo = repo; }

    @GetMapping("/member/notifications/{memberId}")
    public List<InAppNotification> list(@PathVariable String memberId) {
        return repo.findAllByMember(memberId);
    }

    @GetMapping("/member/notifications/{memberId}/unread-count")
    public long unreadCount(@PathVariable String memberId) {
        return repo.countUnread(memberId);
    }

    @PostMapping("/member/notifications/{id}/read")
    public void markRead(@PathVariable Long id) {
        repo.findById(id).ifPresent(n -> { n.setReadFlag(true); repo.save(n); });
    }

    @PostMapping("/member/notifications/mark-all-read/{memberId}")
    public void markAllRead(@PathVariable String memberId) {
        repo.findAllByMember(memberId).forEach(n -> { n.setReadFlag(true); repo.save(n); });
    }
}
