package com.materialmaker.dto;

public record ErrorResponse(
        String code,
        String message,
        String timestamp) {
}