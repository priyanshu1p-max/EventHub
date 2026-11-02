package com.flowtech.eventhub_spring_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NearbyEventResponse {

	private EventResponse event;

    private double distanceKm;
}
