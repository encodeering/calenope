package de.synyx.calenope.organizer.middleware

import android.content.Intent
import android.util.Log
import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.epic.Epic
import com.encodeering.conflate.experimental.epic.Story
import com.encodeering.conflate.experimental.epic.Story.Happening
import com.encodeering.conflate.experimental.epic.story.Book.anecdote
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.spi.BoardProvider
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.SynchronizeAccount
import de.synyx.calenope.organizer.SynchronizeCalendar
import de.synyx.calenope.organizer.rx.delay
import de.synyx.calenope.organizer.rx.eventloop
import io.reactivex.Observable.concat
import io.reactivex.Observable.empty
import io.reactivex.Observable.just
import org.joda.time.DateTime
import java.util.ServiceLoader
import java.util.TimeZone
import kotlin.properties.Delegates

/**
 * @author clausen - clausen@synyx.de
 */
class GoogleMiddleware private constructor (private val application : Application) {

    companion object {

        private val TAG = GoogleMiddleware::class.java.name

        fun middleware (application : Application) : Middleware<State> = GoogleMiddleware (application).run {
            Epic (
                anecdote (),
                account (),
                calendars (),
                events ()
            )
        }

    }

    private var board : Board? = null

    private var account : String by Delegates.observable ("") { _, previous, next ->
        if (next == previous) return@observable
        if (next.isBlank ())  return@observable

        val meta = mapOf (
            "context" to application.applicationContext,
            "account" to GoogleAccountManager (application).getAccountByName (next)
        )

        board = ServiceLoader.load (BoardProvider::class.java).map { it.create (meta) }.firstOrNull ()
    }

    private fun account () : Story<State>  {
        return anecdote {
            it.flatMap { (_, state) ->
                val known = account
                            account = state.setting.account

                when {
                    known != account -> just (initial (SynchronizeAccount ()))
                    else             -> empty()
                }
            }
        }
    }

    private fun calendars () : Story<State> {
        return anecdote (SynchronizeAccount::class.java) {
            it.flatMap { (action) ->
                val initial = just  (                                                        next (action.copy (calendars = emptyList ()))              )
                val request = oauth (emptyList<String> ()) { all ().map { it.id () } }.map { next (action.copy (calendars = it, synchronizing = false)) }

                concat (initial, request)
            }
        }
    }

    private fun events () : Story<State> {
        return anecdote (SynchronizeCalendar::class.java) {
            it.flatMap { (action, state) ->
                val selection = state.overview.selection ?: ""
                val yearmonth = DateTime ().withYear (action.year).withMonthOfYear (action.month)

                val      from = yearmonth.withDayOfMonth (1).withTimeAtStartOfDay ()
                val to = from.plusMonths (1)

                val initial = just  (                                                                                                                                       next (action.copy (events = emptyList ()))              )
                val request = oauth (emptyList ()) { name (selection)?.query ()?.between (from.toInstant(), to.toInstant(), TimeZone.getDefault ()) ?: emptyList () }.map { next (action.copy (events = it, synchronizing = false)) }

                concat (initial, request)
            }
        }
    }

    private fun <R> oauth (default : R? = null, command : Board.() -> R) =
        delay { command (board!!) }.onErrorResumeNext { e : Throwable ->
            Log.e (TAG, e.message, e)

            when (e) {
                is UserRecoverableAuthIOException -> application.startActivity (e.intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            if (default != null) just (default)
            else
                empty<R> ()
        }.eventloop ()
}


private fun initial (action : Action) : Happening = Happening.Initial (action)

private fun next (action : Action) : Happening = Happening.Next (action)

