package de.synyx.google.calendar.core.std.model

import de.synyx.google.calendar.core.api.model.Attendee
import de.synyx.google.calendar.core.api.model.Event
import org.joda.time.Instant

/**
 * @author clausen - clausen@synyx.de
 */
data class MemoryEvent(

        private val id          : String,
        private val title       : String,
        private val description : String? = "",
        private val location    : String? = "",
        private val start       : Instant,
        private val end         : Instant?,
        private val creator     : Attendee,
        private val attendees   : Collection<Attendee>? = emptyList ()

) : Event {

    override fun id ()          : String = id
    override fun title ()       : String = title

    override fun description () : String = description ?: ""
    override fun location ()    : String = location    ?: ""

    override fun start ()       : Instant = start
    override fun end ()         : Instant? = end

    override fun creator ()     : Attendee = creator

    override fun attendees ()   : Collection<Attendee> = attendees ?: emptyList ()

}
