package de.synyx.calenope.organizer.ui

import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.middleware.DataMiddleware
import de.synyx.calenope.organizer.middleware.PseudoMiddleware
import trikita.anvil.Anvil
import trikita.jedux.Store
import android.app.Application as Android

/**
 * @author clausen - clausen@synyx.de
 */

class Application () : Android () {

    companion object {

        private var self : Application? = null

        fun store () : Store<Action<*>, State> = self!!.store!!

    }

    private var store : Store<Action<*>, State>? = null

    override fun onCreate () {
        super.onCreate ()

        Application.self = this

        store = Store (State.Reducer, State.Default (), PseudoMiddleware (this), DataMiddleware (this))
        store?.subscribe { Anvil.render () }

        store?.dispatch (Action.Synchronize ())
    }

}
