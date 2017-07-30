package de.synyx.calenope.organizer.component

import android.support.design.widget.AppBarLayout
import android.support.design.widget.AppBarLayout.Behavior
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.widget.LinearLayout
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.ui.anvilcast
import de.synyx.calenope.organizer.ui.anvilonce
import trikita.anvil.DSL.MATCH
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.id
import trikita.anvil.DSL.layoutParams
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onLongClick
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.size
import trikita.anvil.DSL.visibility
import trikita.anvil.appcompat.v7.AppCompatv7DSL.popupTheme
import trikita.anvil.appcompat.v7.AppCompatv7DSL.toolbar
import trikita.anvil.design.DesignDSL.appBarLayout
import trikita.anvil.design.DesignDSL.collapsingToolbarLayout
import trikita.anvil.design.DesignDSL.coordinatorLayout
import trikita.anvil.design.DesignDSL.expanded
import trikita.anvil.design.DesignDSL.floatingActionButton
import trikita.anvil.design.DesignDSL.title
import trikita.anvil.design.DesignDSL.titleEnabled
import trikita.anvil.support.v4.Supportv4DSL.onRefresh
import trikita.anvil.support.v4.Supportv4DSL.swipeRefreshLayout

/**
 * @author clausen - clausen@synyx.de
 */
object Layouts {

    private val scrolling by lazy {
        val params = CoordinatorLayout.LayoutParams (MATCH, MATCH)
            params.behavior = AppBarLayout.ScrollingViewBehavior ()
            params
    }

    class Regular(
        private val fab     : FloatingActionButton.(Boolean) -> Unit = {},
        private val content : SwipeRefreshLayout.(Boolean )  -> Unit = {},
        private val toolbar : Toolbar.(Boolean)              -> Unit = {}
    ) : Component () {

        override fun view () = component ("layout") {
            Collapsible (
            fab     = fab,
            content = content,
            toolbar = toolbar,
            collapsible = {
                if (it) return@Collapsible

                title ("")
                titleEnabled (false)
            }
            )
        }

    }

    class Collapsible(
        private val draggable   : Boolean = false,
        private val fab         : FloatingActionButton.(Boolean)    -> Unit = {},
        private val content     : SwipeRefreshLayout.(Boolean)      -> Unit = {},
        private val appbar      : AppBarLayout.(Boolean)            -> Unit = {},
        private val toolbar     : Toolbar.(Boolean)                 -> Unit = {},
        private val collapsible : CollapsingToolbarLayout.(Boolean) -> Unit = {}
    ) : Component () {

        override fun view () {
            coordinatorLayout {
                size (MATCH, MATCH)
                orientation (LinearLayout.VERTICAL)

                appBarLayout {
                    anvilonce<AppBarLayout> {
                        val behavior = Behavior ()
                            behavior.setDragCallback (drag (draggable))

                        val params = layoutParams as CoordinatorLayout.LayoutParams
                            params.behavior = behavior

                        appbar (true)
                    }

                    anvilcast<AppBarLayout> {
                        size (MATCH, WRAP)
                        expanded (false)

                        appbar (false)
                    }

                    collapsingToolbarLayout {
                        anvilonce<CollapsingToolbarLayout> {
                            val params = layoutParams as AppBarLayout.LayoutParams
                                params.scrollFlags = params.scrollFlags or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

                            collapsible (true)
                        }

                        anvilcast<CollapsingToolbarLayout> {
                            size (MATCH, MATCH)
                            titleEnabled (true)

                            collapsible (false)
                        }

                        toolbar {
                            anvilonce<Toolbar> {
                                val params = layoutParams as CollapsingToolbarLayout.LayoutParams
                                    params.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN

                                elevation = -1.0f

                                toolbar (true)
                            }

                            anvilcast<Toolbar> {
                                size (MATCH, dip (56))
                                popupTheme (R.style.AppTheme_PopupOverlay)

                                toolbar (false)
                            }
                        }
                    }
                }

                swipeRefreshLayout {
                    anvilonce<SwipeRefreshLayout> {
                        content (true)
                    }

                    anvilcast<SwipeRefreshLayout> {
                        id (viewID ("content"))

                        layoutParams (scrolling)
                        size (MATCH, MATCH)

                        onRefresh {}

                        content (false)
                    }
                }

                floatingActionButton {
                    anvilonce<FloatingActionButton> {
                        val params = layoutParams as CoordinatorLayout.LayoutParams
                            params.anchorId = viewID ("content")
                            params.anchorGravity = Gravity.BOTTOM or Gravity.END

                        fab (true)
                    }

                    anvilcast<FloatingActionButton> {
                        visibility (false)

                        size (WRAP, WRAP)
                        margin (dip (16))

                        onClick {}
                        onLongClick { false }

                        fab (false)
                    }
                }
            }
        }
    }

    private fun drag (draggable : Boolean) : Behavior.DragCallback {
        return object : Behavior.DragCallback () {

            override fun canDrag (layout : AppBarLayout) : Boolean = draggable

        }
    }
}