package de.synyx.google.calendar.internal.model;

import de.synyx.google.calendar.api.model.Attendee;
import de.synyx.google.calendar.api.model.Event;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author clausen - clausen@synyx.de
 */
public final class DefaultEvent implements Event {

    private ZonedDateTime start;
    private ZonedDateTime end;

    private String name;
    private String description;
    private String location;

    private List<Attendee> attendees;

    private DefaultEvent () {}

    @Override
    public final String name () {
        return name;
    }

    @Override
    public final String description () {
        return description;
    }

    @Override
    public final String location () {
        return location;
    }

    @Override
    public final Stream<Attendee> attendees () {
        return attendees.stream ();
    }

    @Override
    public final ZonedDateTime start () {
        return start;
    }

    @Override
    public final Optional<ZonedDateTime> end () {
        return Optional.ofNullable (end);
    }

    @Override
    public String toString () {
        return "Event{" +
                "start=" + start +
                ", end=" + end +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", attendees=" + attendees +
                '}';
    }

    public final static class Builder implements Supplier<Event> {

        private ZonedDateTime start = null;
        private ZonedDateTime end   = null;

        private String name;
        private String description = "";
        private String location    = "";

        private List<Attendee> attendees = new ArrayList<> ();

        public final Builder name (String name) {
                   this.name = name;
            return this;
        }

        public final Builder description (String description) {
                   this.description = Objects.toString (description, "");
            return this;
        }

        public final Builder location (String location) {
                   this.location = Objects.toString (location, "");
            return this;
        }

        public final Builder start (ZonedDateTime start) {
                   this.start = start;
            return this;
        }

        public final Builder end (ZonedDateTime end) {
                   this.end = end;
            return this;
        }

        @Override
        public final Event get () {
            DefaultEvent event = new DefaultEvent ();
                         event.name        = Objects.requireNonNull (name);
                         event.start       = start;
                         event.end         = end;
                         event.description = description;
                         event.location    = location;
//                         event.attendees   = Collections.unmodifiableList (new ArrayList<> (attendees));

            return event;
        }

    }

}
