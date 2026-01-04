package com.run.runsocialplatform.common.utils;


import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JsonUtil {

    private JsonUtil() {
        // 工具类不需要实例化
    }

    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue);
        } catch (Exception e) {
            log.error("对象转JSON失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.error("JSON转对象失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转List
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            return JSON.parseArray(json, clazz);
        } catch (Exception e) {
            log.error("JSON转List失败", e);
            return null;
        }
    }

    /**
     * 对象转JSON字符串（Hutool实现）
     */
    public static String toJsonStr(Object obj) {
        try {
            return JSONUtil.toJsonStr(obj);
        } catch (Exception e) {
            log.error("对象转JSON字符串失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转对象（Hutool实现）
     */
    public static <T> T toBean(String jsonStr, Class<T> clazz) {
        try {
            return JSONUtil.toBean(jsonStr, clazz);
        } catch (Exception e) {
            log.error("JSON字符串转对象失败", e);
            return null;
        }
    }
}