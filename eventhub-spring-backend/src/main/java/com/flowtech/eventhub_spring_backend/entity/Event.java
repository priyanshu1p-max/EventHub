package com.flowtech.eventhub_spring_backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.flowtech.eventhub_spring_backend.enums.EventVerification;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="events")
public class Event {

	@GeneratedValue(strategy=GenerationType.IDENTITY,generator="event")
	@SequenceGenerator(sequenceName="event",name="event_seq",initialValue=87871)
	@Id
	private Integer id;
	private String name;
	private String description;
	private String location;
	
	// NEW: Used for smart event recommendations
	private String category;

	// NEW: Used to calculate distance from customer
	private Double latitude;
	private Double longitude;
	
	private LocalDateTime eventDateTime;
	
	@CreationTimestamp
	private LocalDate createdAt;
	private double ticketPrice;
	private int availableTickets;
	
	
	@Column(length = 500)
	private String imageUrl;
	
	// Store multiple image URLs for each event.
	@ElementCollection
	@CollectionTable(
	    name = "event_images",
	    joinColumns = @JoinColumn(name = "event_id")
	)
	@Column(name = "image_url", length = 500)
	private List<String> imageUrls = new ArrayList<>();
	
	@Enumerated(EnumType.STRING)
	private EventVerification status=EventVerification.PENDING;// pending, approved, rejected
	
	@ManyToOne
	@JoinColumn(name = "organiser_id")
	private User user;
	
	
}
