package de.synyx.calenope.organizer

import android.content.Context
import de.synyx.calenope.organizer.middleware.FlowMiddleware
import de.synyx.calenope.organizer.ui.Settings

/**
 * @author clausen - clausen@synyx.de
 */
interface Action {

    data class Synchronize    (val state : State = State.Default ()) : Action

    data class UpdateOverview (val calendars : Collection<String> = emptyList ()) : Action

    data class SelectCalendar (val name : String) : Action

    data class Open           (override val context : Context, override val screen : Class<out Context>) : FlowMiddleware.Open, Action

    data class OpenSettings   (override val context : Context) : FlowMiddleware.Open by Open (context, Settings::class.java), Action

}
