package com.materialmaker.service;

import com.materialmaker.dto.SubmitRequest;
import com.materialmaker.exception.BadRequestException;
import com.materialmaker.exception.NotFoundException;
import com.materialmaker.mapper.AssetMapper;
import com.materialmaker.mapper.TaskMapper;
import com.materialmaker.model.Asset;
import com.materialmaker.model.Task;
import com.materialmaker.model.TaskStatus;
import com.materialmaker.model.Template;
import com.materialmaker.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final AssetMapper assetMapper;
    private final TemplateService templateService;
    private final UserService userService;
    private final TaskProcessor taskProcessor;

    public TaskService(TaskMapper taskMapper,
                       AssetMapper assetMapper,
                       TemplateService templateService,
                       UserService userService,
                       TaskProcessor taskProcessor) {
        this.taskMapper = taskMapper;
        this.assetMapper = assetMapper;
        this.templateService = templateService;
        this.userService = userService;
        this.taskProcessor = taskProcessor;
    }

    /** 受理任务：完成校验并创建记录后立即返回，不等待制作结束。 */
    public Task submit(String userId, SubmitRequest req) {
        userService.ensureUser(userId);
        if (req == null || req.templateId() == null || req.templateId().isBlank()) {
            throw new BadRequestException("缺少模板参数 templateId");
        }

        Template template = templateService.findEntity(req.templateId());
        List<String> assetIds = req.assetIds() == null ? List.of() : req.assetIds();
        if (assetIds.size() != template.getRequiredImageCount()) {
            throw new BadRequestException("素材数量不满足要求，需要 " + template.getRequiredImageCount()
                    + " 张，实际 " + assetIds.size() + " 张");
        }

        List<String> accepted = templateService.acceptedMimeTypes(template);
        for (String assetId : assetIds) {
            if (assetId == null || assetId.isBlank()) {
                throw new BadRequestException("素材标识不合法");
            }
            Asset asset = assetMapper.findById(assetId);
            if (asset == null) {
                throw new BadRequestException("素材不存在: " + assetId);
            }
            if (!userId.equals(asset.getUserId())) {
                throw new BadRequestException("素材不属于当前用户: " + assetId);
            }
            if (!accepted.contains(asset.getMimeType())) {
                throw new BadRequestException("素材类型与模板不匹配: " + assetId);
            }
            if (asset.getSizeBytes() > template.getMaxFileSizeBytes()) {
                throw new BadRequestException("素材超过大小限制: " + assetId);
            }
        }

        Task task = Task.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .templateId(req.templateId())
                .status(TaskStatus.WAITING)
                .assetIds(JsonUtil.toJsonList(assetIds))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        taskMapper.insert(task);
        taskProcessor.enqueue(task.getId());
        return task;
    }

    public List<Task> history(String userId) {
        userService.ensureUser(userId);
        return taskMapper.findByUserId(userId);
    }

    public Task getById(String userId, String taskId) {
        Task task = taskMapper.findById(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new NotFoundException("任务不存在: " + taskId);
        }
        return task;
    }
}