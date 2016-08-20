package de.synyx.calenope.organizer.ui

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.google.android.gms.common.AccountPicker
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.R

class SettingsLayout () : PreferenceFragment (), SharedPreferences.OnSharedPreferenceChangeListener {

    fun Preference.update (value : String?) {
        this.summary =     value
        this.editor.putString (this.key, value).apply ()
    }

    companion object {
        private val ACCOUNT = 1000
        private val STORAGE = "storage"

        fun create (storage : String) : SettingsLayout {
            val bundle = Bundle ()
                bundle.putString (STORAGE, storage)

            val instance = SettingsLayout ()
                instance.arguments = bundle

            return instance
        }
    }

    private val store   by lazy { Application.store () }
    private val account by lazy { findPreference (getString (R.string.account)) }

    override fun onCreate (bundle : Bundle?) {
        super.onCreate    (bundle)

        preferenceManager.sharedPreferencesName = arguments.getString (STORAGE)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener (this)

        addPreferencesFromResource (R.xml.settings)

        account ()
    }

    override fun onDestroy () {
        super.onDestroy ()

        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener (this)
    }

    override fun onSharedPreferenceChanged (preferences : SharedPreferences?, key : String?) {
        findPreference (key)?.apply {
            this.onPreferenceChangeListener?.onPreferenceChange (this, preferences?.all?.get (key!!))
        }
    }

    override fun onActivityResult (requestcode : Int, resultcode : Int, data : Intent?) {
        super.onActivityResult    (requestcode,       resultcode,       data)

        when (requestcode) {
            ACCOUNT ->
                if (resultcode == Activity.RESULT_OK && data != null && data.extras != null) {
                    val name = data.getStringExtra (AccountManager.KEY_ACCOUNT_NAME)
                    if (name != null) {
                        account.update (name)
                    }
                }
        }
    }

    private fun account () {
        this.account.update (value (account.key, ""))

        this.account.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            startActivityForResult(AccountPicker.newChooseAccountIntent (null, null, arrayOf(GoogleAccountManager.ACCOUNT_TYPE), true, null, null, null, null), ACCOUNT)
            true
        }

        this.account.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, value ->
            if (value is String &&
                value.isNotEmpty ()) store.dispatch (Action.SelectAccount (value))

            true
        }
    }

    private fun value (key : String, default : String = "") = preferenceManager.sharedPreferences.getString (key, default)

}
