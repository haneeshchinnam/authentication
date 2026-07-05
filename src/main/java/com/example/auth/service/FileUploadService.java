package com.example.auth.service;

import com.example.auth.dto.PageInfo;
import com.example.auth.interfaces.FileUploadInterface;
import com.example.auth.model.JobItem;
import com.example.auth.model.PrintJob;
import com.example.auth.model.User;
import com.example.auth.repository.PrintJobRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class FileUploadService implements FileUploadInterface {

    private final S3Client s3Client;
    private final PrintJobRepository printJobRepository;
    private final UserRepository userRepository;

    @Value("${S3.bucket-name}")
    private String bucketName;

    public FileUploadService(S3Client s3Client, PrintJobRepository printJobRepository, UserRepository userRepository) {
        this.s3Client = s3Client;
        this.printJobRepository = printJobRepository;
        this.userRepository = userRepository;
    }

    public void uploadFile(String bucketName, String key, MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    @Override
    public void uploadFiles(MultipartFile[] multipartFiles, PageInfo[] pageInfos) {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        PrintJob printJob = PrintJob.builder()
                .user(user)
                .paymentStatus("PENDING")
                .overallStatus("PENDING")
                .totalPriceCents(0)
                .items(new ArrayList<>())
                .build();

        int totalPrice = 0;

        for (int i = 0; i < multipartFiles.length; i++) {
            MultipartFile file = multipartFiles[i];
            PageInfo pageInfo = pageInfos[i];
            String key = "uploads/" + file.getOriginalFilename(); // Example key structure

            try {
                uploadFile(bucketName, key, file);
                
                JobItem jobItem = JobItem.builder()
                        .job(printJob)
                        .filePath(key)
                        .printType(pageInfo.getPageType())
                        .unitPriceCents(pageInfo.getPrice())
                        .pageCount(1)
                        .build();

                printJob.getItems().add(jobItem);
                totalPrice += pageInfo.getPrice();
                
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception as needed (e.g., log it, rethrow it, etc.)
            }
        }

        printJob.setTotalPriceCents(totalPrice);
        printJobRepository.save(printJob);
    }
}
