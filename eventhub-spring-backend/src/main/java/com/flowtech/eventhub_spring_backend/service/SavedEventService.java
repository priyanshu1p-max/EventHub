package com.flowtech.eventhub_spring_backend.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.SavedEvent;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.enums.EventVerification;
import com.flowtech.eventhub_spring_backend.repository.EventRepository;
import com.flowtech.eventhub_spring_backend.repository.SavedEventRepository;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavedEventService {

	private final SavedEventRepository savedEventRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Save an event to customer favourites
    @Transactional
    public SavedEvent saveEvent(Integer eventId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NoSuchElementException("Customer not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NoSuchElementException("Event not found"));

        // Only approved events can be saved
        if (event.getStatus() != EventVerification.APPROVED) {
            throw new IllegalArgumentException(
                    "Only approved events can be saved");
        }

        // Prevent duplicate saved events
        if (savedEventRepository.existsByUser_EmailAndEvent_Id(
                email, eventId)) {
            throw new IllegalArgumentException(
                    "Event is already saved");
        }

        SavedEvent savedEvent = new SavedEvent();
        savedEvent.setUser(user);
        savedEvent.setEvent(event);

        return savedEventRepository.save(savedEvent);
    }

    // Get all saved events of a customer
    @Transactional(readOnly = true)
    public List<SavedEvent> getSavedEvents(String email) {

        return savedEventRepository
                .findByUser_EmailOrderBySavedAtDesc(email);
    }

    // Remove an event from customer favourites
    @Transactional
    public void removeSavedEvent(Integer eventId, String email) {

        SavedEvent savedEvent = savedEventRepository
                .findByUser_EmailAndEvent_Id(email, eventId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Saved event not found"));

        savedEventRepository.delete(savedEvent);
    }
}
