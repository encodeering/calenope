package de.synyx.calenope.organizer.rx

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author clausen - clausen@synyx.de
 */

fun <R> delay (delay : Long = 0, action : () -> R) = Observable.timer (delay, TimeUnit.MILLISECONDS, Schedulers.io ()).map { action () }!!

fun <T> Observable<T>.eventloop () = this.observeOn (AndroidSchedulers.mainThread ())!!
