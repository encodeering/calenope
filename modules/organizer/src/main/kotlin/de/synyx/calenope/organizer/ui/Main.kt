package de.synyx.calenope.organizer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.toast

/**
 * @author clausen - clausen@synyx.de
 */
class Main : AppCompatActivity () {

    companion object {
        private val PERMISSION = 1000
    }

    override fun onCreate (bundle : Bundle?) {
        super.onCreate    (bundle)

        setContentView (MainLayout (this))
    }

    override fun onResume () {
        super.onResume ()

        authorize ()
    }

    private fun authorize () {
        if (ContextCompat.checkSelfPermission (this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale (this, Manifest.permission.GET_ACCOUNTS)) {
                toast (R.string.request_runtime_permission)
            }

            ActivityCompat.requestPermissions (this, arrayOf (Manifest.permission.GET_ACCOUNTS), PERMISSION)
        }
    }

    override fun onRequestPermissionsResult (requestcode : Int, permissions : Array<out String>, grants : IntArray) {
        super.onRequestPermissionsResult    (requestcode,       permissions,                     grants)

        when (requestcode) {
            PERMISSION -> if (grants.any { it == PackageManager.PERMISSION_GRANTED })
                Application.store ().dispatch (Action.SynchronizeAccount ())
        }
    }

}
