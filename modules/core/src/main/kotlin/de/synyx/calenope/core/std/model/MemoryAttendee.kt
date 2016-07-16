package de.synyx.calenope.core.std.model

import de.synyx.calenope.core.api.model.Attendee

/**
 * @author clausen - clausen@synyx.de
 */
data class MemoryAttendee(

    private val name  : String,
    private val email : String?

) : Attendee {

    override fun name  () : String = name
    override fun email () : String = email ?: ""

}
