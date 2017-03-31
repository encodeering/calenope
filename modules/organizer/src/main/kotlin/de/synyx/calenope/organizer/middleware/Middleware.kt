package de.synyx.calenope.organizer.middleware

import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.rx.delay
import de.synyx.calenope.organizer.rx.eventloop
import trikita.jedux.Store

/**
 * @author clausen - clausen@synyx.de
 */

abstract class Middleware (private val dispatch : (Action) -> Unit) : Store.Middleware<Action, State> {

    fun fire (action : Action) = delay { Unit }.eventloop ().subscribe { dispatch (action) }

}
