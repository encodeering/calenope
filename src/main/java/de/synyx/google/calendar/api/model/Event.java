package de.synyx.google.calendar.api.model;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author clausen - clausen@synyx.de
 */
public interface Event {

    public String name ();

    public String description ();

    public String location ();

    public Stream<Attendee> attendees ();

    public ZonedDateTime start ();

    public Optional<ZonedDateTime> end ();

    public default Optional<Duration> duration () {
        return end ().map (end -> Duration.between (start (), end));
    }

}
