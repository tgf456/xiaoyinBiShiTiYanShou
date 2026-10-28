package com.materialmaker.service;

import com.materialmaker.mapper.AssetMapper;
import com.materialmaker.mapper.TaskMapper;
import com.materialmaker.model.Asset;
import com.materialmaker.model.Task;
import com.materialmaker.model.TaskStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskProcessorTest {

    private TaskMapper taskMapper;
    private AssetMapper assetMapper;
    private ResultService resultService;
    private SimulatorConfig simulator;
    private TaskProcessor processor;

    @BeforeEach
    void setUp() {
        taskMapper = mock(TaskMapper.class);
        assetMapper = mock(AssetMapper.class);
        resultService = mock(ResultService.class);
        ThreadPoolTaskExecutor executor = mock(ThreadPoolTaskExecutor.class);

        simulator = new SimulatorConfig();
        for (TaskStage stage : TaskStage.values()) {
            simulator.setStageDelay(stage, 0);
        }

        processor = new TaskProcessor(taskMapper, assetMapper, simulator, resultService, executor);
    }

    @Test
    void processesStagesInOrderAndSucceeds() {
        Task task = Task.builder().id("t1").assetIds("[\"a1\"]").build();
        when(taskMapper.findById("t1")).thenReturn(task);
        when(assetMapper.findById("a1")).thenReturn(Asset.builder().id("a1").build());
        when(resultService.produce(task)).thenReturn("/results/task-t1.jpg");

        processor.process("t1");

        InOrder inOrder = inOrder(taskMapper);
        inOrder.verify(taskMapper).startStage("t1", "VALIDATING");
        inOrder.verify(taskMapper).startStage("t1", "GENERATING");
        inOrder.verify(taskMapper).startStage("t1", "FINALIZING");
        inOrder.verify(taskMapper).markSuccess("t1", "/results/task-t1.jpg");

        verify(taskMapper, never()).markFailed(anyString(), anyString(), anyString());
    }

    @Test
    void failsAtGeneratingAndStopsBeforeFinalizing() {
        simulator.setFailStage(TaskStage.GENERATING);
        Task task = Task.builder().id("t1").assetIds("[\"a1\"]").build();
        when(taskMapper.findById("t1")).thenReturn(task);
        when(assetMapper.findById("a1")).thenReturn(Asset.builder().id("a1").build());

        processor.process("t1");

        InOrder inOrder = inOrder(taskMapper);
        inOrder.verify(taskMapper).startStage("t1", "VALIDATING");
        inOrder.verify(taskMapper).startStage("t1", "GENERATING");
        inOrder.verify(taskMapper).markFailed(eq("t1"), eq("GENERATING"), contains("模拟生成"));

        verify(taskMapper, never()).startStage("t1", "FINALIZING");
        verify(taskMapper, never()).markSuccess(anyString(), anyString());
        verify(resultService, never()).produce(any());
    }

    @Test
    void failsAtValidatingAndNeverAdvances() {
        simulator.setFailStage(TaskStage.VALIDATING);
        Task task = Task.builder().id("t1").assetIds("[\"a1\"]").build();
        when(taskMapper.findById("t1")).thenReturn(task);

        processor.process("t1");

        InOrder inOrder = inOrder(taskMapper);
        inOrder.verify(taskMapper).startStage("t1", "VALIDATING");
        inOrder.verify(taskMapper).markFailed(eq("t1"), eq("VALIDATING"), contains("素材检查"));

        verify(taskMapper, never()).startStage("t1", "GENERATING");
        verify(taskMapper, never()).startStage("t1", "FINALIZING");
        verify(taskMapper, never()).markSuccess(anyString(), anyString());
    }
}