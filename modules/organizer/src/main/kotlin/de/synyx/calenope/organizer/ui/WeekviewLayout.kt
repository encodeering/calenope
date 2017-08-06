package de.synyx.calenope.organizer.ui

import android.graphics.Color
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.encodeering.conflate.experimental.api.Storage
import de.synyx.calenope.core.api.model.Event
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.Interact
import de.synyx.calenope.organizer.Interaction
import de.synyx.calenope.organizer.Interaction.Create
import de.synyx.calenope.organizer.Interaction.Inspect
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.State.Events
import de.synyx.calenope.organizer.SynchronizeCalendar
import de.synyx.calenope.organizer.color
import de.synyx.calenope.organizer.component.Layouts.Collapsible
import de.synyx.calenope.organizer.component.WeekviewEditor
import de.synyx.calenope.organizer.component.WeekviewTouchProxy
import de.synyx.calenope.organizer.floor
import de.synyx.calenope.organizer.speech.Speech
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.Minutes
import trikita.anvil.Anvil
import trikita.anvil.DSL.alpha
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.enabled
import trikita.anvil.DSL.id
import trikita.anvil.DSL.imageResource
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.v
import trikita.anvil.DSL.visibility
import trikita.anvil.RenderableView
import trikita.anvil.design.DesignDSL.collapsedTitleTextColor
import trikita.anvil.design.DesignDSL.contentScrimResource
import trikita.anvil.design.DesignDSL.expanded
import trikita.anvil.design.DesignDSL.expandedTitleMarginBottom
import trikita.anvil.design.DesignDSL.expandedTitleMarginStart
import trikita.anvil.design.DesignDSL.title
import trikita.anvil.support.v4.Supportv4DSL.onRefresh
import trikita.anvil.support.v4.Supportv4DSL.refreshing
import java.util.Calendar
import java.util.Random
import kotlin.properties.Delegates

/**
 * @author clausen - clausen@synyx.de
 */
class WeekviewLayout (weekview : Weekview) : RenderableView (weekview), AutoCloseable {

    private val speech = object : Speech {

        override fun ask (prompt : String, callback : (String) -> Unit) {
            weekview.ask (prompt, prompt.sumBy { it.toInt () } % (2 * Short.MAX_VALUE), callback)
        }
    }

    private val store by lazy { Application.store }

    private val editor : WeekviewEditor = WeekviewEditor (weekview, speech, store)

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
        weekview.view ()
    }

    val weekview by lazy {
        Collapsible (
            fab = {
                once += {
                        alpha (0.0f)
                }

                always += {
                        visibility (true)

                        onClick {}

                        val   visualize   = store.state.events.visualize
                        val   interaction = store.state.events.interaction
                        when (interaction) {
                            is Create -> {
                                if (visualize) {
                                    imageResource (R.drawable.ic_save)
                                    onClick {
                                        store.state.events.interaction.apply {
                                            when (this) {
                                                is Create -> store.dispatcher.dispatch (Interact (copy (draft = false)))
                                            }
                                        }
                                    }
                                }

                                else {
                                    imageResource (R.drawable.ic_add)
                                    onClick {
                                        store.state.events.interaction.apply {
                                            when (this) {
                                                is Create -> store.dispatcher.dispatch (Interact (this, visualize = true))
                                            }
                                        }
                                    }
                                }
                            }
                            is Inspect -> {
                                imageResource (R.drawable.ic_subject)
                                onClick {
                                    store.state.events.interaction.apply {
                                        when (this) {
                                            is Inspect -> store.dispatcher.dispatch (Interact (this, visualize = true))
                                        }
                                    }
                                }
                            }
                        }

                        when (interaction) {
                            is Interaction.Read -> if (alpha  > 0)    animate ().alpha (0.0f).setDuration (500L).start ()
                            else                -> if (alpha == 0.0f) animate ().alpha (1.0f).setDuration (500L).start ()
                        }
                }
            },

            content = {
                always += {
                v (WeekviewTouchProxy::class.java) {
                    anvilonce<WeekviewTouchProxy> {
                        id (R.id.weekview_proxy)

                        scrolling = object : WeekviewTouchProxy.Scrolling {

                            private var previous by Delegates.vetoable (Double.MIN_VALUE) {
                                _, previous,                       next ->
                                   previous == Double.MIN_VALUE || next == Double.MIN_VALUE
                            }

                            override fun top (state : Boolean) {
                                swipeable = state
                            }

                            override fun hour (earliest : Double) {
                                if (store.state.events.interaction == Interaction.Read)
                                    return reset ()

                                if (store.state.events.visualize)
                                    return reset ()

                                previous = earliest

                                val delta : Double = Math.abs (previous - earliest)
                                if (delta > 1) {
                                    store.dispatcher.dispatch (Interact (Interaction.Read))
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

                        setOnEventClickListener { event, _ ->
                            events.convert       (event)?.let {
                                store.dispatcher.dispatch (
                                        Interact (Inspect (it), visualize = store.state.events.visualize)
                                )
                            }
                        }

                        setEmptyViewClickListener { calendar ->
                            store.dispatcher.dispatch (
                                        Interact (Create  (draft = true, start = DateTime (calendar.time).floor (15)), visualize = store.state.events.visualize)
                            )
                        }
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
            },

            appbar = {
                always += {
                expanded (store.state.events.visualize)
                }
            },

            collapsible = {
                always += {
                contentScrimResource (R.color.primary)

                collapsedTitleTextColor (Color.WHITE)

                expandedTitleMarginBottom (dip (20))
                expandedTitleMarginStart  (dip (20))

                val subject = store.state.events.interaction.let {
                    when (it) {
                        is Create  -> return@let it.start.toString ("HH:mm") + (it.end?.let { " - ${it.toString ("HH:mm")}" } ?: "")
                        is Inspect -> return@let it.event.title ()
                        else       -> return@let store.state.events.name
                    }
                }

                title (subject)

                editor.view ()
                }
            }
        )
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

        fun convert (event : WeekViewEvent) = map.flatMap { it.value.second }.firstOrNull { identifiers[it.id ()] == event.id }

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
