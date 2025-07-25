package com.aluminate.aluminate_organization_backend.service.announcement;

import com.aluminate.aluminate_organization_backend.dto.announcement.AnnouncementRequest;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GroupsRepository groupsRepository;
    @Autowired
    private EmailService emailService;

    public int sendEmailAnnouncement(AnnouncementRequest announcementRequest) {
        if (!announcementRequest.isSendEmail()) return 0;

        List<String> emails = List.of();
        if ("all".equals(announcementRequest.getRecipients())){
            emails = memberRepository.findAllEmails();
            System.out.println("Sending email to all members: " + emails.size() + " emails found.");
            System.out.println("emails: " + emails);
        }
        for(String email: emails) {
            emailService.sendEmail(email, announcementRequest.getTitle(), announcementRequest.getMessage());
        }
        return emails.size();
    }
}
