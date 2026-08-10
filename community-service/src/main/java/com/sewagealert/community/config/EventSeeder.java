package com.sewagealert.community.config;

import com.sewagealert.community.model.Event;
import com.sewagealert.community.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * EventSeeder: Seeds a set of demo awareness events on first startup.
 * <p>
 * Only runs when the events table is empty, so it never overwrites events
 * created through the admin console. Dates are relative to "today" so the
 * seeded events always appear as upcoming.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            log.info("Events already exist. Skipping demo event seeding.");
            return;
        }

        LocalDate today = LocalDate.now();

        save("Musi River Clean-up Drive",
                "Join citizens, GHMC and partner NGOs for a morning of riverbank clean-up along the Musi. Gloves, bags and refreshments provided.",
                "Musi Riverbank, near Purana Pul", today.plusDays(14),
                "GHMC · HMWS&SB", 300);

        save("Sewage Awareness Workshop",
                "Learn how Hyderabad's sewerage network works, what should never go down the drain, and how citizens can spot early signs of blockages.",
                "GHMC Head Office, Tank Bund Road", today.plusDays(28),
                "HMWS&SB", 120);

        save("Lake Restoration Walk",
                "A guided heritage walk around the restored lakes of Hyderabad — understand the treatment, inlet control and green infrastructure keeping them alive.",
                "Durgam Cheruvu, Madhapur", today.plusDays(42),
                "Hyderabad Lake Friends", 150);

        save("Citizen Feedback Townhall",
                "An open townhall with ward-level officials to discuss sewage complaints, response times and neighbourhood hotspots. Bring your complaints!",
                "Community Hall, Secunderabad", today.plusDays(56),
                "SewageAlert + GHMC", 200);

        save("Water & Waste Poster Contest",
                "School students showcase artwork on saving water and keeping drains clean. Winning entries get featured on the SewageAlert platform.",
                "Public Library, Abids", today.plusDays(70),
                "SewageAlert", null);

        save("Sewer Safety & Worker Appreciation Day",
                "A day to honour the sanitation workers who keep Hyderabad flowing — with safety demos, health camps and a community thank-you.",
                "Charminar Maidan", today.plusDays(84),
                "GHMC Sanitation", null);

        log.info("Seeded {} demo events.", eventRepository.count());
    }

    private void save(String title, String description, String location, LocalDate eventDate,
                      String organizerName, Integer capacity) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setEventDate(eventDate);
        event.setOrganizerName(organizerName);
        event.setCapacity(capacity);
        eventRepository.save(event);
    }
}
