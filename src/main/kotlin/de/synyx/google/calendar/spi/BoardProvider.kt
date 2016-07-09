package de.synyx.google.calendar.spi

import de.synyx.google.calendar.api.service.Board

/**
 * @author clausen - clausen@synyx.de
 */

interface BoardProvider {

    fun create (meta : Map<String, Any>, detector : (String) -> Boolean) : Board?

}
