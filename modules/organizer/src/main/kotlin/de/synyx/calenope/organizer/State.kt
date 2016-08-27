package de.synyx.calenope.organizer

import trikita.jedux.Store

/**
 * @author clausen - clausen@synyx.de
 */

interface State {

    val overview : Overview
    val setting  : Setting

    data class Default (
        override val overview : Overview = Overview (),
        override val setting  : Setting  = Setting  ()
    ) : State

    data class Overview (
        val calendars : Collection<String> = emptyList (),
        val selection : String? = null
    )

    data class Setting (val account : String = "")

    companion object Reducer : Store.Reducer<Action, State> {

        override fun reduce  (action : Action, previous : State) : State =
            when (previous) {
                is Default -> previous.copy (overview = overview (action, previous.overview),
                                             setting  = setting  (action, previous.setting))
                else       -> previous
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
