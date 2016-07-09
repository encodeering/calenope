package de.synyx.google.calendar.api.service;

import de.synyx.google.calendar.api.model.Calendar;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author clausen - clausen@synyx.de
 */
public interface Board {

    public Stream<Calendar> all ();

    public Optional<Calendar> name (String name);

    public Optional<Calendar> room (String name);

    public default Stream<Calendar> all (Predicate<Calendar> predicate) {
        return all ().filter (Objects.requireNonNull (predicate));
    }

}
