package de.synyx.google.calendar.api.service

import de.synyx.google.calendar.api.model.Calendar

/**
 * @author clausen - clausen@synyx.de
 */
interface Board {

    fun all  () : Collection<Calendar>

    fun room (name: String) : Calendar?

    fun name (name: String) : Calendar? = all { name == it.id() }.firstOrNull ()

    fun all (predicate: (Calendar) -> Boolean) : Collection<Calendar> = all ().filter (predicate)

}
