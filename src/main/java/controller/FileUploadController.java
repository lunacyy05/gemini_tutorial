package controller;

import dto.PropertyImageDto;
import entity.Property;
import entity.PropertyImage;
import repository.PropertyImageRepository;
import repository.PropertyRepository;
import service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;

    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadService.FileUploadResult result = fileUploadService.uploadImage(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", result.getSavedFilename());
            response.put("originalFilename", result.getOriginalFilename());
            response.put("fileSize", result.getFileSize());
            response.put("contentType", result.getContentType());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/upload/image/resize")
    public ResponseEntity<?> uploadAndResizeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "width", defaultValue = "800") int maxWidth,
            @RequestParam(value = "height", defaultValue = "600") int maxHeight) {

        try {
            FileUploadService.FileUploadResult result =
                fileUploadService.uploadAndResizeImage(file, maxWidth, maxHeight);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", result.getSavedFilename());
            response.put("originalFilename", result.getOriginalFilename());
            response.put("fileSize", result.getFileSize());
            response.put("contentType", result.getContentType());
            response.put("width", result.getWidth());
            response.put("height", result.getHeight());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("이미지 리사이징 및 업로드 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미지 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/upload/property/{propertyId}/images")
    public ResponseEntity<?> uploadPropertyImages(
            @PathVariable Long propertyId,
            @RequestParam("files") List<MultipartFile> files) {

        if (!propertyRepository.existsById(propertyId)) {
            return ResponseEntity.notFound().build();
        }

        try {
            List<PropertyImageDto.Response> uploadedImages = files.stream()
                    .map(file -> {
                        try {
                            FileUploadService.FileUploadResult result =
                                fileUploadService.uploadAndResizeImage(file, 1200, 800);

                            Property property = propertyRepository.findById(propertyId).orElse(null);
                            if (property == null) return null;

                            PropertyImage propertyImage = PropertyImage.builder()
                                    .property(property)
                                    .imageUrl("/uploads/" + result.getSavedFilename())
                                    .imageOrder(0)
                                    .build();

                            PropertyImage savedImage = propertyImageRepository.save(propertyImage);
                            return PropertyImageDto.Response.fromEntity(savedImage);
                        } catch (Exception e) {
                            log.error("매물 이미지 업로드 실패: {}", file.getOriginalFilename(), e);
                            return null;
                        }
                    })
                    .filter(image -> image != null)
                    .collect(Collectors.toList());

            updateImageOrder(propertyId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("uploadedCount", uploadedImages.size());
            response.put("images", uploadedImages);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("매물 이미지 업로드 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        try {
            boolean deleted = fileUploadService.deleteFile(filename);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "파일이 삭제되었습니다." : "파일을 찾을 수 없습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "파일 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/property/{propertyId}/image/{imageId}")
    public ResponseEntity<?> deletePropertyImage(@PathVariable Long propertyId, @PathVariable Long imageId) {
        try {
            PropertyImage image = propertyImageRepository.findById(imageId)
                    .orElse(null);

            if (image == null || !image.getProperty().getId().equals(propertyId)) {
                return ResponseEntity.notFound().build();
            }

            String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
            fileUploadService.deleteFile(filename);

            propertyImageRepository.deleteById(imageId);

            updateImageOrder(propertyId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이미지가 삭제되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("매물 이미지 삭제 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미지 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void updateImageOrder(Long propertyId) {
        List<PropertyImage> images = propertyImageRepository.findByPropertyIdOrderByImageOrder(propertyId);
        for (int i = 0; i < images.size(); i++) {
            PropertyImage image = images.get(i);
            image.setImageOrder(i + 1);
            propertyImageRepository.save(image);
        }
    }
}