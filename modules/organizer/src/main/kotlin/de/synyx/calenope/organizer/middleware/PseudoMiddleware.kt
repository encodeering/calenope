package de.synyx.calenope.organizer.middleware

import android.accounts.Account
import android.content.Intent
import android.util.Log
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.spi.BoardProvider
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.ui.Application
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.observers.Observers
import rx.schedulers.Schedulers
import trikita.jedux.Store
import java.util.*
import java.util.concurrent.TimeUnit

class PseudoMiddleware (private val application : Application) : Store.Middleware<Action<*>, State> {

    companion object {

        private val TAG = PseudoMiddleware::class.java.name

    }

    private var board : Board? = null

    override fun dispatch (store : Store<Action<*>, State>, action : Action<*>, next : Store.NextDispatcher<Action<*>>) {
        when (action) {
            is Action.UpdateOverview -> return calendars (Observers.create { calendars -> next.dispatch (Action.UpdateOverview(calendars)) })
            is Action.SelectAccount  -> login (GoogleAccountManager (application).getAccountByName (action.payload))
            is Action.SelectCalendar -> Log.d (TAG, "Clicked on ${action.payload}")
        }

        next.dispatch (action)
    }

    private fun calendars (observer : Observer<Collection<String>>) {
        oauth { all ().map { it.id () } }.observeOn (AndroidSchedulers.mainThread ()).subscribe (observer)
    }

    private fun login (account : Account) {
        val meta = mapOf (
            "context" to application.applicationContext,
            "account" to account
        )

        board = ServiceLoader.load (BoardProvider::class.java).map { it.create (meta) }.first ()!!
    }

    private fun <R> oauth (command : Board.() -> R) : Observable<R> {
        return Observable.timer (0, TimeUnit.MILLISECONDS, Schedulers.io ()).map { command (board!!) }.onErrorResumeNext { e ->
            Log.e (PseudoMiddleware.TAG, e.message, e)

            when (e) {
                is UserRecoverableAuthIOException -> application.startActivity (e.intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            Observable.empty<R> ()
        }
    }

}
