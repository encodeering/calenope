package de.synyx.calenope.organizer.component

import android.content.Context
import android.view.MotionEvent
import com.alamkanak.weekview.WeekView
import java.lang.Math.abs
import kotlin.properties.Delegates

/**
 * @author clausen - clausen@synyx.de
 */
class WeekviewTouchProxy (context : Context) : WeekView (context) {

    interface Scrolling {

        fun top (state : Boolean)

    }

    var scrolling : Scrolling? = null

    var scrollingObservable by Delegates.observable (0.0) { property, previous, next ->
        scrolling?.top (abs (next) < 0.1)
    }

    override fun onTouchEvent (event : MotionEvent) : Boolean {
        scrollingObservable = firstVisibleHour

        return super.onTouchEvent (event)
    }

}
