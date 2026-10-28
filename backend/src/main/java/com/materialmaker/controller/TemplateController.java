package com.materialmaker.controller;

import com.materialmaker.dto.TemplateDto;
import com.materialmaker.service.TemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<TemplateDto> list(@RequestParam(value = "category", required = false) String category) {
        return templateService.findTemplates(category);
    }

    @GetMapping("/{id}")
    public TemplateDto detail(@PathVariable String id) {
        return templateService.findById(id);
    }
}