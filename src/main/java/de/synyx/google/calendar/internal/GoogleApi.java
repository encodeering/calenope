package de.synyx.google.calendar.internal;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.calendar.Calendar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author clausen - clausen@synyx.de
 */
public final class GoogleApi {

    private final String name = "calendar";

    private final Collection<String> scopes;

    private final HttpTransport transport;
    private final Supplier<GoogleClientSecrets> secret;

    public GoogleApi (HttpTransport transport, Collection<String> scopes, Supplier<GoogleClientSecrets> secret) {
        this.transport = Objects.requireNonNull (transport);
        this.secret    = Objects.requireNonNull (secret);

        this.scopes    = new ArrayList<> (Objects.requireNonNull (scopes));
    }

    public final Directory directory () throws IOException {
        return new Directory.Builder (transport, jackson (), credential ())
                            .setApplicationName (name)
                                .build ();
    }

    public final Calendar calendar () throws IOException {
        return new Calendar.Builder (transport, jackson (), credential ())
                           .setApplicationName (name)
                               .build ();
    }

    public final Credential credential () throws IOException {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder (transport, jackson (), secret.get (), scopes)
                        .setDataStoreFactory (storage (userhome ()))
                        .setAccessType ("offline")
                            .build ();

        return new AuthorizationCodeInstalledApp (flow, new LocalServerReceiver ()).authorize (username ());
    }

    private FileDataStoreFactory storage (String   path) throws IOException {
        return new FileDataStoreFactory (new File (path, "." + name));
    }

    private String username () {
        return System.getProperty ("user.name");
    }

    private String userhome () {
        return System.getProperty ("user.home");
    }

    public final static JsonFactory jackson () {
        return JacksonFactory.getDefaultInstance ();
    }

}
