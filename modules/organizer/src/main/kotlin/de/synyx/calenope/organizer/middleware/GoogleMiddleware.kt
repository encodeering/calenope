package de.synyx.calenope.organizer.middleware

import android.content.Intent
import android.util.Log
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import de.synyx.calenope.core.api.model.Event
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.spi.BoardProvider
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.ui.Application
import org.joda.time.DateTime
import rx.Observable
import trikita.jedux.Store
import java.util.*
import kotlin.properties.Delegates

class GoogleMiddleware(private val application : Application) : Middleware {

    companion object {

        private val TAG = GoogleMiddleware::class.java.name

    }

    private var board : Board? by Delegates.observable (null as Board?) { property, previous, next ->
        if (next == previous) return@observable

        fire (Action.SynchronizeAccount ())
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

    override fun dispatch (store : Store<Action, State>, action : Action, next : Store.NextDispatcher<Action>) {
        when (action) {
            is Action.SynchronizeAccount -> {
                                              next.dispatch (action.copy (emptyList ()))
                return request { calendars -> next.dispatch (action.copy (calendars)) }
            }
            is Action.SynchronizeCalendar -> {
                                                                                           next.dispatch (action.copy (events = emptyList ()))
                return events (store.state.overview.selection ?: "", action.key) { list -> next.dispatch (action.copy (events = list)) }
            }
        }

        next.dispatch (action)

        account = store.state.setting.account
    }

    private fun request (observer : (Collection<String>) -> Unit) {
        oauth (emptyList<String> ()) { all ().map { it.id () } }.eventloop ().subscribe (observer)
    }

    private fun events (calendar : String, start : Pair<Int, Int>, observer : (Collection<Event>) -> Unit) {
        val yearmonth = DateTime ().withYear (start.first).withMonthOfYear (start.second)

        val      from = yearmonth.withDayOfMonth (1).withTimeAtStartOfDay ()
        val to = from.plusMonths (1)

        oauth (emptyList ()) { name (calendar)?.query ()?.between (from.toInstant(), to.toInstant(), TimeZone.getDefault ()) ?: emptyList () }.eventloop ().subscribe (observer)
    }

    private fun <R> oauth (default : R? = null, command : Board.() -> R) : Observable<R> {
        return delay { command (board!!) }.onErrorResumeNext { e ->
            Log.e (GoogleMiddleware.TAG, e.message, e)

            when (e) {
                is UserRecoverableAuthIOException -> application.startActivity (e.intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            if (default != null) Observable.just (default)
            else
                Observable.empty<R> ()
        }
    }

}
