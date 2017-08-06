package de.synyx.calenope.organizer.component

import android.app.TimePickerDialog
import android.content.Context
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import com.encodeering.conflate.experimental.api.Storage
import de.synyx.calenope.organizer.Interact
import de.synyx.calenope.organizer.Interaction
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.State
import de.synyx.calenope.organizer.component.Widgets.button
import de.synyx.calenope.organizer.component.Widgets.speechinput
import de.synyx.calenope.organizer.speech.Speech
import de.synyx.calenope.organizer.ui.anvilonce
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.layoutGravity
import trikita.anvil.DSL.MATCH
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.linearLayout
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onEditorAction
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
import trikita.anvil.DSL.text
import trikita.anvil.DSL.textSize
import trikita.anvil.DSL.textView

/**
 * @author clausen - clausen@synyx.de
 */

class WeekviewEditor (
        private val context : Context,
        private val speech  : Speech,
        private val store   : Storage<State>
) : Anvil.Renderable {

    override fun view () {
        linearLayout {
            anvilonce<LinearLayout> {
                size (MATCH, dip (210))
                orientation (LinearLayout.VERTICAL)
            }

            val   interaction = store.state.events.interaction
            when (interaction) {
                is Interaction.Inspect -> {
                    textView {
                        text (interaction.event.description ().run { take (197) + if (length > 197) "..." else "" })
                        textSize (sip (16.0f))
                        size (MATCH, WRAP)
                        margin (dip (20), dip (20), dip (20), 0)
                    }
                }
                is Interaction.Create  -> {
                    fun editorwatch (action : TextView. () -> Unit) = { view : TextView, code : Int, _ : KeyEvent? ->
                        when (code) {
                            EditorInfo.IME_ACTION_PREVIOUS,
                            EditorInfo.IME_ACTION_NEXT,
                            EditorInfo.IME_ACTION_DONE -> action (view)
                        }
                        false
                    }

                    speechinput (interaction.title, context.getString (R.string.weekview_editor_title),
                        input = {
                            onEditorAction (editorwatch {
                                store.state.events.interaction.apply {
                                    when (this) {
                                        is Interaction.Create -> store.dispatcher.dispatch (Interact (copy (title = text.toString ()), visualize = true))
                                    }
                                }
                            })
                        },
                        button = {
                            onClick {
                                speech.ask (context.getString (R.string.weekview_editor_title)) {
                                    store.state.events.interaction.apply {
                                        when (this) {
                                            is Interaction.Create -> store.dispatcher.dispatch (Interact (copy (title = it), visualize = true))
                                        }
                                    }
                                }
                            }
                        }
                    )

                    speechinput (interaction.description, context.getString (R.string.weekview_editor_description),
                        input = {
                            onEditorAction (editorwatch {
                                store.state.events.interaction.apply {
                                    when (this) {
                                        is Interaction.Create -> store.dispatcher.dispatch (Interact (copy (description = text.toString ()), visualize = true))
                                    }
                                }
                            })
                        },
                        button = {
                            onClick {
                                speech.ask (context.getString (R.string.weekview_editor_description)) {
                                    store.state.events.interaction.apply {
                                        when (this) {
                                            is Interaction.Create -> store.dispatcher.dispatch (Interact (copy (description = it), visualize = true))
                                        }
                                    }
                                }
                            }
                        }
                    )

                    button (R.drawable.ic_timelapse) {
                        margin (0, dip (20), dip (20), 0)
                        layoutGravity (Gravity.RIGHT)
                        onClick {
                            store.state.events.interaction.apply {
                                when (this) {
                                    is Interaction.Create -> start.plusMinutes (15).let {
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
}