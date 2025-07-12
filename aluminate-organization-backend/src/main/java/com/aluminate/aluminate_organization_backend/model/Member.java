package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

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
}
