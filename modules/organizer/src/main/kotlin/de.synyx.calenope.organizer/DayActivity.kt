package de.synyx.calenope.organizer

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.admin.directory.Directory
import com.google.api.services.admin.directory.DirectoryScopes
import java.util.*

class DayActivity : AppCompatActivity () {

    companion object {
        private val TAG = DayActivity::class.java.name

        private val REQUEST_ACCOUNT_PICKER = 1000
        private val REQUEST_AUTHORIZATION  = 1001

        private val PREF_ACCOUNT_NAME = "account-name"
        private val SCOPES = listOf (DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY)
    }

    private var account : Account?
        get () =                 credential?.selectedAccount
        set (value : Account?) { credential?.selectedAccount = value }

    private var credential : GoogleAccountCredential? = null

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        credential = GoogleAccountCredential.usingOAuth2 (this, SCOPES).setBackOff (ExponentialBackOff ())

        setContentView (R.layout.activity_day)

        val toolbar = findViewById (R.id.toolbar) as Toolbar?
        val button  = findViewById (R.id.button)  as Button?
            button?.setOnClickListener { update () }
        val fab = findViewById (R.id.fab) as FloatingActionButton?
            fab!!.setOnClickListener { view -> Snackbar.make (view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction ("Action", null).show () }

        setSupportActionBar (toolbar)
    }

    override fun onCreateOptionsMenu (menu : Menu) : Boolean {
        menuInflater.inflate (R.menu.menu_day, menu)
        return true
    }

    override fun onOptionsItemSelected (item : MenuItem) : Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected (item)
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

                        update (name)
                    }
                }

            REQUEST_AUTHORIZATION -> if (resultcode == Activity.RESULT_OK) update ()
        }
    }

    private fun user     (name : String?) = GoogleAccountManager (this).getAccountByName (name)

    private fun identify (name : String?, action : (name : String) -> Unit) {
        when (name) {
            null -> startActivityForResult (credential?.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            else -> action (name)
        }
    }

    private fun update (name : String? = getPreferences (Context.MODE_PRIVATE).getString (PREF_ACCOUNT_NAME, null)) {
        fun execute () : Any? = Task ().execute () // account might be empty here [::identity (name)]

              account = user (name)
        when (account) {
            null -> identify (name) { account = user (it); execute () }
            else ->                                        execute ()
        }
    }

    private fun toast (message : String) {
        Toast.makeText (this, message, Toast.LENGTH_SHORT).show ()
    }

    private inner class Task (
        val year  : Int = Calendar.getInstance ().get (Calendar.YEAR),
        val month : Int = Calendar.getInstance ().get (Calendar.MONTH)
    ) : AsyncTask<Unit, Unit, List<String>> () {

        private var service : Directory? = null

        init {
            val transport   = AndroidHttp.newCompatibleTransport ()
            val jsonFactory = JacksonFactory.getDefaultInstance  ()

            service = Directory.Builder (transport, jsonFactory, credential).setApplicationName ("Directory API Android Quickstart").build ()
        }

        private val data : List<String>
            get () {
                val             result = service!!.resources ().calendars().list ("my_customer").setMaxResults (10).execute ()
                val    users  = result.items
                return users?.map { it.resourceName } ?: emptyList ()
            }

        override fun doInBackground (vararg params : Unit?) : List<String> {
            try {
                return data
            } catch        (e : Exception) {
                Log.e (TAG, e.message, e)

                when (e) {
                    is UserRecoverableAuthIOException -> startActivityForResult (e.intent, REQUEST_AUTHORIZATION)
                }
            }

            return emptyList ()
        }

        override fun onPostExecute (result : List<String>?) {
            toast ("available entries <${result?.joinToString () ?: "nothing"}>")
        }

    }
}
