package de.synyx.calenope.organizer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * @author clausen - clausen@synyx.de
 */

class Weekview () : AppCompatActivity () {

    override fun onCreate (bundle : Bundle?) {
        super.onCreate    (bundle)

        setContentView (WeekviewLayout (this))
    }

}
