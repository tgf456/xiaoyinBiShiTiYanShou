package com.materialmaker.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> list = MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
            return list == null ? List.of() : list;
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String toJsonList(List<String> list) {
        try {
            return MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("序列化素材标识失败", e);
        }
    }
}