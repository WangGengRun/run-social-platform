package com.run.runsocialplatform.module.avatar.service;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarService {
    // 上传头像
    String uploadAvatar(MultipartFile file) throws Exception;

    // 获取头像URL
    String getAvatarUrl(String objectName) throws Exception;

    // 删除头像
    void deleteAvatar(String objectName) throws Exception;

    // 更换头像（先删旧的，再传新的）
    String replaceAvatar(MultipartFile newFile, String oldObjectName) throws Exception;
}