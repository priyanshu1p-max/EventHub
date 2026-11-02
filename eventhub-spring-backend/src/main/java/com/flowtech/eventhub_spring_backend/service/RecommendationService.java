package com.flowtech.eventhub_spring_backend.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.dto.EventResponse;
import com.flowtech.eventhub_spring_backend.dto.RecommendationResponse;
import com.flowtech.eventhub_spring_backend.entity.Booking;
import com.flowtech.eventhub_spring_backend.entity.CustomerPreference;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.SavedEvent;
import com.flowtech.eventhub_spring_backend.enums.EventVerification;
import com.flowtech.eventhub_spring_backend.repository.BookingRepository;
import com.flowtech.eventhub_spring_backend.repository.CustomerPreferenceRepository;
import com.flowtech.eventhub_spring_backend.repository.EventRepository;
import com.flowtech.eventhub_spring_backend.repository.SavedEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

	
	private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final CustomerPreferenceRepository preferenceRepository;
    private final SavedEventRepository savedEventRepository;

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(String email) {

        // 1. Get customer preferences
        CustomerPreference preference =
                preferenceRepository.findByUser_Email(email).orElse(null);

        // 2. Get customer's booking history
        List<Booking> bookings =
                bookingRepository.findByUser_EmailOrderByBookingDateTimeDesc(email);

        // 3. Get customer's saved events
        List<SavedEvent> savedEvents =
                savedEventRepository.findByUser_EmailOrderBySavedAtDesc(email);

        // Categories from previous bookings, excluding cancelled bookings
        Set<String> bookedCategories = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getBookingStatus()))
                .map(b -> b.getEvent().getCategory())
                .filter(category -> category != null)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Categories from saved events
        Set<String> savedCategories = savedEvents.stream()
                .map(s -> s.getEvent().getCategory())
                .filter(category -> category != null)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // 4. Get all approved events
        List<Event> events =
                eventRepository.findByStatus(EventVerification.APPROVED);

        // 5. Filter and calculate recommendation score
        List<RecommendationResponse> recommendations = new ArrayList<>();

        for (Event event : events) {

            // Do not recommend past events
            if (event.getEventDateTime() == null ||
                    !event.getEventDateTime().isAfter(LocalDateTime.now())) {
                continue;
            }

            // Apply preference filters
            if (!matchesPreferences(event, preference)) {
                continue;
            }

            RecommendationResponse response =
                    calculateScore(
                            event,
                            preference,
                            bookedCategories,
                            savedCategories);

            recommendations.add(response);
        }

        // Highest recommendation score first
        recommendations.sort(
                (a, b) -> Integer.compare(b.getScore(), a.getScore()));

        return recommendations;
    }

    // Check budget, group size and preferred days
    private boolean matchesPreferences(
            Event event, CustomerPreference preference) {

        if (preference == null) {
            return true;
        }

        // Budget is treated as maximum ticket price per person
        if (preference.getBudget() != null &&
                event.getTicketPrice() > preference.getBudget()) {
            return false;
        }

        // Check available tickets for the requested group size
        if (preference.getGroupSize() != null &&
                event.getAvailableTickets() < preference.getGroupSize()) {
            return false;
        }

        // Check customer's preferred days
        List<String> availableDays = preference.getAvailableDays();

        if (availableDays != null && !availableDays.isEmpty()) {

            String eventDay = event.getEventDateTime()
                    .getDayOfWeek().name();

            boolean matchingDay = availableDays.stream()
                    .anyMatch(day ->
                            day.equalsIgnoreCase(eventDay) ||
                            (day.equalsIgnoreCase("Weekdays") &&
                             !event.getEventDateTime().getDayOfWeek()
                                     .equals(DayOfWeek.SATURDAY) &&
                             !event.getEventDateTime().getDayOfWeek()
                                     .equals(DayOfWeek.SUNDAY)) ||
                            (day.equalsIgnoreCase("Weekend") &&
                             (eventDay.equals("SATURDAY") ||
                              eventDay.equals("SUNDAY"))));

            if (!matchingDay) {
                return false;
            }
        }

        return true;
    }

    // Calculate recommendation score
    private RecommendationResponse calculateScore(
            Event event,
            CustomerPreference preference,
            Set<String> bookedCategories,
            Set<String> savedCategories) {

        int score = 0;
        List<String> reasons = new ArrayList<>();

        String category = event.getCategory() == null
                ? ""
                : event.getCategory().toLowerCase();

        // A. Interest match: 30 points
        if (preference != null &&
                preference.getInterests() != null &&
                preference.getInterests().stream()
                        .anyMatch(i -> i.equalsIgnoreCase(category))) {

            score += 30;
            reasons.add("Matches your interests");
        }

        // B. Budget match: 20 points
        if (preference != null && preference.getBudget() != null) {

            score += 20;
            reasons.add("Within your ticket budget");
        }

        // C. Distance match: maximum 15 points
        if (preference != null &&
                preference.getLatitude() != null &&
                preference.getLongitude() != null &&
                event.getLatitude() != null &&
                event.getLongitude() != null) {

            double distance = calculateDistance(
                    preference.getLatitude(),
                    preference.getLongitude(),
                    event.getLatitude(),
                    event.getLongitude());

            if (distance <= 5) {
                score += 15;
                reasons.add("Very close to your location");
            } else if (distance <= 15) {
                score += 10;
                reasons.add("Within 15 km of your location");
            } else if (distance <= 30) {
                score += 5;
                reasons.add("Within 30 km of your location");
            }
        }

        // D. Preferred date match: 15 points
        if (preference == null ||
                preference.getAvailableDays() == null ||
                preference.getAvailableDays().isEmpty() ||
                matchesPreferences(event, preference)) {

            score += 15;
            reasons.add("Matches your available days");
        }

        // E. Previous booking category: 10 points
        if (!category.isEmpty() && bookedCategories.contains(category)) {

            score += 10;
            reasons.add("Similar to events you booked before");
        }

        // F. Saved event category: 10 points
        if (!category.isEmpty() && savedCategories.contains(category)) {

            score += 10;
            reasons.add("Similar to events you saved");
        }

        RecommendationResponse response = new RecommendationResponse();

        response.setEvent(EventResponse.from(event));
        response.setScore(score);
        response.setReasons(reasons);

        return response;
    }

    // Calculate straight-line distance using Haversine formula
    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2) {

        final int EARTH_RADIUS = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
