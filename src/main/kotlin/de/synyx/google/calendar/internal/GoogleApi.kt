package de.synyx.google.calendar.internal

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.admin.directory.Directory
import com.google.api.services.calendar.Calendar
import java.io.File

/**
 * @author clausen - clausen@synyx.de
 */
class GoogleApi (

    private val transport : HttpTransport,
    private val scopes    : Collection<String>,
    private val secret    : () -> GoogleClientSecrets

) {

    private val name = "calendar"

    private val credential by lazy {
        val flow = GoogleAuthorizationCodeFlow.Builder(transport, jackson(), secret(), scopes)
                .setDataStoreFactory (FileDataStoreFactory (File (userhome (), "." + name)))
                .setAccessType ("offline")
                .build ()

        AuthorizationCodeInstalledApp (flow, LocalServerReceiver ()).authorize (username ())
    }

    fun directory () : Directory {
        return Directory.Builder(transport, jackson(), credential).setApplicationName (name).build ()
    }

    fun calendar () : Calendar {
        return Calendar.Builder(transport, jackson(), credential).setApplicationName (name).build ()
    }

    private fun username () : String {
        return System.getProperty ("user.name")
    }

    private fun userhome () : String {
        return System.getProperty ("user.home")
    }

    companion object {

        fun jackson () : JsonFactory {
            return JacksonFactory.getDefaultInstance ()
        }

    }

}
