package com.carocart.authentication.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class FirebaseStorageUtil {

	@PostConstruct
	public void init() throws IOException {
	    if (FirebaseApp.getApps().isEmpty()) {
	        String credentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
	        if (credentialsJson != null && !credentialsJson.isEmpty()) {
	            // Prefer ENV VAR (for ECS, prod)
	            ByteArrayInputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
	            FirebaseOptions options = new FirebaseOptions.Builder()
	                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
	                    .setStorageBucket("sound-memory-470316-m4.appspot.com")
	                    .build();
	            FirebaseApp.initializeApp(options);
	        } else {
	            // Fallback: File for local dev
	            try (FileInputStream serviceAccount = new FileInputStream("src/main/resources/sound-memory-470316-m4-firebase-adminsdk-fbsvc-61dda0f4de.json")) {
	                FirebaseOptions options = new FirebaseOptions.Builder()
	                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
	                        .setStorageBucket("sound-memory-470316-m4.appspot.com")
	                        .build();
	                FirebaseApp.initializeApp(options);
	            } catch (IOException e) {
	                throw new RuntimeException("Firebase credentials not found in ENV or file!", e);
	            }
	        }
	    }
	}

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = "products/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        StorageClient.getInstance().bucket().create(fileName, file.getBytes(), file.getContentType());
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                StorageClient.getInstance().bucket().getName(),
                fileName.replace("/", "%2F"));
    }
}
