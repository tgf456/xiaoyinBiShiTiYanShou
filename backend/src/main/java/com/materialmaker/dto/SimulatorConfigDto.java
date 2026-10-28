package com.materialmaker.dto;

import java.util.Map;

/**
 * 模拟器配置快照。failStage 为 null 表示不注入失败，否则为指定失败阶段名称。
 */
public record SimulatorConfigDto(
        Map<String, Long> stageDelayMs,
        String failStage) {
}