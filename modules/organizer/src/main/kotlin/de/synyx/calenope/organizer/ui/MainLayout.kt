package de.synyx.calenope.organizer.ui

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.widget.LinearLayout
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.R
import rx.Observer
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.FILL
import trikita.anvil.BaseDSL.MATCH
import trikita.anvil.BaseDSL.init
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.adapter
import trikita.anvil.DSL.button
import trikita.anvil.DSL.centerHorizontal
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.gridView
import trikita.anvil.DSL.horizontalSpacing
import trikita.anvil.DSL.layoutParams
import trikita.anvil.DSL.linearLayout
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.numColumns
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onItemClick
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.relativeLayout
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
import trikita.anvil.DSL.text
import trikita.anvil.DSL.textColor
import trikita.anvil.DSL.textSize
import trikita.anvil.DSL.textView
import trikita.anvil.DSL.verticalSpacing
import trikita.anvil.RenderableAdapter
import trikita.anvil.RenderableView
import trikita.anvil.appcompat.v7.AppCompatv7DSL.popupTheme
import trikita.anvil.appcompat.v7.AppCompatv7DSL.toolbar
import trikita.anvil.design.DesignDSL.appBarLayout
import trikita.anvil.design.DesignDSL.coordinatorLayout

/**
 * @author clausen - clausen@synyx.de
 */

class MainLayout (private val main : Main) : RenderableView (main) {

    private val store by lazy { Application.store () }

    private val scrolling by lazy {
        val params = CoordinatorLayout.LayoutParams (MATCH, MATCH)
            params.behavior = AppBarLayout.ScrollingViewBehavior ()
            params
    }

    override fun view () {
        bind ()
        overview ()
    }

    private val tiles : RxRenderableAdapter<String> by lazy {
        RxRenderableAdapter<String> { item, position ->
            relativeLayout {
                size (WRAP, WRAP)
                orientation (LinearLayout.VERTICAL)

                textView {
                    size (WRAP, WRAP)
                    text (item)
                    textSize (sip (10.toFloat()))
                    textColor (Color.BLACK)
                    centerHorizontal ()
                    margin (dip (20))
                }
            }
        }
    }

    private fun bind () {
        tiles.onNext (store.state.overview.calendars)
    }

    private fun overview () {
        coordinatorLayout {
            size (MATCH, MATCH)
            orientation (LinearLayout.VERTICAL)

            appBarLayout {
                size (MATCH, WRAP)

                toolbar {
                    init {
                        main.setTheme (R.style.AppTheme_AppBarOverlay)
                        main.setSupportActionBar (Anvil.currentView ())
                    }

                    popupTheme (R.style.AppTheme_PopupOverlay)

                    button {
                        text ("Setting ${store.state.setting.account}")
                        onClick { store.dispatch (Action.OpenSettings (main)) }
                    }

                    button {
                        text ("Update")
                        onClick { store.dispatch (Action.SynchronizeAccount ()) }
                    }
                }
            }

            linearLayout {
                layoutParams (scrolling)

                gridView {
                    size (FILL, FILL)
                    adapter (tiles)
                    numColumns (2)
                    horizontalSpacing (dip (0))
                    verticalSpacing   (dip (0))
                    onItemClick { adapter, view, position, id -> store.dispatch (Action.SelectCalendar (adapter.getItemAtPosition(position) as String? ?: "")) }
                }
            }
        }
    }

    private inner class RxRenderableAdapter<T> (private val view : (value : T, position : Int) -> Unit) : RenderableAdapter (), Observer<Collection<T>> {

        private var last : Collection<T> = emptyList ()

        override fun view (index : Int) {
            view (getItem (index), index)
        }

        override fun getItem (position : Int) : T = last.elementAt (position)

        override fun getCount () : Int = last.size

        override fun onNext (t : Collection<T>) {
            last = t
            notifyDataSetChanged ()
        }

        override fun onCompleted () {
            notifyDataSetChanged ()
        }

        override fun onError (e : Throwable?) {
            last = emptyList ()
            notifyDataSetChanged ()
        }

    }

}
