package de.synyx.google.calendar.internal.model

import de.synyx.google.calendar.api.model.Calendar
import de.synyx.google.calendar.api.service.Query

/**
 * @author clausen - clausen@synyx.de
 */
data class DefaultCalendar (

    private val id    : String,
    private val query : Query

) : Calendar {

    override fun id ()    : String = id
    override fun query () : Query = query

}
