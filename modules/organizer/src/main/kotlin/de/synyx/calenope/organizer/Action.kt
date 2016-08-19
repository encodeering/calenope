package de.synyx.calenope.organizer

/**
 * @author clausen - clausen@synyx.de
 */

interface Action<out T> {

    val payload : T

    class UpdateOverview (private val calendars : Collection<String> = emptyList ()) : Action<Collection<String>>
        by Simple (calendars)

    class SelectCalendar (private val name : String) : Action<String>
        by Simple (name)

    private class Simple<out T> (override val payload : T) : Action<T>

}
