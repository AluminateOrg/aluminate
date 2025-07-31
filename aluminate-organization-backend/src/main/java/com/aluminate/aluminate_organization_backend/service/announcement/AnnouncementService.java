package com.aluminate.aluminate_organization_backend.service.announcement;

import com.aluminate.aluminate_organization_backend.dto.announcement.AnnouncementRequest;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
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
    @Autowired
    private MemberGroupRepository memberGroupRepository;


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

    //function to send email to members of selected groups
    public int sendEmailToSelectedGroups(AnnouncementRequest announcementRequest) {
        if (!announcementRequest.isSendEmail()) return 0;
        List<Long> groupIds = announcementRequest.getSelectedGroups().stream()
                .map(groupIdStr -> Long.parseLong(String.valueOf(groupIdStr)))
                .toList();
        List<MemberGroup> memberGroups = memberGroupRepository.findByGroup_IdIn(groupIds);
        if (memberGroups.isEmpty()){
            System.out.println("No members found");
            return 0;
        }
        System.out.println("Sending email to members: " + memberGroups.size() + " members found.");
        List<String> emails = memberGroups.stream()
                        .map(mg -> mg.getMember().getEmail())
                        .filter(email -> email != null && !email.isEmpty())
                        .distinct()
                        .toList();
        System.out.println("emails: " + emails);
        for(String email: emails) {
            emailService.sendEmail(email, announcementRequest.getTitle(), announcementRequest.getMessage());
        }
        return emails.size();
    }
}
