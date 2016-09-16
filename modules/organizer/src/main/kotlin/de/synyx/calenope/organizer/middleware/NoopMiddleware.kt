package de.synyx.calenope.organizer.middleware

import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import trikita.jedux.Store

/**
 * @author clausen - clausen@synyx.de
 */

class NoopMiddleware (dispatch : (Action) -> Unit) : Middleware (dispatch) {

    override fun dispatch (store : Store<Action, State>, action : Action, next : Store.NextDispatcher<Action>) = next.dispatch (action)

}
