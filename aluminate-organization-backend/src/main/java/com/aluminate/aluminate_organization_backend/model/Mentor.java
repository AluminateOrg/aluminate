package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})
public class Mentor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;


    private int yearsOfExperience;
    private BigDecimal hourlyRate;
    private String bio;
    private String linkedInUrl;
    private String portfolioUrl;
    private String motivation;
    private boolean isApproved;
    private String availability;

    /** Mentor languages, each mentor can have multiple languages be entered from a form field */
    @ElementCollection
    @CollectionTable(name = "mentor_languages", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "language")
    @Builder.Default
    private Set<String> languages = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "mentor_skills", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "skill")
    @Builder.Default
    private Set<String> skills = new HashSet<>();

    private String preferredMenteeLevel;
    private int maxMentees;
    private Double rating;
    private int sessionCount;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
            mappedBy = "mentor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<MentorProgram> programs = new HashSet<>();

    @PreRemove
    private void preRemove() {
        this.languages.clear();
        this.skills.clear();
    }

    /** Utility methods for maintaining the relationship */
    public void addProgram(MentorProgram program) {
        programs.add(program);
        program.setMentor(this);
    }

    public void removeProgram(MentorProgram program) {
        programs.remove(program);
        program.setMentor(null);
    }

}
