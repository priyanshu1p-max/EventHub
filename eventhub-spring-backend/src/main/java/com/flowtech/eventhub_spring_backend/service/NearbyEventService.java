package com.flowtech.eventhub_spring_backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.dto.EventResponse;
import com.flowtech.eventhub_spring_backend.dto.NearbyEventResponse;
import com.flowtech.eventhub_spring_backend.entity.CustomerPreference;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.enums.EventVerification;
import com.flowtech.eventhub_spring_backend.repository.CustomerPreferenceRepository;
import com.flowtech.eventhub_spring_backend.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NearbyEventService {

	 private final EventRepository eventRepository;
	    private final CustomerPreferenceRepository preferenceRepository;

	    @Transactional(readOnly = true)
	    public List<NearbyEventResponse> findNearbyEvents(
	            String email, double radiusKm) {

	        // Validate radius
	        if (!Double.isFinite(radiusKm) || radiusKm <= 0) {
	            throw new IllegalArgumentException(
	                    "Radius must be greater than zero."
	            );
	        }

	        // Get customer's saved location
	        CustomerPreference preference =
	                preferenceRepository.findByUser_Email(email)
	                        .orElseThrow(() -> new IllegalArgumentException(
	                                "Please save your location preferences first."
	                        ));

	        Double customerLat = preference.getLatitude();
	        Double customerLon = preference.getLongitude();

	        if (customerLat == null || customerLon == null
	                || !Double.isFinite(customerLat)
	                || !Double.isFinite(customerLon)
	                || customerLat < -90 || customerLat > 90
	                || customerLon < -180 || customerLon > 180) {

	            throw new IllegalArgumentException(
	                    "Please provide valid latitude and longitude in your preferences."
	            );
	        }

	        // Fetch approved events
	        List<Event> events =
	                eventRepository.findByStatus(EventVerification.APPROVED);

	        // Calculate distance and keep events within radius
	        return events.stream()
	                .filter(event ->
	                        event.getLatitude() != null
	                        && event.getLongitude() != null
	                        && Double.isFinite(event.getLatitude())
	                        && Double.isFinite(event.getLongitude())
	                        && event.getLatitude() >= -90
	                        && event.getLatitude() <= 90
	                        && event.getLongitude() >= -180
	                        && event.getLongitude() <= 180
	                )
	                .map(event -> {
	                    double distance = calculateDistance(
	                            customerLat,
	                            customerLon,
	                            event.getLatitude(),
	                            event.getLongitude()
	                    );

	                    NearbyEventResponse response =
	                            new NearbyEventResponse();

	                    response.setEvent(EventResponse.from(event));
	                    response.setDistanceKm(
	                            Math.round(distance * 10.0) / 10.0
	                    );

	                    return response;
	                })
	                .filter(item -> item.getDistanceKm() <= radiusKm)
	                .sorted(Comparator.comparingDouble(
	                        NearbyEventResponse::getDistanceKm
	                ))
	                .toList();
	    }

	    // Haversine formula: calculate distance between two coordinates
	    private double calculateDistance(
	            double lat1, double lon1,
	            double lat2, double lon2) {

	        final double EARTH_RADIUS_KM = 6371.0;

	        double latDifference = Math.toRadians(lat2 - lat1);
	        double lonDifference = Math.toRadians(lon2 - lon1);

	        double a =
	                Math.sin(latDifference / 2)
	                * Math.sin(latDifference / 2)
	                + Math.cos(Math.toRadians(lat1))
	                * Math.cos(Math.toRadians(lat2))
	                * Math.sin(lonDifference / 2)
	                * Math.sin(lonDifference / 2);

	        double c = 2 * Math.atan2(
	                Math.sqrt(a),
	                Math.sqrt(1 - a)
	        );

	        return EARTH_RADIUS_KM * c;
	    }
}
