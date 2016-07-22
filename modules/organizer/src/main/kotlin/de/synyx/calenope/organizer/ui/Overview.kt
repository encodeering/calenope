package de.synyx.calenope.organizer.ui

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import de.synyx.calenope.organizer.R

/**
 * @author clausen - clausen@synyx.de
 */

class Overview : Fragment () {

    private val grid : GridView by lazy { view.findViewById (R.id.overview_grid) as GridView }

    override fun onActivityCreated (savedInstanceState : Bundle?) {
        super.onActivityCreated    (savedInstanceState)

        val   activity = activity

        when (activity) {
            is Interaction ->
                grid.setOnItemClickListener { parent, view, position, id ->
                    val        adapter = grid.adapter as OverviewAdapter
                    val item = adapter.getItem (position)

                    activity.onOverviewClick (item.first)
                }
        }

        grid.adapter = OverviewAdapter () { activity.layoutInflater }
    }

    override fun onCreateView (inflater : LayoutInflater?, container : ViewGroup?, savedInstanceState : Bundle?) : View {
        return inflater?.inflate (R.layout.overview, container, false)!!
    }

    interface Interaction {

        fun onOverviewClick (name : String) : Unit

    }

}

private class OverviewAdapter (private val inflater : () -> LayoutInflater) : BaseAdapter () {

    private val items = listOf (
        "Nullpointer",
        "MemoryLeak"
    )

    override fun getView (position : Int, previous : View?, parent : ViewGroup?) : View {
        val holder : ViewCache?

        when (previous) {
            null -> {
                    holder = ViewCache (parent)
                    holder.view.tag = holder
            }
            else -> holder = previous.tag as ViewCache
        }

        holder.apply {
            room.text = getItem (position)
        }

        return holder.view
    }

    override fun getItem   (position : Int) : String = items[position]

    override fun getItemId (position : Int) : Long = position.toLong ()

    override fun getCount  () : Int = items.size

    private inner class ViewCache (parent: ViewGroup?) {

        val view   : View     by lazy { inflater ().inflate (R.layout.overview_tile, parent, false) }
        val room   : TextView by lazy { view.findViewById   (R.id.overview_tile_room)   as TextView }

    }

}
