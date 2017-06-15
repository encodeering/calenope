package de.synyx.calenope.organizer.ui

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.OpenSettings
import de.synyx.calenope.organizer.OpenWeekview
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.SelectCalendar
import de.synyx.calenope.organizer.SelectCalendarFilter
import de.synyx.calenope.organizer.State.Overview
import de.synyx.calenope.organizer.SynchronizeAccount
import de.synyx.calenope.organizer.color
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
import trikita.anvil.DSL.onLongClick
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

    companion object {

        val fallback = "Nothing selected"

    }

    private val subscription : Runnable

    init {
        subscription = store.subscribe {
            tiles.update (store.state.overview)
        }
    }

    override fun close () {
        subscription.run ()
    }

    override fun view () {
        overview ()
    }

    private class FilterCallback (val done : (Boolean) -> Unit) : ActionMode.Callback {

        var backbutton = true

        override fun onCreateActionMode (mode : ActionMode, menu : Menu) : Boolean {
            mode.title = "Filter Calendar"
            mode.menuInflater.inflate (R.menu.overview_filter, menu)

            return true
        }

        override fun onPrepareActionMode (mode : ActionMode, menu : Menu) : Boolean {
            return false
        }

        override fun onActionItemClicked (mode : ActionMode, item : MenuItem) : Boolean {
            when (item.itemId) {
                R.id.overview_settings_filter_update -> {
                    backbutton = false
                    mode.finish ()
                }
            }

            return true
        }

        override fun onDestroyActionMode (mode : ActionMode) {
            done (backbutton)
        }

    }

    private val tiles : RenderableAdapter by lazy {
        var modeback : ActionMode.Callback? = null

        RenderableAdapter { item, position ->
            cardView {
                size (MATCH, dip (64))

                gravity (CENTER)

                margin (dip (0), dip (1))

                textView {
                    size (MATCH, MATCH)
                    text (item)
                    textSize (sip (10.toFloat()))
                    textColor (if (! store.state.overview.filtering || tiles.selected (position)) color (R.color.primary_text) else color (R.color.secondary_text))
                    centerHorizontal ()
                    margin (dip (20))
                }

                onLongClick {
                    if (modeback == null)
                        modeback = FilterCallback {
                                                                                                backbutton ->
                            store.dispatcher.dispatch (SelectCalendarFilter (false, stash = if (backbutton) store.state.overview.stash else tiles.selection (false)))
                            modeback = null
                        }.apply {
                            startActionMode (this)
                            store.dispatcher.dispatch (SelectCalendarFilter (true))
                        }

                    true
                }

                if (store.state.overview.filtering) {
                    onClick {
                        tiles.toggle (position)
                    }
                }
                else {
                    if (item == fallback) onClick {}
                    else                  onClick {
                        run {
                            store.dispatcher.dispatch (SelectCalendar (item as String? ?: ""))
                            store.dispatcher.dispatch (OpenWeekview (main))
                        }
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

                        main.setTheme (R.style.AppTheme_NoActionBar)
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
                        gridLayoutManager (1)
                        adapter       (tiles)
                    }
                }
            }
        }
    }

    private class RenderableAdapter (private val view : (value : String, position : Int) -> Unit) : RenderableRecyclerViewAdapter () {

        private var filtering = false
        private var synchronizing = false

        private var last = emptyList<String> ()
        private var selections = mutableMapOf<Int, Boolean> ()

        private val visibles : Collection<String>
            get () = (if (filtering) last else selection (true)).run { if (isEmpty () && ! synchronizing) listOf (fallback) else this }

        override fun view (holder : RecyclerView.ViewHolder) {
            val                       position = holder.layoutPosition
            view (visibles.elementAt (position), position)
        }

        override fun getItemCount () : Int = visibles.size

        fun selection (eq : Boolean)   = selections.filter { (_, v) -> v == eq }.keys.map (last::elementAt)

        fun selected  (position : Int) = selections[position] ?: true

        fun toggle    (position : Int) {
            selections[position] = ! selected (position)
            notifyDataSetChanged ()
        }

        fun update (overview : Overview) {
            last = overview.calendars.sorted ()
            selections = last.mapIndexed { idx, name -> idx to ! overview.stash.contains (name) }.toMap (mutableMapOf ())
            filtering  = overview.filtering
            synchronizing = overview.synchronizing
            notifyDataSetChanged ()
        }

    }

}
