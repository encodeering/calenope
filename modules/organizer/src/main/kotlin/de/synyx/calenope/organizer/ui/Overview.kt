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

        grid.adapter = OverviewAdapter () { activity.layoutInflater }
    }

    override fun onCreateView (inflater : LayoutInflater?, container : ViewGroup?, savedInstanceState : Bundle?) : View {
        return inflater?.inflate (R.layout.overview, container, false)!!
    }

}

private class OverviewAdapter (private val inflater : () -> LayoutInflater) : BaseAdapter () {

    private val items = listOf (
        "Nullpointer",
        "MemoryLeak"
    )

    override fun getView (position : Int, previous : View?, parent : ViewGroup?) : View {
        val        inflater = inflater.invoke ()
        val view = inflater.inflate (R.layout.overview_tile, parent, false)

        val item = getItem (position)

        val room      = view.findViewById (R.id.overview_tile_room) as TextView
            room.text = item

        return view
    }

    override fun getItem   (position : Int) : String = items[position]

    override fun getItemId (position : Int) : Long = position.toLong ()

    override fun getCount  () : Int = items.size

}
