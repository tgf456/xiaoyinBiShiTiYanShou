package com.materialmaker.service;

import com.materialmaker.dto.TemplateDto;
import com.materialmaker.exception.NotFoundException;
import com.materialmaker.mapper.TemplateMapper;
import com.materialmaker.model.Template;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TemplateService {

    private final TemplateMapper templateMapper;

    public TemplateService(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public List<TemplateDto> findTemplates(String category) {
        String c = (category == null || category.isBlank()) ? null : category.trim();
        return templateMapper.findByCategory(c).stream().map(this::toDto).toList();
    }

    public TemplateDto findById(String id) {
        return toDto(findEntity(id));
    }

    /** 内部使用：返回实体，不存在则抛 404 */
    public Template findEntity(String id) {
        Template template = templateMapper.findById(id);
        if (template == null) {
            throw new NotFoundException("模板不存在: " + id);
        }
        return template;
    }

    public List<String> acceptedMimeTypes(Template template) {
        return Arrays.stream(template.getAcceptedMimeTypes().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private TemplateDto toDto(Template t) {
        return new TemplateDto(
                t.getId(),
                t.getTitle(),
                t.getCategory(),
                t.getCoverUrl(),
                t.getDescription(),
                t.getRequiredImageCount(),
                acceptedMimeTypes(t),
                t.getMaxFileSizeBytes());
    }
}