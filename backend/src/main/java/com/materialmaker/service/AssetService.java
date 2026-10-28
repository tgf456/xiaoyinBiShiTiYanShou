package com.materialmaker.service;

import com.materialmaker.dto.AssetResponse;
import com.materialmaker.exception.BadRequestException;
import com.materialmaker.mapper.AssetMapper;
import com.materialmaker.model.Asset;
import com.materialmaker.model.Template;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AssetService {

    private final AssetMapper assetMapper;
    private final TemplateService templateService;
    private final StorageService storageService;
    private final UserService userService;

    public AssetService(AssetMapper assetMapper,
                        TemplateService templateService,
                        StorageService storageService,
                        UserService userService) {
        this.assetMapper = assetMapper;
        this.templateService = templateService;
        this.storageService = storageService;
        this.userService = userService;
    }

    public AssetResponse upload(String userId, MultipartFile file, String templateId) {
        userService.ensureUser(userId);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("未接收到图片文件");
        }

        Template template = templateService.findEntity(templateId);
        List<String> accepted = templateService.acceptedMimeTypes(template);

        String contentType = file.getContentType();
        if (contentType == null || !accepted.contains(contentType)) {
            throw new BadRequestException("图片类型不支持，仅支持: " + String.join(", ", accepted));
        }
        if (file.getSize() > template.getMaxFileSizeBytes()) {
            throw new BadRequestException("图片超过大小限制（最大 " + template.getMaxFileSizeBytes() + " 字节）");
        }

        String id = UUID.randomUUID().toString().replace("-", "");
        String fileName = id + "." + extensionFor(contentType);
        Path target = storageService.getUploadsDir().resolve(fileName);
        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new RuntimeException("保存素材失败", e);
        }

        Asset asset = Asset.builder()
                .id(id)
                .userId(userId)
                .originalName(file.getOriginalFilename())
                .storedPath("uploads/" + fileName)
                .mimeType(contentType)
                .sizeBytes(file.getSize())
                .createdAt(LocalDateTime.now())
                .build();
        assetMapper.insert(asset);

        return new AssetResponse(id, "/" + asset.getStoredPath(), file.getOriginalFilename(), file.getSize());
    }

    private String extensionFor(String contentType) {
        if ("image/png".equals(contentType)) {
            return "png";
        }
        return "jpg";
    }
}