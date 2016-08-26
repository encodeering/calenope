package de.synyx.calenope.core.spi

import de.synyx.calenope.core.api.service.Board

/**
 * @author clausen - clausen@synyx.de
 */

interface BoardProvider {

    fun create (meta : Map<String, Any>) : Board?

}
