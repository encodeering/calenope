package de.synyx.calenope.organizer.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.LinearLayout
import com.google.android.gms.common.AccountPicker
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import rx.Observer
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.WRAP
import trikita.anvil.BaseDSL.centerHorizontal
import trikita.anvil.BaseDSL.dip
import trikita.anvil.BaseDSL.margin
import trikita.anvil.BaseDSL.textSize
import trikita.anvil.DSL.FILL
import trikita.anvil.DSL.adapter
import trikita.anvil.DSL.button
import trikita.anvil.DSL.gridView
import trikita.anvil.DSL.horizontalSpacing
import trikita.anvil.DSL.linearLayout
import trikita.anvil.DSL.numColumns
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onItemClick
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.relativeLayout
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
import trikita.anvil.DSL.text
import trikita.anvil.DSL.textColor
import trikita.anvil.DSL.textView
import trikita.anvil.DSL.verticalSpacing
import trikita.anvil.RenderableAdapter

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity () {

    companion object {
        private val TAG = Main::class.java.name

        private val REQUEST_ACCOUNT_PICKER = 1000

        private val PREF_ACCOUNT_NAME = "account-name"
    }

    private inner class RxRenderableAdapter<T> (private val view : (value : T, position : Int) -> Unit) : RenderableAdapter (), Observer<Collection<T>> {

        private var last : Collection<T> = emptyList ()

        override fun view (index : Int) {
            view (getItem (index), index)
        }

        override fun getItem (position : Int) : T = last.elementAt (position)

        override fun getCount () : Int = last.size

        override fun onNext (t : Collection<T>) {
            last = t
            notifyDataSetChanged ()
        }

        override fun onCompleted () {
            notifyDataSetChanged ()
        }

        override fun onError (e : Throwable?) {
            last = emptyList ()
            notifyDataSetChanged ()
        }

    }

    private val tiles by lazy {
        RxRenderableAdapter<String> { item, position ->
            relativeLayout {
                size (WRAP, WRAP)
                orientation (LinearLayout.VERTICAL)

                textView {
                    size (WRAP, WRAP)
                    text (item)
                    textSize (sip (10.toFloat ()))
                    textColor (Color.BLACK)
                    centerHorizontal ()
                    margin (dip (20))
                }
            }
        }
    }

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        Anvil.mount (findViewById (android.R.id.content)) {
            linearLayout {
                size (FILL, FILL)
                orientation (LinearLayout.VERTICAL)

                button {
                    text ("Update")
                    onClick { Application.calendars (tiles) }
                }

                gridView {
                    size (FILL, FILL)
                    adapter (tiles)
                    numColumns (2)
                    horizontalSpacing (dip (0))
                    verticalSpacing   (dip (0))
                    onItemClick { adapter, view, position, id -> onOverviewClick (adapter.getItemAtPosition (position) as String? ?: "") }
                }
            }
        }
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

    private fun onOverviewClick (name : String) {
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
