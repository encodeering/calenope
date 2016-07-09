package de.synyx.google.calendar.api.model

import de.synyx.google.calendar.api.service.Query

/**
 * @author clausen - clausen@synyx.de
 */
interface Calendar {

    fun id ()    : String

    fun query () : Query

}
