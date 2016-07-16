package de.synyx.google.calendar.core.google.android.internal.spi

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.admin.directory.DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY
import com.google.api.services.calendar.CalendarScopes.CALENDAR_READONLY
import de.synyx.google.calendar.core.api.service.Board
import de.synyx.google.calendar.core.google.GoogleApi
import de.synyx.google.calendar.core.google.service.GoogleBoard
import de.synyx.google.calendar.core.spi.BoardProvider
import java.util.*

/**
 * @author clausen - clausen@synyx.de
 */

class GoogleAndroidBoardProvider : BoardProvider {

    private val name = "calendar"

    private val scopes = Arrays.asList (ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY, CALENDAR_READONLY)

    private val transport by lazy { AndroidHttp.newCompatibleTransport () }

    override fun create (meta : Map<String, Any>, detector : (String) -> Boolean) : Board? {
        val value =      meta["context"]

        if (value !is android.content.Context) return null

        val api = GoogleApi (name, transport, credential (value))

        return GoogleBoard (api) { detector (it.resourceType ?: "unknown") }
    }

    private fun credential (context : android.content.Context) : GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2 (context, scopes).setBackOff (ExponentialBackOff ())
    }

}
