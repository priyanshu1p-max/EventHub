package com.flowtech.eventhub_spring_backend.record;

import java.time.LocalDate;

public record UserProfileResponse(
		
		Integer id,
	    String name,
	    String email,
	    Long phone,
	    LocalDate createdAt,
	    String roleName,

	    String profileImageUrl,
	    String gender,
	    LocalDate dateOfBirth,
	    Long alternatePhone,

	    String address,
	    String country,
	    String state,
	    String city,
	    String postalCode,

	    String bio,

	    String organisationName,
	    String organisationType,
	    String organisationDescription,
	    String website,
	    String businessEmail,
	    String businessPhone,
	    String linkedinUrl,
	    String instagramUrl,
	    String facebookUrl
		
		) {

}
