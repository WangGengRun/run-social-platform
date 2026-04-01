package com.run.runsocialplatform.module.avatar.controller;


import com.run.runsocialplatform.common.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 头像管理接口，提供上传、查询、删除功能
 */
@RestController
@RequestMapping("/avatar")
@CrossOrigin // 允许跨域（前端调试用，生产环境可配置具体域名）
public class AvatarController {

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 接口1：上传头像
     * @param file 前端上传的头像文件
     * @return 上传结果（包含文件存储路径，需存入数据库）
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 校验文件（可选：限制大小、格式）
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "上传文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            long fileSize = file.getSize();
            long maxSize = 5 * 1024 * 1024; // 限制5MB以内
            if (fileSize > maxSize) {
                result.put("code", 400);
                result.put("msg", "文件大小不能超过5MB");
                return ResponseEntity.badRequest().body(result);
            }

            // 调用MinIO工具类上传文件
            String objectName = minioUtil.uploadAvatar(file);

            // 封装返回结果（objectName需要存入用户表的avatar字段）
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", objectName); // 如：avatar/123456-avatar.jpg
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 接口2：获取头像访问URL
     * @param objectName 数据库中存储的文件路径（如：avatar/123456-avatar.jpg）
     * @return 可直接访问的URL（前端用于展示头像）
     */
    @GetMapping("/getUrl")
    public ResponseEntity<Map<String, Object>> getAvatarUrl(@RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String avatarUrl = minioUtil.getAvatarUrl(objectName);
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", avatarUrl); // 可直接粘贴到浏览器访问
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 接口3：删除头像
     * @param objectName 数据库中存储的文件路径
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAvatar(@RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        try {
            minioUtil.deleteAvatar(objectName);
            result.put("code", 200);
            result.put("msg", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}