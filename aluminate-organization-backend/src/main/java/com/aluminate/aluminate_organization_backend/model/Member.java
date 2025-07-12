package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder

public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String nic;
    private String phone;
    private String email;
    private String regNo;
    private String address;
    private String photoUrl;
    private String password;
    private String degree;
    private String company;
    private String position;
    private int batch;

    /** One member can be a member of multiple groups */
    @OneToMany(mappedBy = "member")
    private Set<MemberGroup> memberGroups = new HashSet<>();

    /** One member can be a member of multiple events */
    @OneToMany(mappedBy = "member")
    private Set<MemberEvent> memberEvents = new HashSet<>();
}
