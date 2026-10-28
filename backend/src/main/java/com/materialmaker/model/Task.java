package com.materialmaker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String userId;
    private String templateId;
    private TaskStatus status;
    private TaskStage currentStage;
    private TaskStage failStage;
    private String failReason;
    private String resultUrl;
    /** JSON 数组字符串：["assetId1","assetId2"] */
    private String assetIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}