package de.synyx.calenope.core.api.service

import de.synyx.calenope.core.api.model.Event
import org.joda.time.Instant
import java.util.*

/**
 * @author clausen - clausen@synyx.de
 */
interface Query {

    fun between  (start : Instant, end : Instant, zone : TimeZone) : Collection<Event>

    fun starting (start : Instant, zone : TimeZone) : Collection<Event>

}
