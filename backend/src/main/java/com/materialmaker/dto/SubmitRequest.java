package com.materialmaker.dto;

import java.util.List;

public record SubmitRequest(
        String templateId,
        List<String> assetIds) {
}