package de.synyx.calenope.organizer.middleware

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.api.Middleware.Connection
import com.encodeering.conflate.experimental.api.Middleware.Interceptor
import com.encodeering.conflate.experimental.api.Storage
import com.google.gson.GsonBuilder
import de.synyx.calenope.organizer.Application.Companion.store
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.Synchronize
import de.synyx.calenope.organizer.toast
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * @author clausen - clausen@synyx.de
 */

class DataMiddleware (private val context : Context) : Middleware<State> {

    companion object {
        private val DATA = "data"
    }

    private val gson
        by lazy { GsonBuilder ().create () }

    private val state
        by lazy { context.getSharedPreferences ("organizer-state", Context.MODE_PRIVATE) }

    private val settings
        by lazy { context.getSharedPreferences ("organizer-settings", Context.MODE_PRIVATE) }

    override fun interceptor (connection : Middleware.Connection<State>) : Middleware.Interceptor {
        return object : Middleware.Interceptor {

            private val settingsupdate = SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
                when (key) {
                    property (R.string.account) -> launch (EmptyCoroutineContext) { connection.initial (Synchronize ()) }
                }
            }

            init {
                settings.registerOnSharedPreferenceChangeListener (settingsupdate)
            }

            suspend override fun dispatch(action : Action) {
                when (action) {
                    is Synchronize -> connection.next (action.copy (store.load ()))
                    else           -> connection.next (action)
                }

                store.save ()
            }

        }
    }

    private fun SharedPreferences.getString (id : Int, default : String? = null) : String {
        return this.getString (property     (id),      default)
    }

    private fun SharedPreferences.load () : State.Setting {
        return State.Setting (account = settings.getString (R.string.account, null))
    }

    private fun Storage<State>.load () : State {
        try {
            return gson.fromJson (this@DataMiddleware.state.getString (DATA, ""), State.Default::class.java).copy (setting = settings.load ())
        } catch (e : Exception) {
            return this.state
        }
    }

    private fun Storage<State>.save () {
        try {
            this@DataMiddleware.state.edit ().putString (DATA, gson.toJson (this.state)).commit ()
        } catch (e : Exception) {
            context.toast (R.string.save_error, duration = Toast.LENGTH_LONG)
        }
    }

    private fun property (i : Int) = context.getString (i)

}
