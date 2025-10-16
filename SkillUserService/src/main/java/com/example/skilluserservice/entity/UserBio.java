package com.example.skilluserservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID authUserId;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String education;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bio_id")
    private Set<Skill> skills;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String jobTitle;

    @Column
    private Integer yearsOfExperience;

    @Column(length = 255)
    private String linkedInProfileUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

}
