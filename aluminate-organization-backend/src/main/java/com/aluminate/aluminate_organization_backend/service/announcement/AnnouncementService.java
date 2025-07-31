package com.aluminate.aluminate_organization_backend.service.announcement;

import com.aluminate.aluminate_organization_backend.dto.announcement.AnnouncementRequest;
import com.aluminate.aluminate_organization_backend.model.Member;
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

//    public int sendForGroups (AnnouncementRequest announcementRequest) {
//        if (!announcementRequest.isSendEmail()) return 0;
//        List<String> members = List.of();
//        List<String> emails = List.of();
//        List<Integer> groupsIds = announcementRequest.getSelectedGroups();
//        if ("groups".equals(announcementRequest.getRecipients())){
//            for (long id: groupsIds) {
//                members = memberGroupRepository.findMembersGroupById(id);
//            }
//            for (long id: members)
//        }
//    }
}
