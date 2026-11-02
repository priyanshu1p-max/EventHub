package com.flowtech.eventhub_spring_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flowtech.eventhub_spring_backend.dto.UserProfileRequest;
import com.flowtech.eventhub_spring_backend.record.UserProfileResponse;
import com.flowtech.eventhub_spring_backend.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

	
	 private final UserProfileService userProfileService;

	    // Get logged-in user's profile
	    @GetMapping("/me")
	    public ResponseEntity<UserProfileResponse> getMyProfile(
	            Authentication authentication) {

	        UserProfileResponse profile =
	                userProfileService.getMyProfile(authentication.getName());

	        return ResponseEntity.ok(profile);
	    }

	    // Update logged-in user's profile
	    @PutMapping("/me")
	    public ResponseEntity<UserProfileResponse> updateMyProfile(
	            @RequestBody UserProfileRequest request,
	            
	            Authentication authentication) {

	        UserProfileResponse updatedProfile =
	                userProfileService.updateMyProfile(
	                        authentication.getName(),
	                        request);

	        return ResponseEntity.ok(updatedProfile);
	    }
	    
	    

@PostMapping(
    value = "/me/image",
    consumes = "multipart/form-data"
)
public ResponseEntity<UserProfileResponse> uploadProfileImage(
        @RequestPart("image") MultipartFile image,
        Authentication authentication) {

    UserProfileResponse profile =
            userProfileService.updateProfileImage(
                    authentication.getName(),
                    image
            );

    return ResponseEntity.ok(profile);
}

	    
}
