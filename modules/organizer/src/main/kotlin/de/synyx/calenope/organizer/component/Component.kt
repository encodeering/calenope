package de.synyx.calenope.organizer.component

import trikita.anvil.Anvil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author clausen - clausen@synyx.de
 */
abstract class Component : Anvil.Renderable {

    private val components  : ConcurrentMap<String, Anvil.Renderable> = ConcurrentHashMap ()

    protected fun component (name : String? = null,                   component : () -> Component) {
        val v = if          (name != null) components.getOrPut (name, component) else component ()

        v.view ()
    }

}