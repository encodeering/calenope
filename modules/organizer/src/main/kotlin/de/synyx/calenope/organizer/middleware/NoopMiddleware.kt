package de.synyx.calenope.organizer.middleware

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.api.Middleware.Connection
import com.encodeering.conflate.experimental.api.Middleware.Interceptor
import de.synyx.calenope.organizer.State

/**
 * @author clausen - clausen@synyx.de
 */

class NoopMiddleware : Middleware<State> {

    override fun interceptor (connection : Connection<State>) : Interceptor {
        return object : Interceptor {

            suspend override fun dispatch(action : Action) {
                connection.next (action)
            }

        }
    }

}
