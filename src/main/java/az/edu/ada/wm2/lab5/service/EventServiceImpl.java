package az.edu.ada.wm2.lab5.service;

import az.edu.ada.wm2.lab5.model.Event;
import az.edu.ada.wm2.lab5.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event createEvent(Event event) {
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event updateEvent(UUID id, Event event) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        event.setId(id);
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    public Event partialUpdateEvent(UUID id, Event partialEvent) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Update only non-null fields
        if (partialEvent.getEventName() != null) {
            existingEvent.setEventName(partialEvent.getEventName());
        }
        if (partialEvent.getTags() != null && !partialEvent.getTags().isEmpty()) {
            existingEvent.setTags(partialEvent.getTags());
        }
        if (partialEvent.getTicketPrice() != null) {
            existingEvent.setTicketPrice(partialEvent.getTicketPrice());
        }
        if (partialEvent.getEventDateTime() != null) {
            existingEvent.setEventDateTime(partialEvent.getEventDateTime());
        }
        if (partialEvent.getDurationMinutes() > 0) {
            existingEvent.setDurationMinutes(partialEvent.getDurationMinutes());
        }

        return eventRepository.save(existingEvent);
    }

    // Custom methods
    @Override
    public List<Event> getEventsByTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return List.of();
        }

        String normalized = tag.trim().toLowerCase();

        return eventRepository.findAll().stream()
                .filter(e -> e != null)
                .filter(e -> e.getTags() != null && !e.getTags().isEmpty())
                .filter(e -> e.getTags().stream()
                        .filter(t -> t != null && !t.isBlank())
                        .map(t -> t.trim().toLowerCase())
                        .anyMatch(t -> t.equals(normalized)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();

        return eventRepository.findAll().stream()
                .filter(e -> e != null)
                .filter(e -> e.getEventDateTime() != null)
                .filter(e -> e.getEventDateTime().isAfter(now))
                .sorted((a, b) -> a.getEventDateTime().compareTo(b.getEventDateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("maxPrice cannot be negative");
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal tmp = minPrice;
            minPrice = maxPrice;
            maxPrice = tmp;
        }

        final BigDecimal min = minPrice;
        final BigDecimal max = maxPrice;

        return eventRepository.findAll().stream()
                .filter(e -> e != null)
                .filter(e -> e.getTicketPrice() != null)
                .filter(e -> {
                    BigDecimal price = e.getTicketPrice();
                    boolean okMin = (min == null) || price.compareTo(min) >= 0;
                    boolean okMax = (max == null) || price.compareTo(max) <= 0;
                    return okMin && okMax;
                })
                .sorted((a, b) -> a.getTicketPrice().compareTo(b.getTicketPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            LocalDateTime tmp = start;
            start = end;
            end = tmp;
        }

        final LocalDateTime s = start;
        final LocalDateTime e = end;

        return eventRepository.findAll().stream()
                .filter(ev -> ev != null)
                .filter(ev -> ev.getEventDateTime() != null)
                .filter(ev -> {
                    LocalDateTime dt = ev.getEventDateTime();
                    boolean okStart = (s == null) || !dt.isBefore(s); // dt >= start
                    boolean okEnd = (e == null) || !dt.isAfter(e);    // dt <= end
                    return okStart && okEnd;
                })
                .sorted((a, b) -> a.getEventDateTime().compareTo(b.getEventDateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public Event updateEventPrice(UUID id, BigDecimal newPrice) {
        return null;
    }

}