package de.synyx.calenope.organizer

import de.synyx.calenope.core.api.model.Event
import trikita.jedux.Store
import java.util.Collections.singletonMap

/**
 * @author clausen - clausen@synyx.de
 */

interface State {

    val overview : Overview
    val setting  : Setting
    val events   : Events

    data class Default (
        override val overview : Overview = Overview (),
        override val setting  : Setting  = Setting  (),
        override val events   : Events   = Events   ()
    ) : State

    data class Overview (
        val calendars : Collection<String> = emptyList (),
        val selection : String? = null
    )

    data class Setting (
        val account : String = ""
    )

    data class Events  (
        @Transient val map : Map<Pair<Int, Int>, Collection<Event>> = emptyMap ()
    )

    companion object Reducer : Store.Reducer<Action, State> {

        override fun reduce  (action : Action, previous : State) : State =
            when (previous) {
                is Default -> previous.copy (overview = overview (action, previous.overview),
                                             setting  = setting  (action, previous.setting),
                                             events   = events   (action, previous.events))
                else       -> previous
            }

        private fun events   (action : Action, events : State.Events) : Events =
            when (action) {
                is Action.Synchronize       -> action.state.events
                is Action.SynchronizeCalendar ->            events.copy (map = events.map.plus (singletonMap (action.key, action.events)))
                is Action.SelectCalendar    ->              events.copy (map = emptyMap ())
                else                        ->              events
            }

        private fun setting  (action : Action, setting : State.Setting) : Setting  =
            when (action) {
                is Action.Synchronize   -> action.state.setting
                else                    ->              setting
            }

        private fun overview (action : Action, overview : State.Overview) : Overview =
            when (action) {
                is Action.Synchronize        -> action.state.overview
                is Action.SynchronizeAccount ->              overview.copy (calendars = action.calendars)
                is Action.SelectCalendar     ->              overview.copy (selection = action.name)
                else                         ->              overview
            }

    }

}
