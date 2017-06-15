package de.synyx.calenope.organizer

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast

/**
 * @author clausen - clausen@synyx.de
 */

fun Context.toast        (message : Int, duration : Int = Toast.LENGTH_SHORT) {
    Toast.makeText (this, message,       duration).show ()
}

fun Context.toast        (message : CharSequence, duration : Int = Toast.LENGTH_SHORT) {
    Toast.makeText (this, message,                duration).show ()
}

fun View.color (resource : Int) = ContextCompat.getColor (context, resource)
