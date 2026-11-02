package com.flowtech.eventhub_spring_backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(
	    name = "saved_events",
	    uniqueConstraints = {
	        @UniqueConstraint(
	            columnNames = {"user_id", "event_id"}
	        )
	    }
	)
public class SavedEvent {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;

	    @ManyToOne
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;

	    @ManyToOne
	    @JoinColumn(name = "event_id", nullable = false)
	    private Event event;

	    @CreationTimestamp
	    private LocalDateTime savedAt;
	
}
