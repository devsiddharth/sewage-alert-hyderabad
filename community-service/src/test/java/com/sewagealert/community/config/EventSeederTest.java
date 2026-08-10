package com.sewagealert.community.config;

import com.sewagealert.community.model.Event;
import com.sewagealert.community.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * EventSeederTest: Verifies the demo event seeder only seeds when the events table is
 * empty, and that each seeded event carries the expected title/date/organizer data.
 */
class EventSeederTest {

    private EventRepository eventRepository;
    private EventSeeder seeder;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        seeder = new EventSeeder(eventRepository);
    }

    @Test
    void seedsSixEventsWhenTableIsEmpty() throws Exception {
        when(eventRepository.count()).thenReturn(0L);

        seeder.run();

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(6)).save(captor.capture());

        List<Event> saved = captor.getAllValues();
        assertThat(saved).hasSize(6);
        assertThat(saved)
                .extracting(Event::getTitle)
                .containsExactly(
                        "Musi River Clean-up Drive",
                        "Sewage Awareness Workshop",
                        "Lake Restoration Walk",
                        "Citizen Feedback Townhall",
                        "Water & Waste Poster Contest",
                        "Sewer Safety & Worker Appreciation Day");
    }

    @Test
    void seedsEventsWithFutureDatesAndValidFields() throws Exception {
        when(eventRepository.count()).thenReturn(0L);

        seeder.run();

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(6)).save(captor.capture());

        for (Event event : captor.getAllValues()) {
            assertThat(event.getTitle()).isNotBlank();
            assertThat(event.getDescription()).isNotBlank();
            assertThat(event.getLocation()).isNotBlank();
            assertThat(event.getOrganizerName()).isNotBlank();
            assertThat(event.getEventDate()).isAfterOrEqualTo(java.time.LocalDate.now());
        }
    }

    @Test
    void skipsSeedingWhenEventsAlreadyExist() throws Exception {
        when(eventRepository.count()).thenReturn(3L);

        seeder.run();

        verify(eventRepository, never()).save(any(Event.class));
    }
}
