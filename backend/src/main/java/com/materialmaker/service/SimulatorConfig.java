package com.materialmaker.service;

import com.materialmaker.model.TaskStage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟生成器配置：控制各阶段耗时与指定阶段的失败注入，用于演示“处理中/多任务/失败中断”。
 */
@Component
public class SimulatorConfig {

    private final Map<TaskStage, Long> stageDelayMs = new ConcurrentHashMap<>();
    private volatile TaskStage failStage;

    public SimulatorConfig() {
        reset();
    }

    public long getDelay(TaskStage stage) {
        return stageDelayMs.getOrDefault(stage, 0L);
    }

    public TaskStage getFailStage() {
        return failStage;
    }

    public void setFailStage(TaskStage failStage) {
        this.failStage = failStage;
    }

    public void setStageDelay(TaskStage stage, long ms) {
        stageDelayMs.put(stage, Math.max(0L, ms));
    }

    public void reset() {
        stageDelayMs.put(TaskStage.VALIDATING, 2000L);
        stageDelayMs.put(TaskStage.GENERATING, 3000L);
        stageDelayMs.put(TaskStage.FINALIZING, 2000L);
        this.failStage = null;
    }
}