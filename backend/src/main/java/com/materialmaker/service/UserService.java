package com.materialmaker.service;

import com.materialmaker.exception.BadRequestException;
import com.materialmaker.mapper.AppUserMapper;
import com.materialmaker.model.AppUser;
import org.springframework.stereotype.Service;

/**
 * 轻量用户身份：前端首次生成 id 经 X-User-Id 请求头传递，这里幂等登记用户并返回规范化 id。
 */
@Service
public class UserService {

    private final AppUserMapper appUserMapper;

    public UserService(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    public String ensureUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("缺少用户身份标识请求头 X-User-Id");
        }
        String id = userId.trim();
        appUserMapper.insert(AppUser.builder().id(id).build());
        return id;
    }
}