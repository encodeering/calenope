package de.synyx.calenope.core.google.standalone.internal.spi

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.admin.directory.DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY
import com.google.api.services.calendar.CalendarScopes.CALENDAR_READONLY
import de.synyx.calenope.core.api.service.Board
import de.synyx.calenope.core.google.GoogleApi
import de.synyx.calenope.core.google.service.GoogleBoard
import de.synyx.calenope.core.spi.BoardProvider
import java.io.File
import java.io.Reader
import java.util.*

/**
 * @author clausen - clausen@synyx.de
 */

class GoogleStandaloneBoardProvider : BoardProvider {

    private val name = "calendar"

    private val scopes = Arrays.asList (ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY, CALENDAR_READONLY)

    private val transport by lazy { GoogleNetHttpTransport.newTrustedTransport () }

    override fun create (meta : Map<String, Any>, detector : (String) -> Boolean) : Board? {
        val value =      meta["secret"]

        if (value !is Reader) return null

        val secret = GoogleClientSecrets.load (GoogleApi.jackson (), value)
        val api    = GoogleApi (name, transport, credential (secret))

        return GoogleBoard (api) { detector (it.resourceType ?: "unknown") }
    }

    private fun credential (secret : GoogleClientSecrets) : Credential {
        val flow = GoogleAuthorizationCodeFlow.Builder(transport, GoogleApi.jackson(), secret, scopes)
                .setDataStoreFactory (FileDataStoreFactory (File (".", name)))
                .setAccessType ("offline")
                .build ()

        return AuthorizationCodeInstalledApp (flow, LocalServerReceiver ()).authorize ("user")
    }

}
