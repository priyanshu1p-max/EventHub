package com.flowtech.eventhub_spring_backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="customer_preferences")
public class CustomerPreference {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // One preference record belongs to one customer
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private Double budget;

    private Integer groupSize;

    private String preferredLocation;

    private Double latitude;

    private Double longitude;

    // Example: Music, Sports, Technology
    @ElementCollection
    @CollectionTable(
        name = "customer_interests",
        joinColumns = @JoinColumn(name = "preference_id")
    )
    @Column(name = "interest")
    private List<String> interests = new ArrayList<>();

    // Example: Saturday, Sunday, Weekdays
    @ElementCollection
    @CollectionTable(
        name = "customer_available_days",
        joinColumns = @JoinColumn(name = "preference_id")
    )
    @Column(name = "available_day")
    private List<String> availableDays = new ArrayList<>();
}
