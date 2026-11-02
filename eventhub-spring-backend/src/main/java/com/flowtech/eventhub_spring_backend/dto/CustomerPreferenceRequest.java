package com.flowtech.eventhub_spring_backend.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerPreferenceRequest {

	// Maximum ticket budget
    @DecimalMin(value = "0.0", message = "Budget cannot be negative")
    private Double budget;

    // Number of people attending
    @Min(value = 1, message = "Group size must be at least 1")
    private Integer groupSize;

    private String preferredLocation;

    // Customer's location coordinates
    private Double latitude;
    private Double longitude;

    // Example: Music, Sports, Technology
    @Size(max = 20, message = "Maximum 20 interests allowed")
    private List<String> interests = new ArrayList<>();

    // Example: Saturday, Sunday
    private List<String> availableDays = new ArrayList<>();
}
