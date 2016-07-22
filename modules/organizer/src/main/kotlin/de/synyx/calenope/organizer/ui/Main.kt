package de.synyx.calenope.organizer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.synyx.calenope.organizer.R

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity () {

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        setContentView (R.layout.main)
    }

}
