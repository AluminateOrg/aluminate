package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(of = {"id"})
@Builder
public class MentorProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Many programs can be created by one mentor */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_mentorprogram_mentor",
                    foreignKeyDefinition = "FOREIGN KEY (mentor_id) REFERENCES mentor(id) ON UPDATE CASCADE ON DELETE CASCADE"
            ))
    private Mentor mentor;

    /** Many members can join many programs */
    @ManyToMany
    @JoinTable(
            name = "program_participants",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
            // set on update cascade and on delete cascade
            , foreignKey = @ForeignKey(
                    name = "fk_programparticipants_program",
                    foreignKeyDefinition = "FOREIGN KEY (program_id) REFERENCES mentor_program(id) ON UPDATE CASCADE ON DELETE CASCADE"
            ),
            inverseForeignKey = @ForeignKey(
                    name = "fk_programparticipants_member",
                    foreignKeyDefinition = "FOREIGN KEY (member_id) REFERENCES member(id) ON UPDATE CASCADE ON DELETE CASCADE"
            )
    )
    private Set<Member> participants = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private Payment payment;

    //create a field for save user that created the program
    //fpr each program there is one user that created it
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private Member createdBy;


    private LocalDate date;
    private LocalTime time;
    private LocalDateTime createdAt;
    private String programUrl;
    private String status;
    private boolean isPaid = false;
}
