package com.example.crowdfund.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ImageService {

    public List<String> uploadImage(MultipartFile[] images) throws IOException {
        return uploadImage(images, "file/images/");
    }

    public String uploadAvatar(MultipartFile avatar) throws IOException {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        
        List<String> result = uploadImage(new MultipartFile[]{avatar}, "file/avatars/");
        return result.isEmpty() ? null : result.get(0);
    }

    private List<String> uploadImage(MultipartFile[] images, String uploadDir) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            String originalFileName = image.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                continue;
            }

            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path imagePath = uploadPath.resolve(uniqueFileName);
            Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

            imageUrls.add(uniqueFileName);
        }

        return imageUrls;
    }


}