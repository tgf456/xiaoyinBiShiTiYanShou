package com.materialmaker.mapper;

import com.materialmaker.model.Asset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssetMapper {

    int insert(Asset asset);

    Asset findById(@Param("id") String id);

    List<Asset> findByIds(@Param("ids") List<String> ids);
}