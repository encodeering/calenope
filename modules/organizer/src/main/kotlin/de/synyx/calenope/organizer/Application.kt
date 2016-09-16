package de.synyx.calenope.organizer

import de.synyx.calenope.organizer.middleware.DataMiddleware
import de.synyx.calenope.organizer.middleware.FlowMiddleware
import de.synyx.calenope.organizer.middleware.GoogleMiddleware
import de.synyx.calenope.organizer.middleware.NoopMiddleware
import trikita.anvil.Anvil
import trikita.jedux.Logger
import trikita.jedux.Store
import android.app.Application as Android

/**
 * @author clausen - clausen@synyx.de
 */

class Application () : android.app.Application () {

    companion object {

        private val TAG = Application::class.java.name

        private var self : Application? = null

        fun store () : Store<Action, State> = self!!.store!!

    }

    private var store : Store<Action, State>? = null

    override fun onCreate () {
        super.onCreate ()

        val dispatch = fun (action: Action) { store ().dispatch (action) }

        self = this

        store = Store (State, State.Default (), GoogleMiddleware (this, dispatch), FlowMiddleware (dispatch), DataMiddleware (this, dispatch), if (debuggable ()) Logger (TAG) else NoopMiddleware (dispatch))
        store?.subscribe { Anvil.render () }

        store?.dispatch (Action.Synchronize ())
    }

}
