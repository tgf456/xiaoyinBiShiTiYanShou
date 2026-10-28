package com.materialmaker.mapper;

import com.materialmaker.model.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    int insert(Task task);

    Task findById(@Param("id") String id);

    List<Task> findByUserId(@Param("userId") String userId);

    List<Task> findByStatus(@Param("status") String status);

    int startStage(@Param("id") String id, @Param("stage") String stage);

    int markFailed(@Param("id") String id, @Param("failStage") String failStage, @Param("failReason") String failReason);

    int markSuccess(@Param("id") String id, @Param("resultUrl") String resultUrl);
}