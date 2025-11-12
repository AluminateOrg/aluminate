package com.aluminate.aluminate_organization_backend.service;


import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Mentor;
import com.aluminate.aluminate_organization_backend.model.Organization;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailService {


    private final JavaMailSender mailSender;

    public EmailService (JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendWelcomeEmail(String to, String name, String nic) {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Welcome to Aluminate");
            helper.setText("Hello " + name + ",\n\nYour account has been created.\nNIC: "
                    + nic + "\nPassword: your nic number  \n\nThanks,\nAluminate Team");
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Error sending email");
            throw new RuntimeException(e);
        }
    }

    public String getHtmlTemplates(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void sendMentorApplicationEmail(Member member, Organization organization, Mentor mentor) {
        try {
            String template = getHtmlTemplates("templates/mentor/mentor-application.html");
            String skills = joinSet(mentor.getSkills());
            String languages = joinSet(mentor.getLanguages());

            String html = template
                    .replace("{{organization.name}}", safe(organization.getOrganizationName()))
                    .replace("{{organization.website}}", safe(organization.getOrganizationName()))
                    .replace("{{member.name}}", safe(member.getName()))
                    .replace("{{application.date}}", LocalDate.now().toString())
                    .replace("{{member.email}}", safe(member.getEmail()))
                    .replace("{{mentor.yearsOfExperience}}", safe(mentor.getYearsOfExperience()))
                    .replace("{{mentor.skills}}", skills)
                    .replace("{{mentor.languages}}", languages)
                    .replace("{{mentor.hourlyRate}}", safe(mentor.getHourlyRate()))
                    .replace("{{organization.supportEmail}}", safe(organization.getAdmin().getEmail()))
                    .replace("{{organization.address}}", safe(organization.getAdmin().getEmail()));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(member.getEmail());
            helper.setSubject("We received your mentor application" + organization.getOrganizationName());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String joinSet(Set<String> set) {
        if (set == null || set.isEmpty()) return "N/A";
        return set.stream()
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    private String safe(Object v) {
        return v == null ? "N/A" : String.valueOf(v);
    }

    public void sendMentorApprovalEmail(Member member, Organization organization) {
        try {
            String template = getHtmlTemplates("templates/mentor/mentor-approval.html");
            String html = template
                    .replace("{{organization.name}}", safe(organization.getOrganizationName()))
                    .replace("{{member.name}}", safe(member.getName()))
                    .replace("{{member.email}}", safe(member.getEmail()))
                    .replace("{{approval.date}}", LocalDate.now().toString())
                    .replace("{{organization.supportEmail}}", safe(organization.getAdmin().getEmail()))
                    .replace("{{organization.website}}", safe(organization.getOrganizationName()));
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(member.getEmail());
            helper.setSubject("Your mentor application has been approved - " + organization.getOrganizationName());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMentorRejectionEmail(Member member, Organization organization) {
        try {
            String template = getHtmlTemplates("templates/mentor/mentor-rejection.html");
            String html = template
                    .replace("{{organization.name}}", safe(organization.getOrganizationName()))
                    .replace("{{member.name}}", safe(member.getName()))
                    .replace("{{decision.date}}", LocalDate.now().toString())
                    .replace("{{rejection.reason}}", "After careful consideration, we regret to inform you that your application does not meet our current requirements.")
                    .replace("{{organization.supportEmail}}", safe(organization.getAdmin().getEmail()))
                    .replace("{{organization.website}}", safe(organization.getOrganizationName()));
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(member.getEmail());
            helper.setSubject("Your mentor application has been rejected - " + organization.getOrganizationName());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e)  {
            throw new RuntimeException(e);
        }
    }

    public void sendDeactiveEmailForMentor(Member member, Organization organization) {
        try {
            String template = getHtmlTemplates("templates/mentor/mentor-deactive.html");
            String html = template
                    .replace("{{organization.name}}", safe(organization.getOrganizationName()))
                    .replace("{{member.name}}", safe(member.getName()))
                    .replace("{{deactivation.date}}", LocalDate.now().toString())
                    .replace("{{deactivation.reason}}", "Please be informed that your mentor account")
                    .replace("{{organization.supportEmail}}", safe(organization.getAdmin().getEmail()))
                    .replace("{{organization.website}}", safe(organization.getOrganizationName()));
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(member.getEmail());
            helper.setSubject("Your mentor account has been deactivated - " + organization.getOrganizationName());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
