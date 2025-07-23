package com.example.crowdfund.service.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class S3BucketService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.base.path}")
    private String basePath;

    public List<String> uploadFile(MultipartFile[] file, String identifier, String type) throws IOException {
        try {
            log.info("Uploading files for : {} to AWS S3 bucket....", identifier);
            String path = basePath + "/" + type + "/" + identifier + "/";
            List<String> urls = new ArrayList<>();

            for(MultipartFile currentFile : file){
               String key = path + currentFile.getOriginalFilename();
               
               PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                       .bucket(bucketName)
                       .key(key)
                       .contentType(currentFile.getContentType())
                       .build();
               
               s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                       currentFile.getInputStream(), currentFile.getSize()));
               
               GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                       .bucket(bucketName)
                       .key(key)
                       .build();
               
               urls.add(s3Client.utilities().getUrl(getUrlRequest).toString());
            }

            return urls;
        } catch (Exception e) {
            log.error("Exception occur in AWS s3 bucket upload service: {}", e.getMessage());
        }
        return null;
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }

            String key = extractKeyFromUrl(fileUrl);
            if (key != null) {
                log.info("Deleting file from S3: {}", key);
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                
                s3Client.deleteObject(deleteObjectRequest);
                log.info("Successfully deleted file: {}", key);
            }
        } catch (Exception e) {
            log.error("Exception occurred while deleting file {}: {}", fileUrl, e.getMessage());
        }
    }

    private String extractKeyFromUrl(String url) {
        try {
            log.info("Extracting key from URL: {}", url);
            log.info("Bucket name: {}", bucketName);

            if (url.contains(bucketName)) {

                String pattern = bucketName + "\\.[^/]+/";
                String[] parts = url.split(pattern);
                if (parts.length > 1) {
                    String key = parts[1].split("\\?")[0];
                    log.info("Extracted key: {}", key);
                    return java.net.URLDecoder.decode(key, "UTF-8");
                }
            }
            log.warn("Could not extract key from URL: {}", url);
            return null;
        } catch (Exception e) {
            log.error("Failed to extract key from URL: {}", url, e);
            return null;
        }
    }

}
