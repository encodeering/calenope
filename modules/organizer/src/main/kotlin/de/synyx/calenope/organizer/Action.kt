package de.synyx.calenope.organizer

import android.content.Context

/**
 * @author clausen - clausen@synyx.de
 */

interface Action<out T> {

    val payload : T

    class Synchronize (private val state : State = State.Default ()) : Action<State>
        by Simple (state)

    class UpdateOverview (private val calendars : Collection<String> = emptyList ()) : Action<Collection<String>>
        by Simple (calendars)

    class UpdateSetting (private val context : Context) : Action<Context>
        by Simple (context)

    class SelectCalendar (private val name : String) : Action<String>
        by Simple (name)

    class SelectAccount (private val name : String) : Action<String>
        by Simple (name)

    private class Simple<out T> (override val payload : T) : Action<T>

}
