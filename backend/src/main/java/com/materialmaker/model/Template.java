package com.materialmaker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {
    private String id;
    private String title;
    private String category;
    private String coverUrl;
    private String description;
    private Integer requiredImageCount;
    /** 逗号分隔的 MIME 类型，如 "image/jpeg,image/png" */
    private String acceptedMimeTypes;
    private Long maxFileSizeBytes;
}