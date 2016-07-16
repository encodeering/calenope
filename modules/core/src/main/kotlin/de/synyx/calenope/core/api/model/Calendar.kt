package de.synyx.calenope.core.api.model

import de.synyx.calenope.core.api.service.Query

/**
 * @author clausen - clausen@synyx.de
 */
interface Calendar {

    fun id ()    : String

    fun query () : Query

}
