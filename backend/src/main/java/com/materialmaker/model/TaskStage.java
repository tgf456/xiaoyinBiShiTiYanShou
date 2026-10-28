package com.materialmaker.model;

/**
 * 后台多步骤执行阶段：素材检查 -> 模拟生成 -> 结果整理。
 */
public enum TaskStage {
    VALIDATING,
    GENERATING,
    FINALIZING
}