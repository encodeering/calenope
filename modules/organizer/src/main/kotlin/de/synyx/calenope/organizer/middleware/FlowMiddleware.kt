package de.synyx.calenope.organizer.middleware

import android.content.Context
import android.content.Intent
import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.api.Middleware.Connection
import com.encodeering.conflate.experimental.api.Middleware.Interceptor
import de.synyx.calenope.organizer.State

/**
 * @author clausen - clausen@synyx.de
 */
class FlowMiddleware : Middleware<State> {

    interface Open {
        val context : Context
        val screen  : Class<out Context>
    }

    override fun interceptor (connection : Connection<State>) : Interceptor {
        return object : Interceptor {

            suspend override fun dispatch(action : Action) {
                when (action) {
                    is Open -> start           (action.context, action.screen)
                    else    -> connection.next (action)
                }
            }

        }
    }

    private fun start                 (context : Context, screen : Class<out Context>) {
        context.startActivity (Intent (context,           screen))
    }

}
