package de.synyx.calenope.organizer.ui

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.LinearLayout
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.encodeering.conflate.experimental.api.Storage
import de.synyx.calenope.core.api.model.Event
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.State.Events
import de.synyx.calenope.organizer.SynchronizeCalendar
import de.synyx.calenope.organizer.color
import de.synyx.calenope.organizer.component.WeekviewTouchProxy
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.Minutes
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.MATCH
import trikita.anvil.BaseDSL.WRAP
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.enabled
import trikita.anvil.DSL.id
import trikita.anvil.DSL.imageView
import trikita.anvil.DSL.layoutParams
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.scaleType
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
import trikita.anvil.DSL.v
import trikita.anvil.RenderableView
import trikita.anvil.appcompat.v7.AppCompatv7DSL.popupTheme
import trikita.anvil.appcompat.v7.AppCompatv7DSL.toolbar
import trikita.anvil.design.DesignDSL.appBarLayout
import trikita.anvil.design.DesignDSL.collapsedTitleTextColor
import trikita.anvil.design.DesignDSL.collapsingToolbarLayout
import trikita.anvil.design.DesignDSL.coordinatorLayout
import trikita.anvil.design.DesignDSL.expanded
import trikita.anvil.design.DesignDSL.expandedTitleColor
import trikita.anvil.design.DesignDSL.title
import trikita.anvil.design.DesignDSL.titleEnabled
import trikita.anvil.support.v4.Supportv4DSL.onRefresh
import trikita.anvil.support.v4.Supportv4DSL.refreshing
import trikita.anvil.support.v4.Supportv4DSL.swipeRefreshLayout
import java.util.Calendar
import java.util.Random
import kotlin.properties.Delegates
import kotlin.properties.Delegates.vetoable

/**
 * @author clausen - clausen@synyx.de
 */
class WeekviewLayout (private val weekview : Weekview) : RenderableView (weekview), AutoCloseable {

    private val store by lazy { Application.store }

    private val scrolling by lazy {
        val params = CoordinatorLayout.LayoutParams (MATCH, MATCH)
            params.behavior = AppBarLayout.ScrollingViewBehavior ()
            params
    }

    private lateinit var events : MonthLoaderAdapter

    private var swipeable by Delegates.observable (true) { property, previous, next -> if (next != previous) Anvil.render () }

    private val subscription : Runnable

    init {
        subscription = store.subscribe {
            events.update (store.state.events)
        }
    }

    override fun close () {
        subscription.run ()
    }

    override fun view () {
        weekview ()
    }

    private fun weekview () {
        coordinatorLayout {
            size (MATCH, MATCH)
            orientation (LinearLayout.VERTICAL)

            appBarLayout {
                size (MATCH, WRAP)
                expanded (false)

                collapsingToolbarLayout {
                    anvilonce<CollapsingToolbarLayout> {
                        val params = layoutParams as AppBarLayout.LayoutParams
                            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

                        size (MATCH, MATCH)

                        title (store.state.events.name)
                        titleEnabled (true)

                        expandedTitleColor      (Color.TRANSPARENT)
                        collapsedTitleTextColor (Color.WHITE)
                    }

                    imageView {
                        anvilonce<ImageView> {
                            val params = layoutParams as CollapsingToolbarLayout.LayoutParams
                                params.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX

                            size (MATCH, dip (200))
                            scaleType (ImageView.ScaleType.CENTER_CROP)
                        }
                    }

                    toolbar {
                        anvilonce<Toolbar> {
                            val params = layoutParams as CollapsingToolbarLayout.LayoutParams
                                params.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN

                            size (MATCH, dip (56))
                            popupTheme (R.style.AppTheme_PopupOverlay)
                        }
                    }
                }
            }

            swipeRefreshLayout {
                layoutParams (scrolling)

                size (MATCH, MATCH)

                v (WeekviewTouchProxy::class.java) {
                    anvilonce<WeekviewTouchProxy> {
                        id (R.id.weekview_proxy)

                        scrolling = object : WeekviewTouchProxy.Scrolling {

                                private var previous by vetoable (Double.MIN_VALUE) {
                                    _, previous,                       next ->
                                       previous == Double.MIN_VALUE || next == Double.MIN_VALUE
                                }

                                override fun top (state : Boolean) {
                                    swipeable = state
                                }

                                override fun hour (earliest : Double) {
                                    previous = earliest

                                    val delta : Double = Math.abs (previous - earliest)
                                    if (delta > 1) {
                                        reset ()
                                    }
                                }

                                private fun reset () { previous = Double.MIN_VALUE }

                        }

                        events = MonthLoaderAdapter (this, store)

                        monthChangeListener         = events
                        numberOfVisibleDays         = 1
                        columnGap                   = dip (8)
                        hourHeight                  = dip (600)
                        headerColumnPadding         = dip (8)
                        headerRowPadding            = dip (12)
                        textSize                    = sip (10)
                        eventTextSize               = sip (10)
                        eventTextColor              = color (R.color.primary_text)
                        defaultEventColor           = color (R.color.primary)

                        headerColumnTextColor       = color (R.color.primary_text)
                        headerColumnBackgroundColor = color (R.color.primary_dark)
                        headerRowBackgroundColor    = color (R.color.primary_dark)

                        dayBackgroundColor          = color (R.color.primary_light)
                        todayBackgroundColor        = color (R.color.primary_light)
                        todayHeaderTextColor        = color (R.color.primary_text)

                        hourSeparatorColor          = color (R.color.divider)
                    }
                }

                enabled (swipeable)

                refreshing (store.state.events.synchronizing)
                onRefresh {
                    use<WeekviewTouchProxy> (R.id.weekview_proxy) {
                        store.dispatcher.dispatch (SynchronizeCalendar (
                            year  = firstVisibleDay.get (Calendar.YEAR),
                            month = firstVisibleDay.get (Calendar.MONTH) + 1
                        ))
                    }
                }
            }
        }
    }

    private class MonthLoaderAdapter (private val week : WeekView, private val store : Storage<*>) : MonthLoader.MonthChangeListener {

        private val map = mutableMapOf<Pair<Int, Int>, Pair<DateTime, Collection<Event>>> ()

        private var maphash by Delegates.observable (map.hashCode ()) { _, previous, next ->
            if (previous == next)
                return@observable

            week.notifyDatasetChanged ()
        }

        private val identifiers = mutableMapOf<String, Long> ()

        override fun onMonthChange     (year : Int, month : Int) : List<WeekViewEvent>? {
            val             key = Pair (year,       month)
            val value = map[key]

            if (value == null || outdates (value.first)) {
                store.dispatcher.dispatch (SynchronizeCalendar (year, month))
            }

            return value?.second?.map (this::convert) ?: emptyList<WeekViewEvent> ()
        }

        fun update (events : Events) {
            map += events.map
            maphash = map.hashCode ()
        }

        private fun outdates       (target : DateTime,                  minutes : Minutes = Minutes.TWO) =
            Minutes.minutesBetween (target, DateTime ()).isGreaterThan (minutes)

        private fun convert               (event : Event) : WeekViewEvent {
            val id = identifiers.getOrPut (event.id ()) { Random ().nextLong() }

            return WeekViewEvent (id, event.title (), event.location (), calendar (event.start ()), calendar (event.end ()))
        }

        private fun calendar (instant : Instant?) : Calendar {
            val calendar = Calendar.getInstance ()
                calendar.time = instant?.toDate ()

            return calendar
        }

    }

}
