package com.materialmaker.controller;

import com.materialmaker.dto.AssetResponse;
import com.materialmaker.exception.BadRequestException;
import com.materialmaker.service.AssetService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public AssetResponse upload(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam(value = "templateId", required = false) String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new BadRequestException("缺少模板参数 templateId");
        }
        return assetService.upload(userId, file, templateId);
    }
}