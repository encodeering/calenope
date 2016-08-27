package de.synyx.calenope.organizer.ui

import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.debuggable
import de.synyx.calenope.organizer.middleware.DataMiddleware
import de.synyx.calenope.organizer.middleware.GoogleMiddleware
import de.synyx.calenope.organizer.middleware.NoopMiddleware
import trikita.anvil.Anvil
import trikita.jedux.Logger
import trikita.jedux.Store
import android.app.Application as Android

/**
 * @author clausen - clausen@synyx.de
 */

class Application () : Android () {

    companion object {

        private val TAG = Application::class.java.name

        private var self : Application? = null

        fun store () : Store<Action, State> = self!!.store!!

    }

    private var store : Store<Action, State>? = null

    override fun onCreate () {
        super.onCreate ()

        Application.self = this

        store = Store (State.Reducer, State.Default (), GoogleMiddleware (this), DataMiddleware (this), if (debuggable ()) Logger (TAG) else NoopMiddleware ())
        store?.subscribe { Anvil.render () }

        store?.dispatch (Action.Synchronize ())
    }

}
