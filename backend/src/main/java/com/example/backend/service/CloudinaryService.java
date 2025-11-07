package com.example.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    // CREATE - Upload image
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        Map<String, Object> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "kickcard/images",
                "resource_type", "image"
            )
        );

        return uploadResult.get("secure_url").toString();
    }

    // CREATE - Upload avatar
    public String uploadAvatar(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        Map<String, Object> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "kickcard/avatars",
                "resource_type", "image",
                "transformation", ObjectUtils.asMap(
                    "width", 200,
                    "height", 200,
                    "crop", "fill"
                )
            )
        );

        return uploadResult.get("secure_url").toString();
    }

    // READ - Get image info
    public Map<String, Object> getImageInfo(String publicId) throws Exception {
        return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
    }

    // READ - List images
    public Map<String, Object> listImages() throws Exception {
        return cloudinary.api().resources(ObjectUtils.asMap(
            "type", "upload",
            "prefix", "kickcard/",
            "max_results", 50
        ));
    }

    // UPDATE - Replace image
    public String updateImage(MultipartFile file, String oldPublicId) throws IOException {
        // Xóa ảnh cũ
        deleteImage(oldPublicId);

        // Upload ảnh mới
        return uploadImage(file);
    }

    // DELETE - Delete image
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    // UTILITY - Extract public ID from URL
    public String extractPublicId(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/");
            String fileName = parts[parts.length - 1];

            // Tìm folder path
            String folderPath = "";
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    // Skip version nếu có
                    int startIndex = i + 1;
                    if (startIndex < parts.length && parts[startIndex].startsWith("v")) {
                        startIndex++;
                    }

                    // Ghép folder path
                    for (int j = startIndex; j < parts.length - 1; j++) {
                        if (!folderPath.isEmpty()) {
                            folderPath += "/";
                        }
                        folderPath += parts[j];
                    }
                    break;
                }
            }

            // Bỏ đuôi file
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = fileName.substring(0, dotIndex);
            }

            return folderPath.isEmpty() ? fileName : folderPath + "/" + fileName;
        } catch (Exception e) {
            throw new IllegalArgumentException("URL không hợp lệ: " + e.getMessage());
        }
    }

    // UTILITY - Check if image exists
    public boolean imageExists(String publicId) {
        try {
            cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
