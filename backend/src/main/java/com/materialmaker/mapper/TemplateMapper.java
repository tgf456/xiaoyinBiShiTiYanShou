package com.materialmaker.mapper;

import com.materialmaker.model.Template;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TemplateMapper {

    List<Template> findByCategory(@Param("category") String category);

    Template findById(@Param("id") String id);
}