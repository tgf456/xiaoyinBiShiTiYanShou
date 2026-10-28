package com.materialmaker.service;

import com.materialmaker.mapper.AssetMapper;
import com.materialmaker.model.Asset;
import com.materialmaker.model.Task;
import com.materialmaker.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 结果整理：用第一张素材复制生成“制作结果”文件，返回可预览地址（模拟真实生成）。
 */
@Service
public class ResultService {

    private final AssetMapper assetMapper;
    private final StorageService storageService;

    public ResultService(AssetMapper assetMapper, StorageService storageService) {
        this.assetMapper = assetMapper;
        this.storageService = storageService;
    }

    public String produce(Task task) {
        List<String> ids = JsonUtil.parseStringList(task.getAssetIds());
        if (ids.isEmpty()) {
            throw new IllegalStateException("任务未关联任何素材");
        }
        Asset first = assetMapper.findById(ids.get(0));
        if (first == null) {
            throw new IllegalStateException("素材不存在: " + ids.get(0));
        }
        Path src = storageService.resolveUpload(first.getStoredPath());
        if (!Files.exists(src)) {
            throw new IllegalStateException("素材文件缺失: " + first.getStoredPath());
        }

        String resultName = "task-" + task.getId() + "." + extensionOf(first.getOriginalName());
        Path dest = storageService.getResultsDir().resolve(resultName);
        try {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("生成结果文件失败", e);
        }
        return "/results/" + resultName;
    }

    private String extensionOf(String name) {
        if (name != null && name.contains(".")) {
            return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        }
        return "jpg";
    }
}