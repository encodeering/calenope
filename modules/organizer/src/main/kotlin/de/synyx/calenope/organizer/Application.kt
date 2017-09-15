package de.synyx.calenope.organizer

import android.os.Handler
import com.encodeering.conflate.experimental.android.conflate
import com.encodeering.conflate.experimental.android.logging
import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Storage
import de.synyx.calenope.organizer.middleware.DataMiddleware
import de.synyx.calenope.organizer.middleware.FlowMiddleware
import de.synyx.calenope.organizer.middleware.GoogleMiddleware
import trikita.anvil.Anvil
import android.app.Application as Android



/**
 * @author clausen - clausen@synyx.de
 */

class Application : android.app.Application () {

    companion object {

        private var self : Application? = null

                val store by lazy { self?.store!! }


        val dispatch = fun (action: Action) { store.dispatcher.dispatch (action) }

    }

    lateinit var store : Storage<State>

    override fun onCreate () {
        super.onCreate ()

        val handler = Handler ()

        self = this

        store = conflate (State.Default (), State, GoogleMiddleware.middleware (this), FlowMiddleware (), DataMiddleware (this), logging ())
        store.subscribe {
            handler.postDelayed (Anvil::render, 0)
        }
    }

}
