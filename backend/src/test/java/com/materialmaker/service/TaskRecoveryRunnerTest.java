package com.materialmaker.service;

import com.materialmaker.mapper.TaskMapper;
import com.materialmaker.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskRecoveryRunnerTest {

    private TaskMapper taskMapper;
    private TaskProcessor taskProcessor;
    private TaskRecoveryRunner runner;

    @BeforeEach
    void setUp() {
        taskMapper = mock(TaskMapper.class);
        taskProcessor = mock(TaskProcessor.class);
        runner = new TaskRecoveryRunner(taskMapper, taskProcessor);
    }

    @Test
    void reEnqueuesUnfinishedTasksOnStartup() {
        Task waiting = Task.builder().id("w1").build();
        Task processing = Task.builder().id("p1").build();
        when(taskMapper.findByStatus("WAITING")).thenReturn(List.of(waiting));
        when(taskMapper.findByStatus("PROCESSING")).thenReturn(List.of(processing));

        runner.run(null);

        ArgumentCaptor<String> ids = ArgumentCaptor.forClass(String.class);
        verify(taskProcessor, org.mockito.Mockito.times(2)).enqueue(ids.capture());
        assertThat(ids.getAllValues()).containsExactly("w1", "p1");

        // 已完成任务不触碰
        verify(taskMapper, never()).findById("done");
    }

    @Test
    void doesNothingWhenNoUnfinishedTasks() {
        when(taskMapper.findByStatus("WAITING")).thenReturn(List.of());
        when(taskMapper.findByStatus("PROCESSING")).thenReturn(List.of());

        runner.run(null);

        verify(taskProcessor, never()).enqueue(org.mockito.ArgumentMatchers.anyString());
    }
}