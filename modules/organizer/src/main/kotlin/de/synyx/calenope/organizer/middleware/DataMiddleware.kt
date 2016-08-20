package de.synyx.calenope.organizer.middleware

import android.content.Context
import android.widget.Toast
import com.google.gson.GsonBuilder
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.State
import trikita.jedux.Store

/**
 * @author clausen - clausen@synyx.de
 */

class DataMiddleware (private val context : Context) : Store.Middleware<Action<*>, State> {

    companion object {
        private val DATA = "data"
    }

    private val gson
        by lazy { GsonBuilder ().create () }

    private val state
        by lazy { context.getSharedPreferences ("organizer-state", Context.MODE_PRIVATE) }

    override fun dispatch (store : Store<Action<*>, State>, action : Action<*>, next : Store.NextDispatcher<Action<*>>) {
        when (action) {
            is Action.Synchronize -> next.dispatch (Action.Synchronize (store.load ()))
            else                  -> next.dispatch (action)
        }

        store.save ()
    }

    private fun Store<Action<*>, State>.load () : State {
        try {
            return gson.fromJson (this@DataMiddleware.state.getString (DATA, ""), State.Default::class.java)
        } catch (e : Exception) {
            return this.state
        }
    }

    private fun Store<Action<*>, State>.save () {
        try {
            this@DataMiddleware.state.edit ().putString (DATA, gson.toJson (this.state)).commit ()
        } catch (e : Exception) {
            toast (R.string.save_error)
        }
    }

    private fun toast (message : Int) {
        Toast.makeText (context, message, Toast.LENGTH_LONG).show ()
    }

}
