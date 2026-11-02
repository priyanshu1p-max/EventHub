package com.flowtech.eventhub_spring_backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class RecommendationResponse {

	private EventResponse event;

    // Recommendation score out of 100
    private int score;

    // Why this event is recommended
    private List<String> reasons;
}
