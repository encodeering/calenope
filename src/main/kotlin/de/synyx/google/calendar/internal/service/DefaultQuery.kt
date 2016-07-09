package de.synyx.google.calendar.internal.service

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar.Events.List
import com.google.api.services.calendar.model.Event.Creator
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.Events
import de.synyx.google.calendar.api.model.Attendee
import de.synyx.google.calendar.api.model.Event
import de.synyx.google.calendar.api.service.Query
import de.synyx.google.calendar.internal.model.DefaultAttendee
import de.synyx.google.calendar.internal.model.DefaultEvent
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author clausen - clausen@synyx.de
 */
class DefaultQuery (private val events: () -> List) : Query {

    override fun between  (start: Instant, end: Instant, zone : TimeZone): Collection<Event> {
        return process (
                events ().setTimeMin (datetime (start, zone))
                         .setTimeMax (datetime (end,   zone))
                         .setSingleEvents (true)
                            .execute ()
        )
    }

    override fun starting (start: Instant, zone : TimeZone): Collection<Event> {
        return process (
                events ().setTimeMin (datetime (start, zone))
                         .setSingleEvents (true)
                            .execute ()
        )
    }

    private fun process (events : Events) : Collection<Event> = events.items.filterNot { it.start == null }.map { convert (it) }

    private fun convert (e: com.google.api.services.calendar.model.Event): Event {
        return DefaultEvent (
                id          = e.id,
                title       = e.summary,
                location    = e.location,
                description = e.description,
                start       = instant   (e.start),
                end         = instant   (e.end),
                creator     = attendee  (e.creator),
                attendees   = attendees (e.attendees)
        )
    }

    private fun datetime (instant: Instant, zone : TimeZone): DateTime {
        return DateTime  (instant.toDate (), zone)
    }

    private fun instant (time: EventDateTime): Instant {
        val start: DateTime = time.dateTime ?:
                              time.date

        val value  = start.value
        val offset = TimeUnit.MINUTES.toMillis (start.timeZoneShift.toLong ()).toInt ()

        return org.joda.time.DateTime (value, DateTimeZone.forOffsetMillis (offset)).toInstant ()
    }

    private fun attendees (attendees : Collection<EventAttendee>) : Collection<Attendee> {
        return attendees.map { attendee (it) }
    }

    private fun attendee (attendee : Any) = when (attendee) {
        is EventAttendee -> DefaultAttendee (name = attendee.displayName ?: attendee.email, email = attendee.email)
        is Creator       -> DefaultAttendee (name = attendee.displayName ?: attendee.email, email = attendee.email)
        else             -> throw UnsupportedOperationException ("$attendee unsupported")
    }

}
