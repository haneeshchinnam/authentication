package com.example.auth.controller;

import com.example.auth.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Value("${S3.bucket-name}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select a file to upload.");
        }

        try {
            // Get the file and save it somewhere
            String fileName = file.getOriginalFilename();
            long size = file.getSize();

            // Note: In a real application, you'd save it to a persistent storage system
            // here.
            // For now, we simply acknowledge the file size and name received.

            fileUploadService.uploadFile(bucketName, fileName, file);

            return ResponseEntity
                    .ok(String.format("File '%s' of size '%d' bytes uploaded successfully.", fileName, size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage());
        }
    }
}
