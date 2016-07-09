package de.synyx.google.calendar.sample

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.admin.directory.DirectoryScopes
import com.google.api.services.calendar.CalendarScopes
import de.synyx.google.calendar.api.service.Board
import de.synyx.google.calendar.internal.GoogleApi
import de.synyx.google.calendar.internal.service.DefaultBoard
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.Arrays.asList

/**
 * @author clausen - clausen@synyx.de
 */
object BoardSample {

    @JvmStatic fun main(args : Array<String>) {
        val board = board(args[0])

        val day = LocalDateTime.now ().plusDays (3).toDateTime ().toInstant ()

        board.all().forEach { println(it) }
        board.name ("Werkstatt")!!.query ().between (day, day.plus (Duration.standardDays(1)), TimeZone.getDefault()).forEach { println (it) }
    }

    private fun board(filename : String) : Board {
        val stream = Files.newInputStream (Paths.get (filename))

        val scopes = asList(
                DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY,
                CalendarScopes.CALENDAR_READONLY
        )

        val secret = GoogleClientSecrets.load(GoogleApi.jackson(), InputStreamReader (stream))

        val api = GoogleApi(GoogleNetHttpTransport.newTrustedTransport(), scopes) { secret }

        return DefaultBoard(api) { resource -> "Besprechungsraum" == resource.resourceType }
    }

}
