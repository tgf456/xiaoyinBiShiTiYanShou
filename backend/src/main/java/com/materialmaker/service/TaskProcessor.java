package com.materialmaker.service;

import com.materialmaker.mapper.AssetMapper;
import com.materialmaker.mapper.TaskMapper;
import com.materialmaker.model.Task;
import com.materialmaker.model.TaskStage;
import com.materialmaker.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 多步骤异步执行引擎：素材检查 -> 模拟生成 -> 结果整理。
 * 每一步都持久化 current_stage，失败即停止后续步骤并落库 fail_stage / fail_reason。
 */
@Component
public class TaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(TaskProcessor.class);

    private final TaskMapper taskMapper;
    private final AssetMapper assetMapper;
    private final SimulatorConfig simulator;
    private final ResultService resultService;
    private final ThreadPoolTaskExecutor executor;

    public TaskProcessor(TaskMapper taskMapper,
                         AssetMapper assetMapper,
                         SimulatorConfig simulator,
                         ResultService resultService,
                         @Qualifier("taskExecutor") ThreadPoolTaskExecutor executor) {
        this.taskMapper = taskMapper;
        this.assetMapper = assetMapper;
        this.simulator = simulator;
        this.resultService = resultService;
        this.executor = executor;
    }

    public void enqueue(String taskId) {
        executor.execute(() -> {
            try {
                process(taskId);
            } catch (Exception e) {
                log.error("任务后台处理出现未预期异常: {}", taskId, e);
                taskMapper.markFailed(taskId, null, "后台处理异常: " + e.getMessage());
            }
        });
    }

    /** 同步执行三步，便于单元测试直接验证步骤顺序与失败中断。 */
    public void process(String taskId) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            return;
        }

        if (!executeStage(taskId, TaskStage.VALIDATING,
                "素材检查阶段被配置为模拟失败", () -> checkAssets(task))) {
            return;
        }
        if (!executeStage(taskId, TaskStage.GENERATING,
                "模拟生成阶段被配置为模拟失败", () -> {
                })) {
            return;
        }
        if (!executeStage(taskId, TaskStage.FINALIZING,
                "结果整理阶段被配置为模拟失败", () -> {
                    String resultUrl = resultService.produce(task);
                    taskMapper.markSuccess(taskId, resultUrl);
                })) {
            return;
        }
    }

    @FunctionalInterface
    private interface StepAction {
        void run() throws Exception;
    }

    private boolean executeStage(String taskId, TaskStage stage, String simulatedFailReason, StepAction work) {
        taskMapper.startStage(taskId, stage.name());
        sleep(simulator.getDelay(stage));

        if (simulator.getFailStage() == stage) {
            taskMapper.markFailed(taskId, stage.name(), simulatedFailReason);
            return false;
        }

        try {
            work.run();
            return true;
        } catch (Exception e) {
            taskMapper.markFailed(taskId, stage.name(), stageLabel(stage) + "失败: " + e.getMessage());
            return false;
        }
    }

    private void checkAssets(Task task) {
        List<String> ids = JsonUtil.parseStringList(task.getAssetIds());
        if (ids.isEmpty()) {
            throw new IllegalStateException("任务未关联任何素材");
        }
        for (String id : ids) {
            if (assetMapper.findById(id) == null) {
                throw new IllegalStateException("素材不存在: " + id);
            }
        }
    }

    private void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("任务执行被中断", e);
        }
    }

    private String stageLabel(TaskStage stage) {
        return switch (stage) {
            case VALIDATING -> "素材检查";
            case GENERATING -> "模拟生成";
            case FINALIZING -> "结果整理";
        };
    }
}