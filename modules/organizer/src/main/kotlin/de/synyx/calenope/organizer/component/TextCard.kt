package de.synyx.calenope.organizer.component

import android.support.v7.widget.CardView
import android.widget.TextView
import trikita.anvil.DSL.CENTER
import trikita.anvil.DSL.MATCH
import trikita.anvil.DSL.centerHorizontal
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.gravity
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onLongClick
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
import trikita.anvil.DSL.textSize
import trikita.anvil.DSL.textView
import trikita.anvil.cardview.v7.CardViewv7DSL.cardView
import trikita.anvil.cardview.v7.CardViewv7DSL.radius

/**
 * @author clausen - clausen@synyx.de
 */
class TextCard(
    private val card : Element<CardView>.() -> Unit = {},
    private val text : Element<TextView>.() -> Unit = {}
) : Component () {

    override fun view () {
        cardView {
            configure<CardView> {
                once += {
                    size (MATCH, dip (64))
                    gravity (CENTER)
                    margin (dip (0), dip (0), dip (0), dip (1))
                    radius (0.0f)
                }

                always += {
                    onClick {}
                    onLongClick { true }
                }

                card (this)
            }

            textView {
                configure<TextView> {
                    once += {
                        size (MATCH, MATCH)
                        textSize (sip (10.toFloat ()))
                        centerHorizontal ()
                        margin (dip (20))
                    }

                    text (this)
                }
            }
        }
    }

}