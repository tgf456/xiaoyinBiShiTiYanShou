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
public class Asset {
    private String id;
    private String userId;
    private String originalName;
    /** 相对存储根的路径，如 uploads/xxx.jpg */
    private String storedPath;
    private String mimeType;
    private Long sizeBytes;
    private LocalDateTime createdAt;
}