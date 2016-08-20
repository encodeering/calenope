package de.synyx.calenope.organizer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * @author clausen - clausen@synyx.de
 */

class Settings : AppCompatActivity () {

    override fun onCreate (bundle : Bundle?) {
        super.onCreate    (bundle)

        if                (bundle != null)
            return

        fragmentManager.beginTransaction ().replace (android.R.id.content, SettingsLayout.create ("organizer-settings")).commit ()
    }

}

