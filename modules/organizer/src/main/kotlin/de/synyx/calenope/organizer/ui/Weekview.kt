package de.synyx.calenope.organizer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.Interact
import de.synyx.calenope.organizer.Interaction

/**
 * @author clausen - clausen@synyx.de
 */

class Weekview : AppCompatActivity () {

    private lateinit var layout : WeekviewLayout

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
}
