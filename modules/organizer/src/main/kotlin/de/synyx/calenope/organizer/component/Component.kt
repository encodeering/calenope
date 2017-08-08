package de.synyx.calenope.organizer.component

import android.view.View
import de.synyx.calenope.organizer.ui.anvilcast
import de.synyx.calenope.organizer.ui.anvilonce
import trikita.anvil.Anvil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author clausen - clausen@synyx.de
 */
abstract class Component : Anvil.Renderable {

    private val identifiers : ConcurrentMap<String, Int> = ConcurrentHashMap ()
    private val components  : ConcurrentMap<String, Component> = ConcurrentHashMap ()

    protected fun component (name : String? = null,                   component : () -> Component) {
        val v = if          (name != null) components.getOrPut (name, component) else component ()

        v.view ()
    }

    protected fun viewID            (name : String) : Int {
        return identifiers.getOrPut (name) { View.generateViewId () }
    }

    protected inline fun <reified C : View> configure (crossinline code : Element<C>.() -> Unit) {
        anvilcast<C> {
            Element (this@Component, this@anvilcast).apply { code (this) }.view ()
        }
    }

    class Element<C> (val component : Component, val view : C) : Anvil.Renderable {

        val once   by lazy { Customization<C.(Component) -> Unit> () }
        val always by lazy { Customization<C.(Component) -> Unit> () }

        override fun view () {
            anvilonce<View> {
                once.actions.forEach { it (view, component) }
            }

            always.actions.forEach { it (view, component) }
        }
    }

    class Customization<C> {

        val actions = mutableListOf<C> ()

        operator fun plusAssign (action : C) {
            actions += action
        }

    }

}