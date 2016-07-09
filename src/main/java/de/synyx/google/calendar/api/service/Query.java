package de.synyx.google.calendar.api.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * @author clausen - clausen@synyx.de
 */
public interface Query<T> {

    public Stream<T> between  (ZonedDateTime start, ZonedDateTime end);

    public Stream<T> starting (ZonedDateTime start);

    public default Stream<T> day (ZonedDateTime start) {
        ZonedDateTime   startofday = start.truncatedTo (ChronoUnit.DAYS);
        return between (startofday, startofday.plusDays (1));
    }

}
