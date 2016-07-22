package de.synyx.calenope.organizer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import de.synyx.calenope.organizer.R
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity (), Overview.Interaction {

    companion object {
        private val TAG = Main::class.java.name
    }

    private var subscription : Subscription? = null

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        setContentView (R.layout.main)

        subscription = CompositeSubscription (bindOverview (
            Observable.from (
                listOf (
                    listOf (
                        "Nullpointer",
                        "MemoryLeak"
                    )
                )
            )
        ))
    }

    override fun onDestroy () {
        try {
            super.onDestroy ()
        } finally {
            subscription?.unsubscribe ()
            subscription = null
        }
    }

    override fun onOverviewClick (name : String) {
        Log.d (TAG, "Clicked on $name")
    }

    private fun bindOverview (source : Observable<Collection<String>>) : Subscription? {
        return source.subscribeOn (Schedulers.io ()).observeOn (AndroidSchedulers.mainThread ()).subscribe ({
            val overview = fragmentManager.findFragmentById (R.id.overview)

            if (overview is Overview) {
                overview.update (it)
            }
        })
    }

}
