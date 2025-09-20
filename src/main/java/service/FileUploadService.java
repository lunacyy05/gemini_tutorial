package service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload.path}")
    private String uploadPath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileUploadResult uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = generateUniqueFilename(fileExtension);

        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("파일 업로드 성공: {} -> {}", originalFilename, newFilename);

        return FileUploadResult.builder()
                .originalFilename(originalFilename)
                .savedFilename(newFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .success(true)
                .build();
    }

    public FileUploadResult uploadAndResizeImage(MultipartFile file, int maxWidth, int maxHeight) throws IOException {
        validateImageFile(file);

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("이미지 파일을 읽을 수 없습니다.");
        }

        BufferedImage resizedImage = resizeImage(originalImage, maxWidth, maxHeight);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = generateUniqueFilename(fileExtension);

        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(newFilename);
        File outputFile = filePath.toFile();

        String formatName = fileExtension.toLowerCase();
        if ("jpg".equals(formatName)) {
            formatName = "jpeg";
        }

        ImageIO.write(resizedImage, formatName, outputFile);

        log.info("이미지 리사이징 및 업로드 성공: {} -> {} ({}x{})",
                originalFilename, newFilename, resizedImage.getWidth(), resizedImage.getHeight());

        return FileUploadResult.builder()
                .originalFilename(originalFilename)
                .savedFilename(newFilename)
                .filePath(filePath.toString())
                .fileSize(outputFile.length())
                .contentType(file.getContentType())
                .width(resizedImage.getWidth())
                .height(resizedImage.getHeight())
                .success(true)
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 10MB를 초과할 수 없습니다.");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, png, gif, webp만 허용)");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalImage;
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        return resizedImage;
    }

    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadPath, filename);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("파일 삭제 성공: {}", filename);
            } else {
                log.warn("파일을 찾을 수 없음: {}", filename);
            }
            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filename, e);
            return false;
        }
    }

    public static class FileUploadResult {
        private String originalFilename;
        private String savedFilename;
        private String filePath;
        private long fileSize;
        private String contentType;
        private Integer width;
        private Integer height;
        private boolean success;

        public static FileUploadResultBuilder builder() {
            return new FileUploadResultBuilder();
        }

        public String getOriginalFilename() { return originalFilename; }
        public String getSavedFilename() { return savedFilename; }
        public String getFilePath() { return filePath; }
        public long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
        public Integer getWidth() { return width; }
        public Integer getHeight() { return height; }
        public boolean isSuccess() { return success; }

        public static class FileUploadResultBuilder {
            private String originalFilename;
            private String savedFilename;
            private String filePath;
            private long fileSize;
            private String contentType;
            private Integer width;
            private Integer height;
            private boolean success;

            public FileUploadResultBuilder originalFilename(String originalFilename) {
                this.originalFilename = originalFilename;
                return this;
            }

            public FileUploadResultBuilder savedFilename(String savedFilename) {
                this.savedFilename = savedFilename;
                return this;
            }

            public FileUploadResultBuilder filePath(String filePath) {
                this.filePath = filePath;
                return this;
            }

            public FileUploadResultBuilder fileSize(long fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            public FileUploadResultBuilder contentType(String contentType) {
                this.contentType = contentType;
                return this;
            }

            public FileUploadResultBuilder width(Integer width) {
                this.width = width;
                return this;
            }

            public FileUploadResultBuilder height(Integer height) {
                this.height = height;
                return this;
            }

            public FileUploadResultBuilder success(boolean success) {
                this.success = success;
                return this;
            }

            public FileUploadResult build() {
                FileUploadResult result = new FileUploadResult();
                result.originalFilename = this.originalFilename;
                result.savedFilename = this.savedFilename;
                result.filePath = this.filePath;
                result.fileSize = this.fileSize;
                result.contentType = this.contentType;
                result.width = this.width;
                result.height = this.height;
                result.success = this.success;
                return result;
            }
        }
    }
}