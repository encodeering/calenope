package de.synyx.calenope.organizer

import android.content.Context
import com.encodeering.conflate.experimental.api.Action
import de.synyx.calenope.core.api.model.Event
import de.synyx.calenope.organizer.middleware.FlowMiddleware
import de.synyx.calenope.organizer.ui.Settings
import de.synyx.calenope.organizer.ui.Weekview
import org.joda.time.DateTime

/**
 * @author clausen - clausen@synyx.de
 */

data class Synchronize    (val state : State = State.Default ()) : Action

data class SynchronizeAccount (val calendars : Collection<String> = emptyList (), val synchronizing : Boolean = true) : Action

data class SynchronizeCalendar (val year : Int, val month : Int, val timestamp : DateTime = DateTime.now (), val events : Collection<Event> = emptyList (), val synchronizing : Boolean = true) : Action {

    val key :    Pair<Int, Int>
        get () = Pair (year, month)

}

data class SelectCalendar (val name : String) : Action

data class SelectCalendarFilter (val filtering : Boolean = true, val stash : Collection<String>? = null) : Action

data class Open           (override val context : Context, override val screen : Class<out Context>) : FlowMiddleware.Open, Action

data class OpenSettings   (override val context : Context) : FlowMiddleware.Open by Open (context, Settings::class.java), Action

data class OpenWeekview   (override val context : Context) : FlowMiddleware.Open by Open (context, Weekview::class.java), Action
