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

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity () {

    companion object {
        private val TAG = Main::class.java.name

        private val REQUEST_ACCOUNT_PICKER = 1000

        private val PREF_ACCOUNT_NAME = "account-name"
    }

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        setContentView (MainLayout (this))
    }

    override fun onResume () {
        super.onResume ()

        update ()
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
        }
    }

    fun onOverviewClick (name : String) {
        Log.d (TAG, "Clicked on $name")
    }

    private fun update () = authenticate (success = { Application.login (it) })

    private fun user         (name : String?) = GoogleAccountManager (this).getAccountByName (name)

    private fun authenticate (name : String? = getPreferences (Context.MODE_PRIVATE).getString (PREF_ACCOUNT_NAME, null), success : (Account) -> Unit = {}) {
        val   account = user (name)
        when (account) {
            null -> startActivityForResult (AccountPicker.newChooseAccountIntent (null, null, arrayOf (GoogleAccountManager.ACCOUNT_TYPE), true, null, null, null, null), REQUEST_ACCOUNT_PICKER)
            else -> success (account)
        }
    }

}
