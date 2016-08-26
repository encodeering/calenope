package de.synyx.calenope.core.google.android.internal.spi

import android.accounts.Account
import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.admin.directory.DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY
import com.google.api.services.calendar.CalendarScopes.CALENDAR_READONLY
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.google.GoogleApi
import de.synyx.calenope.core.google.service.GoogleBoard
import de.synyx.calenope.core.spi.BoardProvider
import java.util.*

/**
 * @author clausen - clausen@synyx.de
 */

class GoogleAndroidBoardProvider : BoardProvider {

    private val name = "calendar"

    private val scopes = Arrays.asList (ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY, CALENDAR_READONLY)

    private val transport by lazy { AndroidHttp.newCompatibleTransport () }

    override fun create (meta : Map<String, Any>) : Board? {
        val context =    meta["context"]
        val account =    meta["account"]

        if (context !is Context) return null
        if (account !is Account) return null

        val api = GoogleApi (name, transport, credential (context, account))

        return GoogleBoard (api)
    }

    private fun credential (context : Context, account : Account) : HttpRequestInitializer {
        return GoogleAccountCredential.usingOAuth2 (context, scopes).setBackOff (ExponentialBackOff ()).setSelectedAccount (account)
    }

}
