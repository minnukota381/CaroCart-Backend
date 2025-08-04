// FileUploadController.java
package com.carocart.delivery.controller;

import com.carocart.delivery.util.FirebaseStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private FirebaseStorageUtil firebaseStorageUtil;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "folder", required = false) String folder) {
        try {
            String url = firebaseStorageUtil.uploadFile(file, folder);
            return ResponseEntity.ok().body(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }
}
