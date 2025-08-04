package com.carocart.delivery.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.IOException;
import java.util.UUID;

@Component
public class FirebaseStorageUtil {

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // Load service account from resources
            InputStream serviceAccount = getClass().getResourceAsStream("/carocart-d4171-firebase-adminsdk-fbsvc-6bfc483193.json");
            if (serviceAccount == null) throw new IOException("Firebase credential file not found in resources!");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("carocart-d4171.firebasestorage.app") // Correct bucket, .app → .com
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String fileName = (folder != null ? folder + "/" : "") + UUID.randomUUID() + "_" + file.getOriginalFilename();
        StorageClient.getInstance().bucket().create(fileName, file.getBytes(), file.getContentType());

        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                StorageClient.getInstance().bucket().getName(),
                fileName.replace("/", "%2F"));
    }

    // Overload: if folder not given, use "other"
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "other");
    }
}
