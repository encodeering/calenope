package de.synyx.calenope.organizer.ui

import android.accounts.Account
import android.util.Log
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.spi.BoardProvider
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.observers.Observers
import rx.schedulers.Schedulers
import trikita.anvil.Anvil
import trikita.jedux.Store
import java.util.*
import java.util.concurrent.TimeUnit
import android.app.Application as Android

/**
 * @author clausen - clausen@synyx.de
 */

class Application () : Android () {

    companion object {

        private val TAG = Application::class.java.name

        private var self : Application? = null

        fun store () : Store<Action<*>, State> = self!!.store!!

        fun calendars (observer : Observer<Collection<String>>) {
            self!!.oauth { all ().map { it.id () } }.observeOn (AndroidSchedulers.mainThread ()).subscribe (observer)
        }

        fun login (account : Account) {
            val application = self!!

            val meta = mapOf (
                "context" to application.applicationContext,
                "account" to account
            )

            application.board = ServiceLoader.load (BoardProvider::class.java).map { it.create (meta) }.first ()!!
        }

    }

    private var board : Board? = null

    private var store : Store<Action<*>, State>? = null

    override fun onCreate () {
        super.onCreate ()

        Application.self = this

        store = Store (State.Reducer, State.Default (), PseudoMiddleware ())
        store?.subscribe { Anvil.render () }
    }

    private fun <R> oauth (command : Board.() -> R) : Observable<R> {
        return Observable.timer (0, TimeUnit.MILLISECONDS, Schedulers.io ()).map { command (board!!) }.onErrorResumeNext { e ->
            Log.e (Application.TAG, e.message, e)

            when (e) {
                is UserRecoverableAuthIOException -> startActivity (e.intent)
            }

            Observable.empty<R> ()
        }
    }

    private class PseudoMiddleware : Store.Middleware<Action<*>, State> {

        override fun dispatch (store : Store<Action<*>, State>, action : Action<*>, next : Store.NextDispatcher<Action<*>>) {
            when (action) {
                is Action.UpdateOverview -> return Application.calendars (Observers.create { calendars -> next.dispatch (Action.UpdateOverview (calendars)) })
                is Action.SelectAccount  -> Application.login (GoogleAccountManager (self).getAccountByName (action.payload))
                is Action.SelectCalendar -> Log.d (TAG, "Clicked on ${action.payload}")
            }

            next.dispatch (action)
        }

    }

}
