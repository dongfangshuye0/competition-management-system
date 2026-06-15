package com.tangqilin.competition.storage;

import com.tangqilin.competition.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile multipartFile, String category) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw BusinessException.badRequest("请上传文件");
        }

        String originalName = StringUtils.cleanPath(Objects.requireNonNullElse(multipartFile.getOriginalFilename(), "file"));
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = UUID.randomUUID() + "-" + safeName;
        Path targetDir = Paths.get(uploadDir, category).toAbsolutePath().normalize();
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + category + "/" + filename;
        } catch (IOException e) {
            throw BusinessException.badRequest("文件保存失败：" + e.getMessage());
        }
    }
}
