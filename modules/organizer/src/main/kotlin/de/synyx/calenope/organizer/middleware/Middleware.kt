package de.synyx.calenope.organizer.middleware

import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.State
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import trikita.jedux.Store
import java.util.concurrent.TimeUnit

/**
 * @author clausen - clausen@synyx.de
 */

abstract class Middleware (private val dispatch : (Action) -> Unit) : Store.Middleware<Action, State> {

    fun fire (action : Action) = delay { Unit }.eventloop ().subscribe { dispatch (action) }

    fun <R> delay (delay : Long = 0, action : () -> R) = Observable.timer (delay, TimeUnit.MILLISECONDS, Schedulers.io ()).map { action () }

}

fun <T> Observable<T>.eventloop () = this.observeOn (AndroidSchedulers.mainThread ())
