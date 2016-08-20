package de.synyx.calenope.organizer.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.R

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity () {

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        setContentView (MainLayout (this))
    }

    override fun onResume () {
        super.onResume ()

        update ()
    }

    private fun update () = authenticate { Application.store ().dispatch (it) }

    private fun user         (name : String?) = GoogleAccountManager (this).getAccountByName (name)

    private fun authenticate (name : String? = getSharedPreferences ("organizer-settings", Context.MODE_PRIVATE).getString (getString (R.string.account), ""), success : (Action<*>) -> Unit = {}) {
        val   account = user (name)
        when (account) {
            null -> Unit
            else -> success (Action.SelectAccount (name!!))
        }
    }

}
