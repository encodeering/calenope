package de.synyx.google.calendar.internal.model;

import de.synyx.google.calendar.api.model.Attendee;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author clausen - clausen@synyx.de
 */
public final class DefaultAttendee implements Attendee {

    private String name;
    private String email;

    private DefaultAttendee () {}

    @Override
    public final String name () {
        return name;
    }

    @Override
    public final String email () {
        return email;
    }

    @Override
    public String toString () {
        return "Attendee{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public final static class Builder implements Supplier<Attendee> {

        private String name;
        private String email;

        public final Builder name (String name) {
                   this.name = name;
            return this;
        }

        public final Builder email (String email) {
                   this.email = email;
            return this;
        }

        @Override
        public final Attendee get () {
            DefaultAttendee attendee = new DefaultAttendee ();
                            attendee.name  = Objects.requireNonNull (name);
                            attendee.email = Objects.requireNonNull (email);
            return attendee;
        }

    }

}
