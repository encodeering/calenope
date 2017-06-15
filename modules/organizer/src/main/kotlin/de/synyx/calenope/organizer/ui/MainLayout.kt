package de.synyx.calenope.organizer.ui

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.LinearLayout
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.OpenSettings
import de.synyx.calenope.organizer.OpenWeekview
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.SelectCalendar
import de.synyx.calenope.organizer.SynchronizeAccount
import rx.Observer
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.init
import trikita.anvil.DSL.CENTER
import trikita.anvil.DSL.MATCH
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.centerHorizontal
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.gravity
import trikita.anvil.DSL.layoutParams
import trikita.anvil.DSL.margin
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.sip
import trikita.anvil.DSL.size
import trikita.anvil.DSL.text
import trikita.anvil.DSL.textColor
import trikita.anvil.DSL.textSize
import trikita.anvil.DSL.textView
import trikita.anvil.RenderableRecyclerViewAdapter
import trikita.anvil.RenderableView
import trikita.anvil.appcompat.v7.AppCompatv7DSL.popupTheme
import trikita.anvil.appcompat.v7.AppCompatv7DSL.title
import trikita.anvil.appcompat.v7.AppCompatv7DSL.titleTextColor
import trikita.anvil.appcompat.v7.AppCompatv7DSL.toolbar
import trikita.anvil.cardview.v7.CardViewv7DSL.cardView
import trikita.anvil.design.DesignDSL.appBarLayout
import trikita.anvil.design.DesignDSL.coordinatorLayout
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.adapter
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.gridLayoutManager
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.itemAnimator
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.layoutManager
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.recyclerView
import trikita.anvil.support.v4.Supportv4DSL.onRefresh
import trikita.anvil.support.v4.Supportv4DSL.refreshing
import trikita.anvil.support.v4.Supportv4DSL.swipeRefreshLayout

/**
 * @author clausen - clausen@synyx.de
 */

class MainLayout (private val main : Main) : RenderableView (main), AutoCloseable {

    private val store by lazy { Application.store }

    private val scrolling by lazy {
        val params = CoordinatorLayout.LayoutParams (MATCH, MATCH)
            params.behavior = AppBarLayout.ScrollingViewBehavior ()
            params
    }

    private val subscription : Runnable

    init {
        subscription = store.subscribe {
            tiles.onNext (store.state.overview.calendars)
        }
    }

    override fun close () {
        subscription.run ()
    }

    override fun view () {
        overview ()
    }

    private val tiles : RxRenderableAdapter<String> by lazy {
        RxRenderableAdapter<String> { item, position ->
            cardView {
                size (MATCH, MATCH)

                gravity (CENTER)

                margin (dip (5), dip (5))

                textView {
                    size (MATCH, MATCH)
                    text (item)
                    textSize (sip (10.toFloat()))
                    textColor (Color.BLACK)
                    centerHorizontal ()
                    margin (dip (20))
                }

                onClick {
                    run {
                        store.dispatcher.dispatch (SelectCalendar (item as String? ?: ""))
                        store.dispatcher.dispatch (OpenWeekview (main))
                    }
                }
            }
        }
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
                                    store.dispatcher.dispatch (OpenSettings (main))
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

            swipeRefreshLayout {
                layoutParams (scrolling)

                refreshing (store.state.overview.synchronizing)
                onRefresh { store.dispatcher.dispatch (SynchronizeAccount ()) }

                recyclerView {
                    init {
                        layoutManager (LinearLayoutManager (context, LinearLayoutManager.VERTICAL, false))
                        itemAnimator  (DefaultItemAnimator ())
                        gridLayoutManager (2)
                        adapter       (tiles)
                    }
                }
            }
        }
    }

    private class RxRenderableAdapter<T : Comparable<T>> (private val view : (value : T, position : Int) -> Unit) : RenderableRecyclerViewAdapter (), Observer<Collection<T>> {

        private var last : Collection<T> = emptyList ()

        override fun view (holder : RecyclerView.ViewHolder) {
            val                   position = holder.layoutPosition
            view (last.elementAt (position), position)
        }

        override fun getItemCount () : Int = last.size

        override fun onNext (t : Collection<T>) {
            last = t.sorted ()
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
