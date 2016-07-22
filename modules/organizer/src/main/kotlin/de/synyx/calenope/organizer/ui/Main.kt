package de.synyx.calenope.organizer.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.AccountPicker
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.spi.BoardProvider
import de.synyx.calenope.organizer.R
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.ReplaySubject
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity (), Overview.Interaction {

    companion object {
        private val TAG = Main::class.java.name

        private val REQUEST_ACCOUNT_PICKER = 1000
        private val REQUEST_AUTHORIZATION  = 1001

        private val PREF_ACCOUNT_NAME = "account-name"
    }

    private val overviewsource = ReplaySubject.create<Collection<String>> (1)
    private var subscription : Subscription? = null

    private var board : Board? by Delegates.observable (null as Board?) {
        property, previous, board ->
            Observable.timer (0, TimeUnit.MILLISECONDS, Schedulers.io ()).map { oauth { all ().map { it.id () } } }.subscribe (overviewsource)
    }

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        setContentView (R.layout.main)

        subscription = CompositeSubscription (bindOverview (overviewsource))
    }

    override fun onResume () {
        super.onResume ()

        update ()
    }

    override fun onDestroy () {
        try {
            super.onDestroy ()
        } finally {
            subscription?.unsubscribe ()
            subscription = null
        }
    }

    override fun onActivityResult (requestcode : Int, resultcode : Int, data : Intent?) {
        super.onActivityResult    (requestcode,       resultcode,       data)

        when (requestcode) {
            REQUEST_ACCOUNT_PICKER ->
                if (resultcode == Activity.RESULT_OK && data != null && data.extras != null) {
                    val name = data.getStringExtra (AccountManager.KEY_ACCOUNT_NAME)
                    if (name != null) {
                        val settings = getPreferences (Context.MODE_PRIVATE)
                        val editor = settings.edit ()
                            editor.putString (PREF_ACCOUNT_NAME, name)
                            editor.apply ()
                    }

                    update ()
                }

            REQUEST_AUTHORIZATION  -> if (resultcode == Activity.RESULT_OK) update ()
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

    private fun update () = authenticate (success = { board = board (it) })

    private fun user         (name : String?) = GoogleAccountManager (this).getAccountByName (name)

    private fun authenticate (name : String? = getPreferences (Context.MODE_PRIVATE).getString (PREF_ACCOUNT_NAME, null), success : (Account) -> Unit = {}) {
        val   account = user (name)
        when (account) {
            null -> startActivityForResult (AccountPicker.newChooseAccountIntent (null, null, arrayOf (GoogleAccountManager.ACCOUNT_TYPE), true, null, null, null, null), REQUEST_ACCOUNT_PICKER)
            else -> success (account)
        }
    }

    private fun board (account : Account) : Board {
        val meta = mapOf (
            "context" to applicationContext,
            "account" to account
        )

        return ServiceLoader.load (BoardProvider::class.java).map { it.create (meta) }.first ()!!
    }

    private fun <R> oauth (command : Board.() -> R) : R {
        try {
            return command (board !!)
        } catch             (e : Exception) {
            Log.e (Main.TAG, e.message, e)

            when (e) {
                is UserRecoverableAuthIOException -> startActivityForResult (e.intent, Main.REQUEST_AUTHORIZATION)
            }

            throw e
        }
    }

}
