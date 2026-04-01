package com.run.runsocialplatform.module.avatar.service.impl;


import com.run.runsocialplatform.common.utils.MinioUtil;
import com.run.runsocialplatform.module.avatar.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AvatarServiceImpl implements AvatarService {

    @Autowired
    private MinioUtil minioUtil;

    @Override
    public String uploadAvatar(MultipartFile file) throws Exception {
        return minioUtil.uploadAvatar(file);
    }

    @Override
    public String getAvatarUrl(String objectName) throws Exception {
        return minioUtil.getAvatarUrl(objectName);
    }

    @Override
    public void deleteAvatar(String objectName) throws Exception {
        minioUtil.deleteAvatar(objectName);
    }

    @Override
    public String replaceAvatar(MultipartFile newFile, String oldObjectName) throws Exception {
        // 1. 删除旧头像
        if (oldObjectName != null && !oldObjectName.isEmpty()) {
            deleteAvatar(oldObjectName);
        }
        // 2. 上传新头像
        return uploadAvatar(newFile);
    }
}
