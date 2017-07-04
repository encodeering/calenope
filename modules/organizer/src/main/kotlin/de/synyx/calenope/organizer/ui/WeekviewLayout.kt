package de.synyx.calenope.organizer.ui

import android.app.TimePickerDialog
import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
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
import de.synyx.calenope.organizer.component.WeekviewTouchProxy
import de.synyx.calenope.organizer.floor
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.Minutes
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.MATCH
import trikita.anvil.BaseDSL.WRAP
import trikita.anvil.BaseDSL.layoutGravity
import trikita.anvil.BaseDSL.textSize
import trikita.anvil.BaseDSL.weight
import trikita.anvil.DSL.backgroundResource
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.enabled
import trikita.anvil.DSL.id
import trikita.anvil.DSL.imageButton
import trikita.anvil.DSL.imageResource
import trikita.anvil.DSL.inputType
import trikita.anvil.DSL.layoutParams
import trikita.anvil.DSL.linearLayout
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onEditorAction
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
import trikita.anvil.DSL.v
import trikita.anvil.DSL.text
import trikita.anvil.DSL.textColor
import trikita.anvil.DSL.textView
import trikita.anvil.RenderableView
import trikita.anvil.appcompat.v7.AppCompatv7DSL.popupTheme
import trikita.anvil.appcompat.v7.AppCompatv7DSL.toolbar
import trikita.anvil.design.DesignDSL.appBarLayout
import trikita.anvil.design.DesignDSL.collapsedTitleTextColor
import trikita.anvil.design.DesignDSL.collapsingToolbarLayout
import trikita.anvil.design.DesignDSL.coordinatorLayout
import trikita.anvil.design.DesignDSL.expanded
import trikita.anvil.design.DesignDSL.floatingActionButton
import trikita.anvil.design.DesignDSL.hint
import trikita.anvil.design.DesignDSL.textInputEditText
import trikita.anvil.design.DesignDSL.textInputLayout
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
                anvilonce<AppBarLayout> {
                    val behavior = AppBarLayout.Behavior ()
                        behavior.setDragCallback (object : AppBarLayout.Behavior.DragCallback () {
                            override fun canDrag (layout : AppBarLayout) : Boolean {
                                return false
                            }
                        })

                    val lparams = layoutParams as CoordinatorLayout.LayoutParams
                        lparams.behavior = behavior
                }

                size (MATCH, WRAP)
                expanded (store.state.events.visualize)

                collapsingToolbarLayout {
                    anvilonce<CollapsingToolbarLayout> {
                        val params = layoutParams as AppBarLayout.LayoutParams
                            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

                        size (MATCH, MATCH)

                        titleEnabled (true)

                        collapsedTitleTextColor (Color.WHITE)

                        setContentScrimResource (R.color.primary)

                        expandedTitleMarginBottom = dip (20)
                        expandedTitleMarginStart  = dip (20)
                    }

                    anvilcast<CollapsingToolbarLayout> {
                        val subject = store.state.events.interaction.let {
                            when (it) {
                                is Create  -> return@let it.start.toString ("HH:mm") + (it.end?.let { " - ${it.toString ("HH:mm")}" } ?: "")
                                is Inspect -> return@let it.event.title ()
                                else       -> return@let store.state.events.name
                            }
                        }

                        title (subject)
                    }

                    editor ()

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
                id (R.id.weekview_pane)
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

            floatingActionButton {
                anvilonce<FloatingActionButton> {
                    alpha = 0.0f

                    val params = layoutParams as CoordinatorLayout.LayoutParams
                        params.anchorId = R.id.weekview_pane
                        params.anchorGravity = Gravity.BOTTOM or Gravity.END
                }

                anvilcast<FloatingActionButton> {
                    size (WRAP, WRAP)
                    margin (dip (16))

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
            }
        }
    }

    private fun editor () {
        linearLayout {
            anvilonce<LinearLayout> {
                val params = layoutParams as CollapsingToolbarLayout.LayoutParams
                    params.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX

                size (MATCH, dip (210))
                orientation (LinearLayout.VERTICAL)
            }

            val   interaction = store.state.events.interaction
            when (interaction) {
                is Inspect -> {
                    textView {
                        text (interaction.event.description ().run { take (197) + if (length > 197) "..." else "" })
                        textColor (color (R.color.primary_text))
                        textSize (sip(16.0f))
                        size (MATCH, WRAP)
                        margin (dip (20), dip (20), dip (20), 0)
                    }
                }

                is Create -> {
                    fun editorwatch (action : TextView.() -> Unit) = { view: TextView, code : Int, _: KeyEvent? ->
                        when (code) {
                            EditorInfo.IME_ACTION_PREVIOUS,
                            EditorInfo.IME_ACTION_NEXT,
                            EditorInfo.IME_ACTION_DONE -> action (view)
                        }
                        false
                    }

                    linearLayout {
                    size (MATCH, WRAP)
                    margin (dip (20), dip (20), dip (20), 0)
                    orientation (LinearLayout.HORIZONTAL)

                    textInputLayout {
                        size (MATCH, WRAP)
                        weight (1.0f)
                        hint (context.getString (R.string.weekview_editor_title))

                        textInputEditText {
                            size (MATCH, dip (40))
                            inputType (InputType.TYPE_CLASS_TEXT)
                            text (interaction.title)
                            onEditorAction (editorwatch {
                                store.state.events.interaction.apply {
                                    when (this) {
                                        is Create -> store.dispatcher.dispatch (Interact (copy (title = text.toString ()), visualize = true))
                                    }
                                }
                            })
                        }
                    }

                    imageButton {
                        size (dip (48), dip (48))
                        imageResource (R.drawable.ic_record)
                        backgroundResource (R.color.primary)
                        layoutGravity (Gravity.RIGHT)
                        onClick {
                            weekview.ask (context.getString (R.string.weekview_editor_title), 100) {
                                store.state.events.interaction.apply {
                                    when (this) {
                                        is Create -> store.dispatcher.dispatch (Interact (copy (title = it), visualize = true))
                                    }
                                }
                            }
                        }
                    }
                    }

                    linearLayout {
                    size (MATCH, WRAP)
                    margin (dip (20), dip (20), dip (20), 0)
                    orientation (LinearLayout.HORIZONTAL)

                    textInputLayout {
                        size (MATCH, WRAP)
                        weight (1.0f)
                        hint (context.getString (R.string.weekview_editor_description))

                        textInputEditText {
                            size (MATCH, dip (40))
                            inputType (InputType.TYPE_CLASS_TEXT)
                            text (interaction.description)
                            onEditorAction (editorwatch {
                                store.state.events.interaction.apply {
                                    when (this) {
                                        is Create -> store.dispatcher.dispatch (Interact (copy (description = text.toString ()), visualize = true))
                                    }
                                }
                            })
                        }
                    }

                    imageButton {
                        size (dip (48), dip (48))
                        imageResource (R.drawable.ic_record)
                        backgroundResource (R.color.primary)
                        layoutGravity (Gravity.RIGHT)
                        onClick {
                            weekview.ask (context.getString (R.string.weekview_editor_title), 200) {
                                store.state.events.interaction.apply {
                                    when (this) {
                                        is Create -> store.dispatcher.dispatch (Interact (copy (description = it), visualize = true))
                                    }
                                }
                            }
                        }
                    }
                    }

                    imageButton {
                        size (dip (48), dip (48))
                        margin (0, dip (20), dip (20), 0)
                        imageResource (R.drawable.ic_timelapse)
                        backgroundResource (R.color.primary)
                        layoutGravity (Gravity.RIGHT)
                        onClick {
                            store.state.events.interaction.apply {
                                when (this) {
                                    is Create -> start.plusMinutes (15).let {
                                        val listener : (TimePicker, Int, Int) -> Unit = { _, hour, minute ->
                                            val to = it.withMinuteOfHour (minute)
                                                       .withHourOfDay (hour)

                                            if (to.isAfter (it)) {
                                                store.dispatcher.dispatch (Interact (copy (end = to), visualize = true))
                                            }
                                        }

                                        TimePickerDialog (context, listener, it.hourOfDay, it.minuteOfHour, true).run {
                                            setCancelable (true)
                                            setTitle (context.getString (R.string.weekview_editor_date))
                                            show ()
                                        }
                                    }
                                }
                            }
                        }
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
