package com.example.crowdfund.service.document;

import com.example.crowdfund.service.aws.S3BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class ImageService {

    @Autowired
    private S3BucketService s3BucketService;

    public List<String> uploadImage(MultipartFile[] images, String identifier) throws IOException {
        return s3BucketService.uploadFile(images, identifier, "campaign");
    }

    public String uploadAvatar(MultipartFile avatar, String email) throws IOException {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        List<String> result = s3BucketService.uploadFile(new MultipartFile[]{avatar}, email, "user");
        return result.isEmpty() ? null : result.getFirst();
    }
}