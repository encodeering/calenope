package de.synyx.calenope.organizer.middleware

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.GsonBuilder
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.toast
import trikita.jedux.Store

/**
 * @author clausen - clausen@synyx.de
 */

class DataMiddleware (private val context : Context, dispatch : (Action) -> Unit) : Middleware (dispatch) {

    companion object {
        private val DATA = "data"
    }

    private val gson
        by lazy { GsonBuilder ().create () }

    private val state
        by lazy { context.getSharedPreferences ("organizer-state", Context.MODE_PRIVATE) }

    private val settings
        by lazy { context.getSharedPreferences ("organizer-settings", Context.MODE_PRIVATE) }

    private val settingsupdate = SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
        when (key) {
            property (R.string.account) -> fire (Action.Synchronize ())
        }
    }

    init {
        settings.registerOnSharedPreferenceChangeListener (settingsupdate)
    }

    override fun dispatch (store : Store<Action, State>, action : Action, next : Store.NextDispatcher<Action>) {
        when (action) {
            is Action.Synchronize -> next.dispatch (action.copy (store.load ()))
            else                  -> next.dispatch (action)
        }

        store.save ()
    }

    private fun SharedPreferences.getString (id : Int, default : String? = null) : String {
        return this.getString (property     (id),      default)
    }

    private fun SharedPreferences.load () : State.Setting {
        return State.Setting (account = settings.getString (R.string.account, null))
    }

    private fun Store<Action, State>.load () : State {
        try {
            return gson.fromJson (this@DataMiddleware.state.getString (DATA, ""), State.Default::class.java).copy (setting = settings.load ())
        } catch (e : Exception) {
            return this.state
        }
    }

    private fun Store<Action, State>.save () {
        try {
            this@DataMiddleware.state.edit ().putString (DATA, gson.toJson (this.state)).commit ()
        } catch (e : Exception) {
            context.toast (R.string.save_error, duration = Toast.LENGTH_LONG)
        }
    }

    private fun property (i : Int) = context.getString (i)

}
