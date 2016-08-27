package de.synyx.calenope.organizer.middleware

import android.content.Context
import android.content.Intent
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import trikita.jedux.Store

/**
 * @author clausen - clausen@synyx.de
 */
class FlowMiddleware () : Middleware {

    interface Open {
        val context : Context
        val screen  : Class<out Context>
    }

    override fun dispatch (store : Store<Action, State>, action : Action, next : Store.NextDispatcher<Action>) =
        when (action) {
            is Open -> start         (action.context, action.screen)
            else    -> next.dispatch (action)
        }

    private fun start                 (context : Context, screen : Class<out Context>) {
        context.startActivity (Intent (context,           screen))
    }

}
