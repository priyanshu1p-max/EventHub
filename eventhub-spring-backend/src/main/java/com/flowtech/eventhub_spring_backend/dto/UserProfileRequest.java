package com.flowtech.eventhub_spring_backend.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserProfileRequest {

	 private String name;

	    private Long phone;

	    private String gender;

	    private LocalDate dateOfBirth;

	    private Long alternatePhone;

	    private String address;

	    private String country;

	    private String state;

	    private String city;

	    private String postalCode;

	    private String bio;

	    // Organiser information
	    private String organisationName;

	    private String organisationType;

	    private String organisationDescription;

	    private String website;

	    private String businessEmail;

	    private String businessPhone;

	    private String linkedinUrl;

	    private String instagramUrl;

	    private String facebookUrl;
}
