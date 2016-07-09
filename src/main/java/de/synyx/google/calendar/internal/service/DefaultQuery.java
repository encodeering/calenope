package de.synyx.google.calendar.internal.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.EventDateTime;
import de.synyx.google.calendar.api.model.Event;
import de.synyx.google.calendar.api.service.Query;
import de.synyx.google.calendar.internal.model.DefaultEvent;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author clausen - clausen@synyx.de
 */
public final class DefaultQuery implements Query<Event> {

    private Supplier<Calendar.Events.List> events;

    public DefaultQuery (Supplier<com.google.api.services.calendar.Calendar.Events.List> events) {
        this.events = Objects.requireNonNull (events);
    }

    @Override
    public final Stream<Event> between (ZonedDateTime start, ZonedDateTime end) {
        return Stream.of (events.get ())
                .map (list ->
                      list.setTimeMin (datetime (start))
                          .setTimeMax (datetime (end))
                )
                .flatMap (this::extract)
                .map     (this::convert);
    }

    @Override
    public final Stream<Event> starting (ZonedDateTime start) {
        return Stream.of (events.get ())
                .map (list ->
                      list.setTimeMin (datetime (start))
                )
                .flatMap (this::extract)
                .map     (this::convert);
    }

    private Event convert (com.google.api.services.calendar.model.Event e) {
        return new DefaultEvent.Builder ()
                               .name        (e.getId ())
                               .location    (e.getLocation ())
                               .description (e.getDescription ())
                               .start (zonetime (e.getStart ()))
                               .end   (zonetime (e.getEnd   ()))
                                   .get ();
    }

    private DateTime datetime (ZonedDateTime zdt) {
        return new DateTime (
                Date.from            (zdt.toInstant ()),
                TimeZone.getTimeZone (zdt.getZone   ())
        );
    }

    private ZonedDateTime zonetime (EventDateTime time) {
        if (time == null) return null;
        System.out.println (time.getTimeZone ());
        DateTime start = time.getDateTime ();
        if (start == null) {
            start = time.getDate ();
        }

        OffsetDateTime odt = Instant.ofEpochMilli (start.getValue ()).atOffset (ZoneOffset.ofTotalSeconds (60 * start.getTimeZoneShift ()));

        System.out.println (odt.toInstant ());

        if (time.getTimeZone () == null) return null;

        System.out.println (start.getTimeZoneShift ());
        return ZonedDateTime.ofInstant (Instant.ofEpochMilli (start.getValue ()), TimeZone.getTimeZone (time.getTimeZone ()).toZoneId ());
    }

    private Stream<com.google.api.services.calendar.model.Event> extract (com.google.api.services.calendar.Calendar.Events.List list) {
        try {
            return list.execute ().getItems ().stream ();
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

}
