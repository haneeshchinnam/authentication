package com.example.auth.controller;

import com.example.auth.dto.UploadRequest;
import com.example.auth.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("files") MultipartFile[] files, @RequestPart("data") UploadRequest uploadRequest) {
        if (files.length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select a file to upload.");
        }

        if (files.length != uploadRequest.getPageInfos().length) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide print info of all uploaded files");
        }

        try {
            fileUploadService.uploadFiles(files, uploadRequest.getPageInfos());

            return ResponseEntity
                    .ok(String.format("File '%s' of size '%d' bytes uploaded successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(" Error: " + e.getMessage());
        }
    }
}
