package de.synyx.calenope.organizer.component

import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import de.synyx.calenope.organizer.R
import trikita.anvil.BaseDSL.MATCH
import trikita.anvil.BaseDSL.WRAP
import trikita.anvil.BaseDSL.layoutGravity
import trikita.anvil.BaseDSL.weight
import trikita.anvil.DSL.backgroundResource
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.imageButton
import trikita.anvil.DSL.imageResource
import trikita.anvil.DSL.inputType
import trikita.anvil.DSL.linearLayout
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onEditorAction
import trikita.anvil.DSL.onLongClick
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.size
import trikita.anvil.DSL.text
import trikita.anvil.design.DesignDSL.hint
import trikita.anvil.design.DesignDSL.textInputEditText
import trikita.anvil.design.DesignDSL.textInputLayout

/**
 * @author clausen - clausen@synyx.de
 */
object Widgets {

    class Button (
        private val resource : Int,
        private val button   : Element<ImageButton>.() -> Unit = {}
    ) : Component () {

        override fun view () {
            imageButton {
                configure<ImageButton> {
                    once += {
                        size (dip (48), dip (48))
                        imageResource (resource)
                        backgroundResource (R.color.primary)
                    }

                    always += {
                        onClick {}
                        onLongClick { false }
                    }

                    button (this)
                }
            }
        }

    }

    class Speechinput (
        private val text   : CharSequence? = "",
        private val hint   : CharSequence? = "",
        private val input  : Element<TextInputEditText>.() -> Unit  = {},
        private val button : Element<ImageButton>.() -> Unit        = {}
    ) : Component ()  {

        override fun view () {
            linearLayout {
                size (MATCH, WRAP)
                orientation (LinearLayout.HORIZONTAL)

                margin (dip (20), dip (20), dip (20), 0)

                textInputLayout {
                    configure<TextInputLayout> {
                        once += {
                            size (MATCH, WRAP)
                            weight (1.0f)
                            hint (this@Speechinput.hint)
                        }
                    }

                    textInputEditText {
                        configure<TextInputEditText> {
                            once += {
                                size (MATCH, dip (40))
                                inputType (InputType.TYPE_CLASS_TEXT)
                            }

                            always += {
                                text (this@Speechinput.text)
                                onEditorAction { _, _, _ -> false }
                            }

                            input (this)
                        }
                    }
                }

                show {
                    Button (R.drawable.ic_record) {
                        once += {
                            layoutGravity (Gravity.END)
                        }

                        button (this)
                    }
                }
            }
        }
    }
}