package com.materialmaker.dto;

import java.util.Map;

/**
 * 更新模拟器配置的请求体。
 */
public record SimulatorUpdateRequest(
        Map<String, Long> stageDelayMs,
        String failStage) {
}