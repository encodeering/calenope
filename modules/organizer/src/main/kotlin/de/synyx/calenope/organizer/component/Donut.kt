package de.synyx.calenope.organizer.component

import android.widget.ProgressBar
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.centerInParent
import trikita.anvil.BaseDSL.init
import trikita.anvil.DSL.progressBar

/**
 * @author clausen - clausen@synyx.de
 */

fun donut (indeterminate : Boolean = true, r : ProgressBar.() -> Unit = {}) {
    progressBar {
        val bar = Anvil.currentView<ProgressBar>()

        init {
            bar.isIndeterminate = indeterminate
        }

        centerInParent ()

        r (bar)
    }

}

