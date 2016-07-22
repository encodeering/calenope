package de.synyx.calenope.organizer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import de.synyx.calenope.organizer.R

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity (), Overview.Interaction {

    companion object {
        private val TAG = Main::class.java.name
    }

    override fun onCreate (savedInstanceState : Bundle?) {
        super.onCreate    (savedInstanceState)

        setContentView (R.layout.main)
    }

    override fun onOverviewClick (name : String) {
        Log.d (TAG, "Clicked on $name")
    }

}
