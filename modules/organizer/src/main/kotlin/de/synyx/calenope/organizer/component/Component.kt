package de.synyx.calenope.organizer.component

import android.view.View
import trikita.anvil.Anvil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author clausen - clausen@synyx.de
 */
abstract class Component : Anvil.Renderable {

    private val identifiers : ConcurrentMap<String, Int> = ConcurrentHashMap ()
    private val components  : ConcurrentMap<String, Anvil.Renderable> = ConcurrentHashMap ()

    protected fun component (name : String? = null,                   component : () -> Component) {
        val v = if          (name != null) components.getOrPut (name, component) else component ()

        v.view ()
    }

    protected fun viewID            (name : String) : Int {
        return identifiers.getOrPut (name) { View.generateViewId () }
    }

}