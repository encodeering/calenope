package de.synyx.calenope.organizer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * @author clausen - clausen@synyx.de
 */

class Weekview () : AppCompatActivity () {

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
}
