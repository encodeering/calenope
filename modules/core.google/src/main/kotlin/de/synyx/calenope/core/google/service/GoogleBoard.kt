package de.synyx.calenope.core.google.service

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.admin.directory.Directory
import com.google.api.services.admin.directory.model.CalendarResource
import com.google.api.services.calendar.model.CalendarListEntry
import de.synyx.calenope.core.api.model.Calendar
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.api.service.Query
import de.synyx.calenope.core.google.GoogleApi
import de.synyx.calenope.core.std.model.MemoryCalendar
import java.util.*

/**
 * @author clausen - clausen@synyx.de
 */
class GoogleBoard constructor (api: GoogleApi) : Board {

    private val calendar  : com.google.api.services.calendar.Calendar
    private val directory : Directory

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
            val calendars = tryremote (list.setPageToken (token)) {
                execute ()
            }

            resources += calendars?.items ?: emptyList ()

                 token = calendars?.nextPageToken ?: null
        } while (token != null)

        return resources.map { MemoryCalendar (id = it.resourceName, query = query (it.resourceEmail)) }
    }

    protected fun byCalendar (): Collection<Calendar> {
        val list = calendar.calendarList ().list ()

        val resources = ArrayList<CalendarListEntry> ()

        var token: String? = null

        do {
            val calendars = tryremote (list.setPageToken (token)) {
                execute ()
            }

            resources += calendars?.items ?: emptyList ()

                 token = calendars?.nextPageToken ?: null
        } while (token != null)

        return resources.map { MemoryCalendar (id = it.id, query = query (it.id)) }
    }

    private fun query (name: String): Query = GoogleQuery { calendar.events ().list (name) }

    private fun <T, R> tryremote (receiver : T, action : T.() -> R) : R? {
        return try {
            action (receiver)
        } catch  (e : Exception) {
            when (e) {
                is GoogleJsonResponseException -> if (e.statusCode == 404) return@tryremote null
            }

            throw e
        }
    }

}
