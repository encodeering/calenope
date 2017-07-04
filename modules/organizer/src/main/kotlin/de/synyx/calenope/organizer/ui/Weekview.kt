package de.synyx.calenope.organizer.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v7.app.AppCompatActivity
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.Interact
import de.synyx.calenope.organizer.Interaction
import java.util.Locale


/**
 * @author clausen - clausen@synyx.de
 */

class Weekview : AppCompatActivity () {

    private lateinit var layout : WeekviewLayout

    private val questions = mutableMapOf<Int, MutableList<(String) -> Unit>> ()

    override fun onCreate (bundle : Bundle?) {
        super.onCreate    (bundle)

        layout = WeekviewLayout (this)

        setContentView (layout)
    }

    override fun onDestroy () {
        try {
            layout.close ()
        } finally {
            super.onDestroy ()
        }
    }

    override fun onBackPressed () {
        val interaction = Application.store.state.events.interaction

        when (interaction) {
            is Interaction.Read -> return super.onBackPressed ()
            else -> Application.dispatch (Interact (Interaction.Read))
        }
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        questions.remove (requestCode)?.apply {
            if (           data != null && resultCode == RESULT_OK) {
                val text = data.getStringArrayListExtra (RecognizerIntent.EXTRA_RESULTS)[0]

                forEach { it (text) }
            }
        }
    }

    fun ask (prompt : String, code : Int, callback : (String) -> Unit) {
        questions.getOrPut (code) { mutableListOf<(String) -> Unit> () }.add (callback)

        val intent = Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra (RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra (RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault ())
        intent.putExtra (RecognizerIntent.EXTRA_PROMPT, prompt)

        try {
            startActivityForResult (intent, code)
        } catch (a : ActivityNotFoundException) {

        }

    }
}
