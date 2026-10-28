package com.materialmaker.service;

import com.materialmaker.mapper.TaskMapper;
import com.materialmaker.model.Task;
import com.materialmaker.model.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 服务启动时的任务恢复：扫描 WAITING / PROCESSING 任务并重新入队。
 * 三阶段流水线本身幂等（阶段状态为覆盖写、结果文件 REPLACE_EXISTING 复制），
 * 因此不追求精确断点，直接整单重跑即可安全续传。
 */
@Component
public class TaskRecoveryRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TaskRecoveryRunner.class);

    private final TaskMapper taskMapper;
    private final TaskProcessor taskProcessor;

    public TaskRecoveryRunner(TaskMapper taskMapper, TaskProcessor taskProcessor) {
        this.taskMapper = taskMapper;
        this.taskProcessor = taskProcessor;
    }

    @Override
    public void run(ApplicationArguments args) {
        int recovered = 0;
        for (TaskStatus status : List.of(TaskStatus.WAITING, TaskStatus.PROCESSING)) {
            List<Task> tasks = taskMapper.findByStatus(status.name());
            for (Task task : tasks) {
                taskProcessor.enqueue(task.getId());
                recovered++;
            }
        }
        if (recovered > 0) {
            log.info("服务重启恢复：已重新入队 {} 个未完成任务", recovered);
        } else {
            log.info("服务重启恢复：没有未完成任务需要恢复");
        }
    }
}