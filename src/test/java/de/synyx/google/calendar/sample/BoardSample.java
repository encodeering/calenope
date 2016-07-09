package de.synyx.google.calendar.sample;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.calendar.CalendarScopes;
import de.synyx.google.calendar.api.service.Board;
import de.synyx.google.calendar.internal.GoogleApi;
import de.synyx.google.calendar.internal.service.DefaultBoard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * @author clausen - clausen@synyx.de
 */
public class BoardSample {

    public static void main (String[] args) throws GeneralSecurityException, IOException {
        Board board = board (args[0]);

        board.all ().forEach (System.out::println);
        board.name ("Werkstatt").ifPresent (b -> b.query ().day (ZonedDateTime.now ()).forEach (System.out::println));
    }

    private static Board board (String filename) throws IOException, GeneralSecurityException {
        InputStream in = Files.newInputStream (Paths.get (filename));

        Collection<String> scopes = asList (
                DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY,
                CalendarScopes.CALENDAR_READONLY
        );

        GoogleClientSecrets secret = GoogleClientSecrets.load (GoogleApi.jackson (), new InputStreamReader (in));

        GoogleApi api = new GoogleApi (GoogleNetHttpTransport.newTrustedTransport (), scopes, () -> secret);

        return new DefaultBoard (api, resource -> "Besprechungsraum".equals (resource.getResourceType ()));
    }

}
