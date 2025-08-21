// src/main/java/com/carocart/authentication/config/FirebaseAdminConfig.java
package com.carocart.authentication.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

@Component
public class FirebaseAdminConfig {

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) return;
        try {
            String credentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
            GoogleCredentials creds;
            if (credentialsJson != null && !credentialsJson.isEmpty()) {
                creds = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
                );
            } else {
                creds = GoogleCredentials.fromStream(
                        new FileInputStream("src/main/resources/carocart-d4171-firebase-adminsdk-fbsvc-6bfc483193.json")
                );
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(creds)
                    .setStorageBucket("carocart-d4171.appspot.com") // ✅ correct suffix
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to init Firebase Admin SDK", e);
        }
    }
}
