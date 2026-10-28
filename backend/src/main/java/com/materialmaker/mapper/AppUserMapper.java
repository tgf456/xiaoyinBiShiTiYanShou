package com.materialmaker.mapper;

import com.materialmaker.model.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppUserMapper {

    int insert(AppUser user);

    AppUser findById(@Param("id") String id);
}