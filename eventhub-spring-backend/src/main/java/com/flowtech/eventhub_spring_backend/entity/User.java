
package com.flowtech.eventhub_spring_backend.entity;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "user")
    @SequenceGenerator(
        sequenceName = "user",
        name = "user_seq",
        initialValue = 5001
    )
    private Integer id;

    // Existing account details
    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private Long phone;

    @CreationTimestamp
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "role-id")
    private Role role;

    // NEW: Personal profile details
    private String profileImageUrl;

    private String gender;

    private LocalDate dateOfBirth;

    private Long alternatePhone;

    // NEW: Address details
    private String address;

    private String country;

    private String state;

    private String city;

    private String postalCode;

    // NEW: General profile information
    @Column(length = 1000)
    private String bio;

    // NEW: Organiser-only information
    private String organisationName;

    private String organisationType;

    @Column(length = 1000)
    private String organisationDescription;

    private String website;

    private String businessEmail;

    private String businessPhone;

    private String linkedinUrl;

    private String instagramUrl;

    private String facebookUrl;
}
