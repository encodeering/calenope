package de.synyx.calenope.organizer.ui

import android.content.Context
import android.graphics.Color
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import de.synyx.calenope.core.api.model.Event
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.Application
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.Minutes
import rx.Observer
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.MATCH
import trikita.anvil.DSL
import trikita.anvil.DSL.init
import trikita.anvil.DSL.linearLayout
import trikita.anvil.DSL.size
import trikita.anvil.DSL.v
import trikita.anvil.RenderableView
import trikita.jedux.Store
import java.util.*

/**
 * @author clausen - clausen@synyx.de
 */
class WeekviewLayout (private val c : Context) : RenderableView (c) {

    private val store by lazy { Application.store () }

    private lateinit var events : MonthLoaderAdapter<Event>

    override fun view () {
        weekview ()
        bind ()
    }

    private fun bind () {
        events.onNext (store.state.events.map)
    }

    private fun weekview () {
        linearLayout {
            size (MATCH, MATCH)

            v (WeekView::class.java) {
                init {
                    val week = Anvil.currentView<WeekView> ()

                    events = MonthLoaderAdapter<Event> (week, store)

                        week.monthChangeListener         = events
                        week.numberOfVisibleDays         = 1
                        week.columnGap                   = DSL.dip (8)
                        week.hourHeight                  = DSL.dip (60)
                        week.headerColumnPadding         = DSL.dip (8)
                        week.headerRowPadding            = DSL.dip (12)
                        week.textSize                    = DSL.sip (12)
                        week.eventTextSize               = DSL.sip (12)
                        week.eventTextColor              = Color.WHITE
                        week.headerColumnTextColor       = Color.parseColor ("#8f000000")
                        week.headerColumnBackgroundColor = Color.parseColor ("#ffffffff")
                        week.headerRowBackgroundColor    = Color.parseColor ("#ffefefef")
                        week.dayBackgroundColor          = Color.parseColor ("#05000000")
                        week.todayBackgroundColor        = Color.parseColor ("#1848adff")
                }
            }
        }
    }

    private class MonthLoaderAdapter<V : Event> (private val week : WeekView, private val store : Store<Action, *>) : MonthLoader.MonthChangeListener, Observer<Map<Pair<Int, Int>, Collection<V>>> {

        private val map : MutableMap<Pair<Int, Int>, Pair<Collection<V>, DateTime>> = mutableMapOf ()

        override fun onMonthChange     (year : Int, month : Int) : List<WeekViewEvent>? {
            val             key = Pair (year,       month)
            val value = map[key]

            if (value == null || outdates (value.second)) {
                store.dispatch (Action.SynchronizeCalendar (year, month))
            }

            return value?.first?.mapIndexed { index, event -> convert (index.toLong (), event) } ?: emptyList<WeekViewEvent> ()
        }

        override fun onNext (t : Map<Pair<Int, Int>, Collection<V>>) {
            val timestamp = DateTime.now ()
            map += t.mapValues { Pair (it.value, timestamp) }
            week.notifyDatasetChanged ()
        }

        override fun onCompleted () {
            week.notifyDatasetChanged ()
        }

        override fun onError (e : Throwable) {
            map.clear ()
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
