package com.flowtech.eventhub_spring_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.flowtech.eventhub_spring_backend.dto.UserProfileRequest;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.record.UserProfileResponse;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;
import java.io.IOException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final UserRepository userRepository;

	private final EventImageStorageService imageStorageService;

	@Transactional(readOnly = true)
	public UserProfileResponse getMyProfile(String email) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		return convertToResponse(user);
	}

	@Transactional
	public UserProfileResponse updateMyProfile(String email, UserProfileRequest request) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		// Personal information
		user.setName(request.getName());
		user.setPhone(request.getPhone());
		user.setGender(request.getGender());
		user.setDateOfBirth(request.getDateOfBirth());
		user.setAlternatePhone(request.getAlternatePhone());

		// Address information
		user.setAddress(request.getAddress());
		user.setCountry(request.getCountry());
		user.setState(request.getState());
		user.setCity(request.getCity());
		user.setPostalCode(request.getPostalCode());

		// General information
		user.setBio(request.getBio());

		// Organiser information
		// Only organiser accounts can update these fields.
		String roleName = user.getRole().getName();

		if ("ROLE_ORGANISER".equalsIgnoreCase(roleName) || "ORGANISER".equalsIgnoreCase(roleName)) {

			user.setOrganisationName(request.getOrganisationName());
			user.setOrganisationType(request.getOrganisationType());
			user.setOrganisationDescription(request.getOrganisationDescription());
			user.setWebsite(request.getWebsite());
			user.setBusinessEmail(request.getBusinessEmail());
			user.setBusinessPhone(request.getBusinessPhone());
			user.setLinkedinUrl(request.getLinkedinUrl());
			user.setInstagramUrl(request.getInstagramUrl());
			user.setFacebookUrl(request.getFacebookUrl());
		}

		User savedUser = userRepository.save(user);

		return convertToResponse(savedUser);
	}

	private UserProfileResponse convertToResponse(User user) {

		return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
				user.getCreatedAt(), user.getRole() != null ? user.getRole().getName() : null,

				user.getProfileImageUrl(), user.getGender(), user.getDateOfBirth(), user.getAlternatePhone(),

				user.getAddress(), user.getCountry(), user.getState(), user.getCity(), user.getPostalCode(),

				user.getBio(),

				user.getOrganisationName(), user.getOrganisationType(), user.getOrganisationDescription(),
				user.getWebsite(), user.getBusinessEmail(), user.getBusinessPhone(), user.getLinkedinUrl(),
				user.getInstagramUrl(), user.getFacebookUrl());
	}

	@Transactional
	public UserProfileResponse updateProfileImage(String email, MultipartFile image) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		if (image == null || image.isEmpty()) {
			throw new RuntimeException("Please select an image");
		}

		// Allow only image files
		String contentType = image.getContentType();

		if (contentType == null || !contentType.startsWith("image/")) {
			throw new RuntimeException("Only image files are allowed");
		}

		// Maximum image size: 5 MB
		if (image.getSize() > 5 * 1024 * 1024) {
			throw new RuntimeException("Image size must not exceed 5 MB");
		}

		try {
			// Store image
			String imageUrl = imageStorageService.storeImage(image);

			user.setProfileImageUrl(imageUrl);

			User savedUser = userRepository.save(user);

			return convertToResponse(savedUser);

		} catch (IOException e) {
			throw new RuntimeException("Failed to upload profile image", e);
		}
	}

}
