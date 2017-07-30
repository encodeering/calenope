package de.synyx.calenope.organizer.ui

import android.view.View
import trikita.anvil.Anvil.currentView
import trikita.anvil.BaseDSL.init

/**
 * @author clausen - clausen@synyx.de
 */
@Suppress("UNCHECKED_CAST")
fun <T : View> View.use(identifier : Int, renderable : T.() -> Unit = {}) : T? {
    return (rootView.findViewById (identifier) as T?)?.apply (renderable)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <reified T : View> anvilonce (noinline renderable: T.() -> Unit) {
    init {
        anvilcast (renderable)
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : View> anvilcast (renderable: T.() -> Unit) {
    currentView<View> ().apply {
        if (this is T)
            this.renderable ()
    }
}