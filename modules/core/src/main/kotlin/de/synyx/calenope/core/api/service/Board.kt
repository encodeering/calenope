package de.synyx.calenope.core.api.service

import de.synyx.calenope.core.api.model.Calendar

/**
 * @author clausen - clausen@synyx.de
 */
interface Board {

    fun all  () : Collection<Calendar>

    fun name (name: String) : Calendar? = all { name == it.id() }.firstOrNull ()

    fun all (predicate: (Calendar) -> Boolean) : Collection<Calendar> = all ().filter (predicate)

}
