package de.synyx.calenope.organizer

import trikita.jedux.Store

/**
 * @author clausen - clausen@synyx.de
 */

interface State {

    val overview : Overview

    data class Default (
        override val overview : Overview = Overview ()
    ) : State

    data class Overview (
        val calendars : Collection<String> = emptyList (),
        val selection : String? = null
    )

    companion object Reducer : Store.Reducer<Action<*>, State> {

        override fun reduce  (action : Action<*>, previous : State) : State =
            when (previous) {
                is Default -> previous.copy (overview = overview (action, previous))
                else       -> previous
            }

        private fun overview (action : Action<*>, previous : State) : Overview =
            when (action) {
                is Action.UpdateOverview -> previous.overview.copy (calendars = action.payload)
                else                     -> previous.overview
            }

    }

}
