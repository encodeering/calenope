package de.synyx.google.calendar.internal.model;

import de.synyx.google.calendar.api.model.Calendar;
import de.synyx.google.calendar.api.model.Event;
import de.synyx.google.calendar.api.service.Query;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author clausen - clausen@synyx.de
 */
public final class DefaultCalendar implements Calendar {

    private String name;
    private Query<Event> query;

    private DefaultCalendar () {}

    @Override
    public final String name () {
        return name;
    }

    @Override
    public final Query<Event> query () {
        return query;
    }

    @Override
    public String toString () {
        return "Calendar{" +
                "name='" + name + '\'' +
                ", query=" + query +
                '}';
    }

    public final static class Builder implements Supplier<Calendar> {

        private String name;

        private Query<Event> query;

        public final Builder name (String name) {
                   this.name = name;
            return this;
        }

        public final Builder query (Query<Event> query) {
                   this.query = query;
            return this;
        }

        @Override
        public final Calendar get () {
            DefaultCalendar calendar = new DefaultCalendar ();
                            calendar.name = Objects.requireNonNull (name);
                            calendar.query = Objects.requireNonNull (query);

            return calendar;
        }

    }

}
