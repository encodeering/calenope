package de.synyx.google.calendar.core.api.model

import org.joda.time.Duration
import org.joda.time.Instant

/**
 * @author clausen - clausen@synyx.de
 */
interface Event {

    fun id () : String

    fun title () : String

    fun description () : String

    fun location () : String

    fun creator () : Attendee

    fun attendees () : Collection<Attendee>

    fun start () : Instant

    fun end () : Instant?

    fun duration () : Duration = Duration (start (), end ())

}
