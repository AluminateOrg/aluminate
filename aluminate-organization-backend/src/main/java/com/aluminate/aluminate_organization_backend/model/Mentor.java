package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder

public class Mentor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    private String availability;
    private int yearsOfExperience;
    private BigDecimal hourlyRate;
    private String bio;
    private String linkedInUrl;
    private String portfolioUrl;
    private String motivation;

    /** Mentor languages, each mentor can have multiple languages be entered from a form field */
    @ElementCollection
    @CollectionTable(name = "mentor_languages", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "language")
    private Set<String> languages = new HashSet<>();

    private String preferredMenteeLevel;
    private int maxMentees;
    private Double rating;
    private int sessionCount;

    @OneToMany(
            mappedBy = "mentor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<MentorProgram> programs = new HashSet<>();

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
