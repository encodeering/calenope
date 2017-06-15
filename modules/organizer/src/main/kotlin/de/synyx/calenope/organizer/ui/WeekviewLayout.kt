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
import de.synyx.calenope.organizer.component.WeekviewTouchProxy
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.Minutes
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.MATCH
import trikita.anvil.BaseDSL.WRAP
import trikita.anvil.BaseDSL.v
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.enabled
import trikita.anvil.DSL.imageResource
import trikita.anvil.DSL.imageView
import trikita.anvil.DSL.init
import trikita.anvil.DSL.layoutParams
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.scaleType
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
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
import kotlin.properties.Delegates

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

    private lateinit var week : WeekView

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
                    size (MATCH, MATCH)

                    init {
                        val layout = Anvil.currentView<CollapsingToolbarLayout> ()
                        val params = layout.layoutParams as AppBarLayout.LayoutParams
                            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

                        title (store.state.events.name)
                        titleEnabled (true)

                        expandedTitleColor      (Color.TRANSPARENT)
                        collapsedTitleTextColor (Color.WHITE)
                    }

                    imageView {
                        size (MATCH, WRAP)

                        init {
                            val imageView = Anvil.currentView<ImageView> ()
                            val params = imageView.layoutParams as CollapsingToolbarLayout.LayoutParams
                                params.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
                        }

                        imageResource (R.drawable.background)
                        scaleType (ImageView.ScaleType.CENTER_CROP)
                    }

                    toolbar {
                        size (MATCH, dip (56))

                        init {
                            val toolbar = Anvil.currentView<Toolbar> ()
                            val params = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
                                params.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN

                            weekview.setTheme (R.style.AppTheme_AppBarOverlay)
                        }

                        popupTheme(R.style.AppTheme_PopupOverlay)
                    }
                }
            }

            swipeRefreshLayout {
                layoutParams (scrolling)

                size (MATCH, MATCH)

                v (WeekviewTouchProxy::class.java) {
                    init {
                         week = Anvil.currentView<WeekviewTouchProxy> ()
                        (week as WeekviewTouchProxy).scrolling = object : WeekviewTouchProxy.Scrolling {

                            override fun top (state : Boolean) {
                                swipeable = state
                            }
                        }

                        events = MonthLoaderAdapter (week, store)

                            week.monthChangeListener         = events
                            week.numberOfVisibleDays         = 1
                            week.columnGap                   = dip (8)
                            week.hourHeight                  = dip (600)
                            week.headerColumnPadding         = dip (8)
                            week.headerRowPadding            = dip (12)
                            week.textSize                    = sip (10)
                            week.eventTextSize               = sip (10)
                            week.eventTextColor              = Color.WHITE
                            week.headerColumnTextColor       = Color.parseColor ("#8f000000")
                            week.headerColumnBackgroundColor = Color.parseColor ("#ffffffff")
                            week.headerRowBackgroundColor    = Color.parseColor ("#ffefefef")
                            week.dayBackgroundColor          = Color.parseColor ("#05000000")
                            week.todayBackgroundColor        = Color.parseColor ("#1848adff")
                    }
                }

                enabled (swipeable)

                refreshing (store.state.events.synchronizing)
                onRefresh {
                    store.dispatcher.dispatch (SynchronizeCalendar (
                        year  = week.firstVisibleDay.get (Calendar.YEAR),
                        month = week.firstVisibleDay.get (Calendar.MONTH) + 1
                    ))
                }
            }
        }
    }

    private class MonthLoaderAdapter (private val week : WeekView, private val store : Storage<*>) : MonthLoader.MonthChangeListener {

        private val map = mutableMapOf<Pair<Int, Int>, Pair<DateTime, Collection<Event>>> ()

        override fun onMonthChange     (year : Int, month : Int) : List<WeekViewEvent>? {
            val             key = Pair (year,       month)
            val value = map[key]

            if (value == null || outdates (value.first)) {
                store.dispatcher.dispatch (SynchronizeCalendar (year, month))
            }

            return value?.second?.mapIndexed { index, event -> convert (index.toLong (), event) } ?: emptyList<WeekViewEvent> ()
        }

        fun update (events : Events) {
            map += events.map
            week.notifyDatasetChanged ()
        }

        private fun outdates       (target : DateTime,                  minutes : Minutes = Minutes.TWO) =
            Minutes.minutesBetween (target, DateTime ()).isGreaterThan (minutes)

        private fun convert(index : Long, event : Event) : WeekViewEvent {
            return WeekViewEvent (index, event.title (), event.location (), calendar (event.start ()), calendar (event.end ()))
        }

        private fun calendar (instant : Instant?) : Calendar {
            val calendar = Calendar.getInstance ()
                calendar.time = instant?.toDate ()

            return calendar
        }

    }

}
