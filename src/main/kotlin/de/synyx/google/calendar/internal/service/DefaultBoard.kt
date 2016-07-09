package de.synyx.google.calendar.internal.service

import com.google.api.services.admin.directory.model.CalendarResource
import com.google.api.services.calendar.model.CalendarListEntry
import de.synyx.google.calendar.api.model.Calendar
import de.synyx.google.calendar.api.service.Board
import de.synyx.google.calendar.api.service.Query
import de.synyx.google.calendar.internal.GoogleApi
import de.synyx.google.calendar.internal.model.DefaultCalendar
import java.util.*

/**
 * @author clausen - clausen@synyx.de
 */
class DefaultBoard constructor (api: GoogleApi, private val room: (CalendarResource) -> Boolean) : Board {

    private val calendar  : com.google.api.services.calendar.Calendar
    private val directory : com.google.api.services.admin.directory.Directory

    init {
        calendar  = api.calendar  ()
        directory = api.directory ()
    }

    override fun all () : Collection<Calendar> {
        return byCalendar () + byDirectory ()
    }

    override fun room (name: String) : Calendar? {
        return byDirectory ().filter { c -> c.id () == name }.firstOrNull ()
    }

    protected fun byDirectory () : Collection<Calendar> {
        val list = directory.resources ().calendars ().list ("my_customer")

        val resources = ArrayList<CalendarResource> ()

        var token: String? = null

        do {
            val calendars = list.setPageToken (token).execute ()

            resources += calendars.items

                 token = calendars.nextPageToken
        } while (token != null)

        return resources.filter (room).map { DefaultCalendar (id = it.resourceName, query = query (it.resourceEmail)) }
    }

    protected fun byCalendar (): Collection<Calendar> {
        val list = calendar.calendarList ().list ()

        val resources = ArrayList<CalendarListEntry> ()

        var token: String? = null

        do {
            val calendars = list.setPageToken (token).execute ()

            resources += calendars.items

                 token = calendars.nextPageToken
        } while (token != null)

        return resources.map { DefaultCalendar (id = it.id, query = query (it.id)) }
    }

    private fun query (name: String): Query = DefaultQuery { calendar.events ().list (name) }

}
