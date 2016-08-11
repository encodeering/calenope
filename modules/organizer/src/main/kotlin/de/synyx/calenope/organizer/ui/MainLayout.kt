package de.synyx.calenope.organizer.ui

import android.graphics.Color
import android.widget.LinearLayout
import rx.Observer
import trikita.anvil.DSL.FILL
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.adapter
import trikita.anvil.DSL.button
import trikita.anvil.DSL.centerHorizontal
import trikita.anvil.DSL.dip
import trikita.anvil.DSL.gridView
import trikita.anvil.DSL.horizontalSpacing
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

/**
 * @author clausen - clausen@synyx.de
 */

class MainLayout (private val main : Main) : RenderableView (main) {

    override fun view () {
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

    private fun overview () {
        linearLayout {
            size (FILL, FILL)
            orientation (LinearLayout.VERTICAL)

            button {
                text ("Update")
                onClick { Application.calendars (tiles) } // TODO should be changed to a dispatch action later
            }

            gridView {
                size (FILL, FILL)
                adapter (tiles)
                numColumns (2)
                horizontalSpacing (dip (0))
                verticalSpacing   (dip (0))
                onItemClick { adapter, view, position, id -> main.onOverviewClick (adapter.getItemAtPosition(position) as String? ?: "") } // TODO should be changed to a dispatch action later
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
