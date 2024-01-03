import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleDriveImageLinks {

    private static Drive driveService;
    private static List<File> imageFiles;

    public GoogleDriveImageLinks(String credentialsPath)throws IOException, GeneralSecurityException{
        this.driveService = getDriveService(credentialsPath);
        this.imageFiles = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {

    }

    private static Drive getDriveService(String credentialsPath) throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream credentialsStream = new FileInputStream(credentialsPath);

        if (credentialsStream == null) {
            throw new FileNotFoundException("Credentials file not found: " + credentialsPath);
        }

        ServiceAccountCredentials credentials =
                (ServiceAccountCredentials) ServiceAccountCredentials.fromStream(credentialsStream)
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

        if (credentials == null) {
            throw new RuntimeException("Failed to load credentials.");
        }

        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        return new Drive.Builder(
                httpTransport,
                jsonFactory,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Your Application Name")
                .build();
    }

    static List<File> listImagesInFolder(String folderId) throws IOException {
        String query = "'" + folderId + "' in parents and mimeType contains 'image/'";
        Drive.Files.List request = driveService.files().list().setQ(query);
        List<File> result = new ArrayList<>();
        do {
            try {
                FileList files = request.execute();
                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        return result;
    }

    public static List<File> getImageFiles() {
        System.out.println(imageFiles.size());
        return imageFiles;
    }

}
