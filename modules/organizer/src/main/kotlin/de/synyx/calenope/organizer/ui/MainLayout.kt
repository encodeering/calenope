package de.synyx.calenope.organizer.ui

import android.graphics.Color
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import de.synyx.calenope.organizer.Application
import de.synyx.calenope.organizer.OpenSettings
import de.synyx.calenope.organizer.OpenWeekview
import de.synyx.calenope.organizer.R
import de.synyx.calenope.organizer.SelectCalendar
import de.synyx.calenope.organizer.SelectCalendarFilter
import de.synyx.calenope.organizer.State.Overview
import de.synyx.calenope.organizer.SynchronizeAccount
import de.synyx.calenope.organizer.color
import de.synyx.calenope.organizer.component.Layouts.Regular
import de.synyx.calenope.organizer.component.TextCard
import trikita.anvil.Anvil
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onLongClick
import trikita.anvil.DSL.text
import trikita.anvil.DSL.textColor
import trikita.anvil.RenderableRecyclerViewAdapter
import trikita.anvil.RenderableView
import trikita.anvil.appcompat.v7.AppCompatv7DSL.onMenuItemClick
import trikita.anvil.appcompat.v7.AppCompatv7DSL.title
import trikita.anvil.appcompat.v7.AppCompatv7DSL.titleTextColor
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.adapter
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.gridLayoutManager
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.itemAnimator
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.layoutManager
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL.recyclerView
import trikita.anvil.support.v4.Supportv4DSL.onRefresh
import trikita.anvil.support.v4.Supportv4DSL.refreshing

/**
 * @author clausen - clausen@synyx.de
 */

class MainLayout (private val main : Main) : RenderableView (main), AutoCloseable {

    private val store by lazy { Application.store }

    private val subscription : Runnable

    private var modeback : ActionMode.Callback? = null

    init {
        subscription = store.subscribe {
            tiles.update (store.state.overview)

            if (! store.state.overview.synchronizing &&
                ! store.state.overview.filtering     &&
                    tiles.itemCount == 0) filtermode ()
        }
    }

    override fun close () {
        subscription.run ()
    }

    override fun view () {
        overview.view ()
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
        RenderableAdapter { item, position ->
            TextCard (
                text = {
                    always += {
                        text (item)
                        textColor (if (! store.state.overview.filtering || tiles.selected (position)) color (R.color.primary_text) else color (R.color.secondary_text))
                    }
                },

                card = {
                    always += {
                        onLongClick {
                            filtermode ()

                            true
                        }

                        if (store.state.overview.filtering) {
                            onClick {
                                tiles.toggle (position)
                            }
                        }
                        else {
                            onClick {
                                run {
                                    store.dispatcher.dispatch (SelectCalendar (item as String? ?: ""))
                                    store.dispatcher.dispatch (OpenWeekview (main))
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    val overview by lazy {
        Regular (
            content = {
                always += {
                    refreshing (store.state.overview.synchronizing)
                    onRefresh { store.dispatcher.dispatch (SynchronizeAccount ()) }

                    recyclerView {
                        anvilonce<View> {
                            layoutManager (LinearLayoutManager (context, LinearLayoutManager.VERTICAL, false))
                            itemAnimator (DefaultItemAnimator ())
                            gridLayoutManager (1)
                            adapter (tiles)
                        }
                    }
                }
            },

            toolbar = {
                once += {
                    menu.clear ()
                    inflateMenu (R.menu.overview)

                    onMenuItemClick (Toolbar.OnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.overview_settings_open -> {
                                store.dispatcher.dispatch (OpenSettings (main))
                                true
                            }
                            else                        -> false
                        }
                    })

                    titleTextColor (Color.WHITE)
                }

                always += {
                    title (store.state.setting.account)
                }
            }
        )
    }

    private fun filtermode () {
        if (modeback == null)
            modeback = FilterCallback {                                             backbutton ->
                store.dispatcher.dispatch (SelectCalendarFilter (false, stash = if (backbutton) store.state.overview.stash else tiles.selection (false)))
                modeback = null
            }.apply {
                startActionMode (this)
                store.dispatcher.dispatch (SelectCalendarFilter (true))
            }
    }

    private class RenderableAdapter (private val view : (value : String, position : Int) -> Anvil.Renderable) : RenderableRecyclerViewAdapter () {

        private var filtering = false
        private var synchronizing = false

        private var last = emptyList<String> ()
        private var selections = mutableMapOf<Int, Boolean> ()

        private val visibles : Collection<String>
            get () = if (filtering) last else selection (true)

        override fun view (holder : RecyclerView.ViewHolder) {
            val                       position = holder.layoutPosition
            view (visibles.elementAt (position), position).view ()
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
