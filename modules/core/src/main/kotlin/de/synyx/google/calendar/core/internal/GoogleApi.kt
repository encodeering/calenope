package de.synyx.google.calendar.core.internal

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.admin.directory.Directory
import com.google.api.services.calendar.Calendar

/**
 * @author clausen - clausen@synyx.de
 */
class GoogleApi (

    private val name       : String,
    private val transport  : HttpTransport,
    private val credential : HttpRequestInitializer

) {

    fun directory () : Directory = Directory.Builder(transport, jackson(), credential).setApplicationName (name).build ()

    fun calendar  () : Calendar = Calendar.Builder(transport, jackson(), credential).setApplicationName (name).build ()

    companion object {

        fun jackson () : JsonFactory {
            return JacksonFactory.getDefaultInstance ()
        }

    }

}
