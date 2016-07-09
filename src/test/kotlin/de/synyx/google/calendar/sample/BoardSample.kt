package de.synyx.google.calendar.sample

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.admin.directory.DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY
import com.google.api.services.calendar.CalendarScopes.CALENDAR_READONLY
import de.synyx.google.calendar.api.service.Board
import de.synyx.google.calendar.internal.GoogleApi
import de.synyx.google.calendar.internal.service.DefaultBoard
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.Arrays.asList

/**
 * @author clausen - clausen@synyx.de
 */
object BoardSample {

    private val name = "calendar"

    private val transport by lazy { GoogleNetHttpTransport.newTrustedTransport () }

    private val scopes = asList (ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY, CALENDAR_READONLY)

    @JvmStatic fun main(args : Array<String>) {
        val board = board(args[0])

        val day = LocalDateTime.now ().plusDays (3).toDateTime ().toInstant ()

        board.all().forEach { println(it) }
        board.name ("Werkstatt")!!.query ().between (day, day.plus (Duration.standardDays(1)), TimeZone.getDefault()).forEach { println (it) }
    }

    private fun board(filename : String) : Board {
        val stream = Files.newInputStream (Paths.get (filename))

        val secret = GoogleClientSecrets.load (GoogleApi.jackson (), InputStreamReader (stream))
        val api = GoogleApi (name, transport, credential (secret))

        return DefaultBoard(api) { resource -> "Besprechungsraum" == resource.resourceType }
    }

    private fun credential (secret : GoogleClientSecrets) : Credential {
        val flow = GoogleAuthorizationCodeFlow.Builder (transport, GoogleApi.jackson (), secret, scopes)
                .setDataStoreFactory (FileDataStoreFactory (File (".", name)))
                .setAccessType ("offline")
                .build ()

        return AuthorizationCodeInstalledApp (flow, LocalServerReceiver ()).authorize ("user")
    }

}
