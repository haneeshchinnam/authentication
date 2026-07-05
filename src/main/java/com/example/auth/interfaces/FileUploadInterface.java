package com.example.auth.interfaces;

import com.example.auth.dto.PageInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadInterface {
    void uploadFiles(MultipartFile[] multipartFiles, PageInfo[] pageInfos);
}
