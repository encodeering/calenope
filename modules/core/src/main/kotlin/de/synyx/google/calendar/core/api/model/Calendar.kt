package de.synyx.google.calendar.core.api.model

import de.synyx.google.calendar.core.api.service.Query

/**
 * @author clausen - clausen@synyx.de
 */
interface Calendar {

    fun id ()    : String

    fun query () : Query

}
