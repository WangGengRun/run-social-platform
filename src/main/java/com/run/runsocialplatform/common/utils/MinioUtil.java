package com.run.runsocialplatform.common.utils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO 工具类，封装文件上传、删除等核心功能
 */
@Component
public class MinioUtil {

    // 注入配置的桶名称和URL过期时间
    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.urlExpire}")
    private Integer urlExpire;

    // 注入MinioClient（从配置类中获取）
    @Autowired
    private MinioClient minioClient;

    /**
     * 第一步：检查并创建桶（如果桶不存在则创建）
     */
    private void checkAndCreateBucket() throws Exception {
        // 判断桶是否存在
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            // 创建桶
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 核心功能1：上传头像（MultipartFile格式，适配Spring Boot文件上传）
     * @param file 前端上传的头像文件（MultipartFile）
     * @return 上传后的文件存储路径（用于存入数据库，后续查询和删除）
     */
    public String uploadAvatar(MultipartFile file) throws Exception {
        // 1. 先检查并创建桶
        checkAndCreateBucket();

        // 2. 处理文件名：避免重名，用UUID+原文件后缀（如：123456-avatar.jpg）
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;

        // 3. 构建头像存储路径（分类存储，方便管理，如：avatar/123456-avatar.jpg）
        String objectName = "avatar/" + newFileName;

        // 4. 获取文件输入流
        InputStream inputStream = file.getInputStream();

        // 5. 上传文件到MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName) // 存储桶名称
                        .object(objectName) // 存储在桶中的文件路径+名称
                        .stream(inputStream, file.getSize(), -1) // 文件流和文件大小
                        .contentType(file.getContentType()) // 文件类型（如image/jpeg）
                        .build()
        );

        // 6. 关闭输入流
        inputStream.close();

        // 7. 返回文件存储路径（存入数据库）
        return objectName;
    }

    /**
     * 上传活动封面（与头像相同存储策略，路径前缀 activity-cover/）
     */
    public String uploadActivityCover(MultipartFile file) throws Exception {
        checkAndCreateBucket();
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        String objectName = "activity-cover/" + newFileName;
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        inputStream.close();
        return objectName;
    }

    /**
     * 上传动态图片（路径前缀 post-image/）
     */
    public String uploadPostImage(MultipartFile file) throws Exception {
        checkAndCreateBucket();
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        String objectName = "post-image/" + newFileName;
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        inputStream.close();
        return objectName;
    }

    /**
     * 上传学生卡照片（路径前缀 student-card/）
     */
    public String uploadStudentCard(MultipartFile file) throws Exception {
        checkAndCreateBucket();
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        String objectName = "student-card/" + newFileName;
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        inputStream.close();
        return objectName;
    }

    /**
     * 核心功能2：获取头像访问URL（用于前端展示）
     * @param objectName 数据库中存储的文件路径（如：avatar/123456-avatar.jpg）
     * @return 可直接访问的临时URL（过期时间可配置，默认1小时）
     */
    public String getAvatarUrl(String objectName) throws Exception {
        // 检查文件是否存在（避免查询不存在的文件）
        minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );

        // 生成预签名URL（GET方式，用于访问文件）
        return minioClient.getPresignedObjectUrl(
                io.minio.GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .method(Method.GET)
                        .expiry(urlExpire)
                        .build()
        );
    }

    /**
     * 核心功能3：删除头像（用户更换头像或删除账号时调用）
     * @param objectName 数据库中存储的文件路径（如：avatar/123456-avatar.jpg）
     */
    public void deleteAvatar(String objectName) throws Exception {
        // 删除MinIO中的文件
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }
}