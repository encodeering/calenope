package de.synyx.calenope.organizer.middleware

import com.encodeering.conflate.experimental.api.Action
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.rx.delay
import de.synyx.calenope.organizer.rx.eventloop

/**
 * @author clausen - clausen@synyx.de
 */

abstract class Middleware (private val dispatch : (Action) -> Unit) : com.encodeering.conflate.experimental.api.Middleware<State> {

    fun fire (action : Action) = delay { Unit }.eventloop ().subscribe { dispatch (action) }

}
