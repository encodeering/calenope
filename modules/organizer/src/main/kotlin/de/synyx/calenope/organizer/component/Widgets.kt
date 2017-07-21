package de.synyx.calenope.organizer.component

import android.widget.ImageButton
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.ui.anvilcast
import de.synyx.calenope.organizer.ui.anvilonce
import trikita.anvil.DSL.backgroundResource
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.imageButton
import trikita.anvil.DSL.imageResource
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onLongClick
import trikita.anvil.DSL.size
import trikita.anvil.design.DesignDSL

/**
 * @author clausen - clausen@synyx.de
 */
object Widgets  {

    class button (
        resource : Int,
        button   : ImageButton.() -> Unit = {}
    ) {
        init {
            imageButton {
                anvilonce<android.widget.ImageButton> {
                    size (dip (48), dip (48))
                    imageResource (resource)
                    backgroundResource (R.color.primary)
                }

                anvilcast<ImageButton> {
                    onClick {}
                    onLongClick { false }

                    button (this)
                }


            }
        }
    }

}