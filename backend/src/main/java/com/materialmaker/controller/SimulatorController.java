package com.materialmaker.controller;

import com.materialmaker.dto.SimulatorConfigDto;
import com.materialmaker.dto.SimulatorUpdateRequest;
import com.materialmaker.exception.BadRequestException;
import com.materialmaker.model.TaskStage;
import com.materialmaker.service.SimulatorConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开发用模拟控制：查看/修改各阶段耗时、注入指定阶段失败、重置默认值。
 */
@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    private final SimulatorConfig simulator;

    public SimulatorController(SimulatorConfig simulator) {
        this.simulator = simulator;
    }

    @GetMapping("/config")
    public SimulatorConfigDto get() {
        return snapshot();
    }

    @PutMapping("/config")
    public SimulatorConfigDto update(@RequestBody SimulatorUpdateRequest req) {
        if (req.stageDelayMs() != null) {
            for (Map.Entry<String, Long> entry : req.stageDelayMs().entrySet()) {
                simulator.setStageDelay(parseStage(entry.getKey()), entry.getValue() == null ? 0 : entry.getValue());
            }
        }
        // PUT 为整体替换语义：failStage 为 null 表示清除失败注入
        simulator.setFailStage(req.failStage() == null ? null : parseStage(req.failStage()));
        return snapshot();
    }

    @PostMapping("/reset")
    public SimulatorConfigDto reset() {
        simulator.reset();
        return snapshot();
    }

    private TaskStage parseStage(String name) {
        try {
            return TaskStage.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("未知阶段: " + name);
        }
    }

    private SimulatorConfigDto snapshot() {
        Map<String, Long> delays = new LinkedHashMap<>();
        for (TaskStage stage : TaskStage.values()) {
            delays.put(stage.name(), simulator.getDelay(stage));
        }
        String failStage = simulator.getFailStage() == null ? null : simulator.getFailStage().name();
        return new SimulatorConfigDto(delays, failStage);
    }
}