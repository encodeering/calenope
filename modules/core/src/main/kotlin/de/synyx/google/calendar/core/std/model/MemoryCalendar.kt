package de.synyx.google.calendar.core.std.model

import de.synyx.google.calendar.core.api.model.Calendar
import de.synyx.google.calendar.core.api.service.Query

/**
 * @author clausen - clausen@synyx.de
 */
data class MemoryCalendar(

    private val id    : String,
    private val query : Query

) : Calendar {

    override fun id ()    : String = id
    override fun query () : Query = query

}
