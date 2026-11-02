package com.flowtech.eventhub_spring_backend.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.dto.CustomerPreferenceRequest;
import com.flowtech.eventhub_spring_backend.entity.CustomerPreference;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.repository.CustomerPreferenceRepository;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerPreferenceService {

	private final CustomerPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    // Save or update customer preferences
    @Transactional
    public CustomerPreference savePreferences(
            CustomerPreferenceRequest request, String email) {

        // Find logged-in customer
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NoSuchElementException("Customer not found"));

        // Check whether preferences already exist
        CustomerPreference preference =
                preferenceRepository.findByUser_Email(email)
                        .orElseGet(CustomerPreference::new);

        // Set customer relationship
        preference.setUser(user);

        // Save preference details
        preference.setBudget(request.getBudget());
        preference.setGroupSize(request.getGroupSize());
        preference.setPreferredLocation(request.getPreferredLocation());
        preference.setLatitude(request.getLatitude());
        preference.setLongitude(request.getLongitude());
        preference.setInterests(request.getInterests());
        preference.setAvailableDays(request.getAvailableDays());

        return preferenceRepository.save(preference);
    }

    // Get preferences of logged-in customer
    @Transactional(readOnly = true)
    public CustomerPreference getPreferences(String email) {

        return preferenceRepository.findByUser_Email(email)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Customer preferences not found"));
    }
}
