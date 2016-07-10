package de.synyx.google.calendar.core.sample

import de.synyx.google.calendar.core.api.service.Board
import de.synyx.google.calendar.core.spi.BoardProvider
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

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

        return ServiceLoader.load (BoardProvider::class.java).map { it.create (mapOf ("secret" to InputStreamReader (stream)), { "Besprechungsraum" == it }) }.first()!!
    }

}
