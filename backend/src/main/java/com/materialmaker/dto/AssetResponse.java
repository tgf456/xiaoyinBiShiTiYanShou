package com.materialmaker.dto;

public record AssetResponse(
        String id,
        String url,
        String originalName,
        long sizeBytes) {
}