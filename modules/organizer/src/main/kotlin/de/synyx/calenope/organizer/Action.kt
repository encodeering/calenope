package de.synyx.calenope.organizer

import android.content.Context

/**
 * @author clausen - clausen@synyx.de
 */
interface Action {

    data class Synchronize    (val state : State = State.Default ()) : Action

    data class UpdateOverview (val calendars : Collection<String> = emptyList ()) : Action

    data class UpdateSetting  (val context : Context) : Action

    data class SelectCalendar (val name : String) : Action

}
