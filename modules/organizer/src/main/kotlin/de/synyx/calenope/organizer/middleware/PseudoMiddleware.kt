package de.synyx.calenope.organizer.middleware

import android.content.Intent
import android.util.Log
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.spi.BoardProvider
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.ui.Application
import de.synyx.calenope.organizer.ui.Settings
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.observers.Observers
import rx.schedulers.Schedulers
import trikita.jedux.Store
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class PseudoMiddleware (private val application : Application) : Middleware {

    companion object {

        private val TAG = PseudoMiddleware::class.java.name

    }

    private var board : Board? by Delegates.observable (null as Board?) { property, previous, next ->
        if (next == previous) return@observable

        fire (Action.UpdateOverview ())
    }

    private var account : String by Delegates.observable ("") { property, previous, next ->
        if (next == previous) return@observable
        if (next.isBlank ())  return@observable

        val meta = mapOf (
            "context" to application.applicationContext,
            "account" to GoogleAccountManager (application).getAccountByName (next)
        )

        board = ServiceLoader.load (BoardProvider::class.java).map { it.create (meta) }.firstOrNull ()
    }

    override fun dispatch (store : Store<Action<*>, State>, action : Action<*>, next : Store.NextDispatcher<Action<*>>) {
        when (action) {
            is Action.UpdateOverview -> return calendars (Observers.create { calendars -> next.dispatch (Action.UpdateOverview(calendars)) })
            is Action.UpdateSetting  -> action.payload.startActivity (Intent (action.payload, Settings::class.java))
            is Action.SelectCalendar -> Log.d (TAG, "Clicked on ${action.payload}")
        }

        next.dispatch (action)

        account = store.state.setting.account
    }

    private fun calendars (observer : Observer<Collection<String>>) {
        oauth (emptyList<String> ()) { all ().map { it.id () } }.observeOn (AndroidSchedulers.mainThread ()).subscribe (observer)
    }

    private fun <R> oauth (default : R? = null, command : Board.() -> R) : Observable<R> {
        return Observable.timer (0, TimeUnit.MILLISECONDS, Schedulers.io ()).map { command (board!!) }.onErrorResumeNext { e ->
            Log.e (PseudoMiddleware.TAG, e.message, e)

            when (e) {
                is UserRecoverableAuthIOException -> application.startActivity (e.intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            if (default != null) Observable.just (default)
            else
                Observable.empty<R> ()
        }
    }

}
