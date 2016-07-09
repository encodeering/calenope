package de.synyx.google.calendar.internal.service;

import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.CalendarResource;
import com.google.api.services.admin.directory.model.CalendarResources;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import de.synyx.google.calendar.api.model.Calendar;
import de.synyx.google.calendar.api.model.Event;
import de.synyx.google.calendar.api.service.Board;
import de.synyx.google.calendar.api.service.Query;
import de.synyx.google.calendar.internal.GoogleApi;
import de.synyx.google.calendar.internal.model.DefaultCalendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author clausen - clausen@synyx.de
 */
public final class DefaultBoard implements Board {

    private final Predicate<CalendarResource> room;

    private final com.google.api.services.calendar.Calendar calendar;
    private final com.google.api.services.admin.directory.Directory directory;

    public DefaultBoard (GoogleApi api, Predicate<CalendarResource> room) throws IOException {
        this.room = Objects.requireNonNull (room);

        calendar  = api.calendar ();
        directory = api.directory ();
    }

    @Override
    public final Stream<Calendar> all () {
        try {
            return Stream.concat (
                    byCalendar (),
                    byDirectory ()
            ).distinct ();
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

    @Override
    public final Optional<Calendar> name (String name) {
        try {
            return byCalendar ().filter (c -> c.name ().equals (name)).findFirst ();
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

    @Override
    public final Optional<Calendar> room (String name) {
        try {
            return byDirectory ().filter (c -> c.name ().equals (name)).findFirst ();
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

    protected final Stream<Calendar> byDirectory () throws IOException {
        Directory.Resources.Calendars.List list = directory.resources ().calendars ().list ("my_customer");

        List<CalendarResource> resources = new ArrayList<> ();

        String token = null;

        do {
            CalendarResources calendars = list.setPageToken (token).execute ();
            resources.addAll (calendars.getItems ());

                 token = calendars.getNextPageToken ();
        } while (token != null);

        return resources.stream ().filter (room).map (r ->
                new DefaultCalendar.Builder ()
                    .name  (       r.getResourceName ())
                    .query (query (r.getResourceEmail ()))
                        .get ()
        );
    }

    protected final Stream<Calendar> byCalendar () throws IOException {
        com.google.api.services.calendar.Calendar.CalendarList.List list = calendar.calendarList ().list ();

        List<CalendarListEntry> resources = new ArrayList<> ();

        String token = null;

        do {
            CalendarList calendars = list.setPageToken (token).execute ();
            resources.addAll (calendars.getItems ());

                 token = calendars.getNextPageToken ();
        } while (token != null);

        return resources.stream ().map (r ->
                new DefaultCalendar.Builder ()
                        .name  (       r.getId ())
                        .query (query (r.getId ()))
                            .get ()
        );
    }

    private Query<Event> query (String name) {
        return new DefaultQuery (() -> {
            try {
                return calendar.events ().list (name);
            } catch (IOException e) {
                throw new RuntimeException (e);
            }
        });
    }

}
