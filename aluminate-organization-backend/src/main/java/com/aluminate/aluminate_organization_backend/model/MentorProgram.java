package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity

public class MentorProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Many programs can be created by one mentor */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    /** Many members can join many programs */
    @ManyToMany
    @JoinTable(
            name = "program_participants",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> participants = new HashSet<>();

    private Date date;
    private Time time;
    private String programUrl;

}
