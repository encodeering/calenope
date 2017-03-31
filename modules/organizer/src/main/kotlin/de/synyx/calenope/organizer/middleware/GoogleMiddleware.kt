package de.synyx.calenope.organizer.middleware

import android.content.Intent
import android.util.Log
import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware.Connection
import com.encodeering.conflate.experimental.api.Middleware.Interceptor
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import de.synyx.calenope.core.api.model.Event
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.spi.BoardProvider
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.SynchronizeAccount
import de.synyx.calenope.organizer.SynchronizeCalendar
import de.synyx.calenope.organizer.rx.delay
import de.synyx.calenope.organizer.rx.eventloop
import org.joda.time.DateTime
import rx.Observable
import java.util.ServiceLoader
import java.util.TimeZone
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.properties.Delegates

class GoogleMiddleware(private val application : Application, dispatch : (Action) -> Unit) : Middleware (dispatch) {

    companion object {

        private val TAG = GoogleMiddleware::class.java.name

    }

    private var board : Board? by Delegates.observable (null as Board?) { property, previous, next ->
        if (next == previous) return@observable

        fire (SynchronizeAccount ())
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

    override fun interceptor (connection : Connection<State>) : Interceptor {
        return object : Interceptor {

            suspend override fun dispatch (action : Action) {
                when (action) {
                    is SynchronizeAccount  -> {
                                           connection.next (action.copy (emptyList ()))
                        return calendars { connection.next (action.copy (it, synchronizing = false)) }
                    }
                    is SynchronizeCalendar -> {
                                                                                                connection.next (action.copy (events = emptyList ()))
                        return events (connection.state.overview.selection ?: "", action.key) { connection.next (action.copy (events = it, synchronizing = false)) }
                    }
                }

                connection.next (action)

                account = connection.state.setting.account
            }

        }
    }

    private suspend fun calendars (observer : suspend (Collection<String>) -> Unit) {
        oauth (observer, emptyList<String> ()) {
            all ().map { it.id () }
        }
    }

    private suspend fun events (calendar : String, start : Pair<Int, Int>, observer : suspend (Collection<Event>) -> Unit) {
        val yearmonth = DateTime ().withYear (start.first).withMonthOfYear (start.second)

        val      from = yearmonth.withDayOfMonth (1).withTimeAtStartOfDay ()
        val to = from.plusMonths (1)

        oauth (observer, emptyList ()) {
            name (calendar)?.query ()?.between (from.toInstant(), to.toInstant(), TimeZone.getDefault ()) ?: emptyList ()
        }
    }

    private suspend fun <R> oauth (action : suspend (R) -> Unit, default : R? = null, command : Board.() -> R) {
        val observer = delay { command (board!!) }.onErrorResumeNext { e ->
            Log.e (GoogleMiddleware.TAG, e.message, e)

            when (e) {
                is UserRecoverableAuthIOException -> application.startActivity (e.intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            if (default != null) Observable.just (default)
            else
                Observable.empty<R> ()
        }.eventloop ()

        suspendCoroutine<Unit> { continuation ->
            observer.subscribe ({

                action.startCoroutine (it, object : Continuation<Unit> {

                    override val context = EmptyCoroutineContext

                    override fun resume (value : Unit) {
                        continuation.resume     (Unit)
                    }

                    override fun resumeWithException     (exception : Throwable) {
                        continuation.resumeWithException (exception)
                    }

                })

            }, continuation::resumeWithException)
        }
    }

}
