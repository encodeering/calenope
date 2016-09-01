package de.synyx.calenope.organizer.ui

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.widget.LinearLayout
import de.synyx.calenope.organizer.Action
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.R
import rx.Observer
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.FILL
import trikita.anvil.BaseDSL.init
import trikita.anvil.DSL.MATCH
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.adapter
import trikita.anvil.DSL.centerHorizontal
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.gridView
import trikita.anvil.DSL.horizontalSpacing
import trikita.anvil.DSL.layoutParams
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.numColumns
import trikita.anvil.DSL.onClick
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
import trikita.anvil.appcompat.v7.AppCompatv7DSL.title
import trikita.anvil.appcompat.v7.AppCompatv7DSL.titleTextColor
import trikita.anvil.appcompat.v7.AppCompatv7DSL.toolbar
import trikita.anvil.design.DesignDSL.appBarLayout
import trikita.anvil.design.DesignDSL.coordinatorLayout
import trikita.anvil.support.v4.Supportv4DSL.onRefresh
import trikita.anvil.support.v4.Supportv4DSL.refreshing
import trikita.anvil.support.v4.Supportv4DSL.swipeRefreshLayout

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
        overview ()
        bind ()
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

                onClick {
                    run {
                        store.dispatch (Action.SelectCalendar (item as String? ?: ""))
                        store.dispatch (Action.OpenWeekview (main))
                    }
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
                    val toolbar = Anvil.currentView<Toolbar> ()

                    init {
                        toolbar.minimumHeight = dip (56)
                        toolbar.inflateMenu (R.menu.overview)
                        toolbar.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.overview_settings_open -> {
                                    store.dispatch (Action.OpenSettings (main))
                                    true
                                }
                                else -> false
                            }
                        }

                        val lparams = toolbar.layoutParams as AppBarLayout.LayoutParams
                            lparams.scrollFlags = lparams.scrollFlags or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

                        main.setTheme (R.style.AppTheme_AppBarOverlay)
                    }

                    title (store.state.setting.account)
                    titleTextColor (Color.WHITE)

                    popupTheme (R.style.AppTheme_PopupOverlay)
                }
            }

            relativeLayout {
                layoutParams (scrolling)

                swipeRefreshLayout {
                refreshing (store.state.overview.synchronizing)
                onRefresh { store.dispatch (Action.SynchronizeAccount ()) }

                gridView {
                    size (FILL, FILL)
                    adapter (tiles)
                    numColumns (2)
                    horizontalSpacing (dip (0))
                    verticalSpacing   (dip (0))
                    }
                }
            }
        }
    }

    private class RxRenderableAdapter<T> (private val view : (value : T, position : Int) -> Unit) : RenderableAdapter (), Observer<Collection<T>> {

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
