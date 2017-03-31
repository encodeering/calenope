package de.synyx.calenope.organizer

import android.util.Log
import com.encodeering.conflate.experimental.android.conflate
import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Storage
import com.encodeering.conflate.experimental.logging.Logging
import de.synyx.calenope.organizer.middleware.DataMiddleware
import de.synyx.calenope.organizer.middleware.FlowMiddleware
import de.synyx.calenope.organizer.middleware.GoogleMiddleware
import de.synyx.calenope.organizer.middleware.NoopMiddleware
import trikita.anvil.Anvil
import android.app.Application as Android

/**
 * @author clausen - clausen@synyx.de
 */

class Application () : android.app.Application () {

    companion object {

        private val TAG = Application::class.java.name

        private var self : Application? = null

                val store by lazy { self?.store!! }


        val dispatch = fun (action: Action) { store.dispatcher.dispatch (action) }

    }

    lateinit var store : Storage<State>

    override fun onCreate () {
        super.onCreate ()

        self = this

        store = conflate (State.Default (), State, GoogleMiddleware (this), FlowMiddleware (), DataMiddleware (this), if (debuggable ()) Logging (log = { text, action -> Log.d (TAG, "$text $action") }) else NoopMiddleware ())
        store.subscribe { Anvil.render () }

        dispatch (Synchronize ())
    }

}
