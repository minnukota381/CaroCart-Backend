package com.carocart.authentication.util;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseAuthUtil {
    public FirebaseToken verifyIdToken(String idToken) throws Exception {
        // Reuses the FirebaseApp you already initialize in FirebaseStorageUtil
        return FirebaseAuth.getInstance().verifyIdToken(idToken, true);
    }
}
