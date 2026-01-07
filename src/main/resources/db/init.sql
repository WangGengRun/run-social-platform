-- 1. 用户表（核心认证表）
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT '密码',
                        `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                        `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                        `avatar` varchar(200) DEFAULT NULL COMMENT '头像URL',
                        `role` varchar(20) DEFAULT 'ALUMNI' COMMENT '角色：ADMIN-管理员, ALUMNI-校友',
                        `status` int DEFAULT '1' COMMENT '状态：0-禁用, 1-启用',
                        `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
                        `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`),
                        UNIQUE KEY `uk_email` (`email`),
                        UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 校友信息表
CREATE TABLE `alumni_info` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `user_id` bigint NOT NULL COMMENT '用户ID',
                               `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
                               `student_id` varchar(50) NOT NULL COMMENT '学号',
                               `admission_year` int NOT NULL COMMENT '入学年份',
                               `graduation_year` int DEFAULT NULL COMMENT '毕业年份',
                               `college` varchar(100) DEFAULT NULL COMMENT '学院',
                               `major` varchar(100) DEFAULT NULL COMMENT '专业',
                               `company` varchar(100) DEFAULT NULL COMMENT '公司',
                               `position` varchar(100) DEFAULT NULL COMMENT '职位',
                               `city` varchar(50) DEFAULT NULL COMMENT '城市',
                               `bio` text COMMENT '个人简介',
                               `verify_status` int DEFAULT '0' COMMENT '审核状态：0-待审核, 1-审核通过, 2-审核不通过',
                               `verify_notes` varchar(200) DEFAULT NULL COMMENT '审核备注',
                               `verify_time` datetime DEFAULT NULL COMMENT '审核时间',
                               `verify_admin_id` bigint DEFAULT NULL COMMENT '审核管理员ID',
                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_user_id` (`user_id`),
                               UNIQUE KEY `uk_student_id` (`student_id`),
                               KEY `idx_admission_year` (`admission_year`),
                               KEY `idx_college_major` (`college`, `major`),
                               CONSTRAINT `fk_alumni_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校友信息表';

-- 3. 动态表
CREATE TABLE `post` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '动态ID',
                        `user_id` bigint NOT NULL COMMENT '发布者用户ID',
                        `content` text NOT NULL COMMENT '动态内容',
                        `image_urls` varchar(500) DEFAULT NULL COMMENT '图片URL，多个用逗号分隔',
                        `visibility` int DEFAULT '0' COMMENT '可见性：0-公开, 1-仅校友, 2-仅自己',
                        `like_count` int DEFAULT '0' COMMENT '点赞数',
                        `comment_count` int DEFAULT '0' COMMENT '评论数',
                        `status` int DEFAULT '1' COMMENT '状态：0-删除, 1-正常',
                        `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_id` (`user_id`),
                        KEY `idx_created_at` (`created_at`),
                        CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- 4. 动态互动表（点赞和评论）
CREATE TABLE `post_interaction` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '互动ID',
                                    `post_id` bigint NOT NULL COMMENT '动态ID',
                                    `user_id` bigint NOT NULL COMMENT '用户ID',
                                    `type` int NOT NULL COMMENT '类型：1-点赞, 2-评论',
                                    `content` text COMMENT '评论内容，点赞时为空',
                                    `parent_id` bigint DEFAULT '0' COMMENT '父评论ID，0表示直接评论动态',
                                    `status` int DEFAULT '1' COMMENT '状态：0-删除, 1-正常',
                                    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_post_user_like` (`post_id`, `user_id`, `type`) COMMENT '防止重复点赞',
                                    KEY `idx_post_id` (`post_id`),
                                    KEY `idx_user_id` (`user_id`),
                                    KEY `idx_parent_id` (`parent_id`),
                                    CONSTRAINT `fk_interaction_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
                                    CONSTRAINT `fk_interaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态互动表';

-- 5. 关注关系表
CREATE TABLE `follow` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                          `follower_id` bigint NOT NULL COMMENT '关注者用户ID',
                          `followee_id` bigint NOT NULL COMMENT '被关注者用户ID',
                          `status` int DEFAULT '1' COMMENT '状态：0-取消, 1-关注',
                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
                          `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`),
                          KEY `idx_followee_id` (`followee_id`),
                          KEY `idx_follower_id` (`follower_id`),
                          CONSTRAINT `fk_follower_user` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                          CONSTRAINT `fk_followee_user` FOREIGN KEY (`followee_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系表';

-- 6. 私信表
CREATE TABLE `private_message` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                   `sender_id` bigint NOT NULL COMMENT '发送者ID',
                                   `receiver_id` bigint NOT NULL COMMENT '接收者ID',
                                   `content` text NOT NULL COMMENT '消息内容',
                                   `message_type` int DEFAULT '1' COMMENT '消息类型：1-文本, 2-图片, 3-文件',
                                   `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读：0-未读, 1-已读',
                                   `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
                                   `status` int DEFAULT '1' COMMENT '状态：0-删除, 1-正常',
                                   `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
                                   KEY `idx_receiver_sender` (`receiver_id`, `sender_id`),
                                   KEY `idx_created_at` (`created_at`),
                                   CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

-- 7. 活动表
CREATE TABLE `activity` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
                            `title` varchar(200) NOT NULL COMMENT '活动标题',
                            `description` text COMMENT '活动描述',
                            `cover_image` varchar(200) DEFAULT NULL COMMENT '封面图',
                            `location` varchar(200) DEFAULT NULL COMMENT '活动地点',
                            `start_time` datetime NOT NULL COMMENT '开始时间',
                            `end_time` datetime NOT NULL COMMENT '结束时间',
                            `organizer_id` bigint NOT NULL COMMENT '组织者用户ID',
                            `max_participants` int DEFAULT '0' COMMENT '最大参与人数，0表示不限制',
                            `current_participants` int DEFAULT '0' COMMENT '当前参与人数',
                            `status` int DEFAULT '0' COMMENT '状态：0-待发布, 1-进行中, 2-已结束, 3-已取消',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_organizer` (`organizer_id`),
                            KEY `idx_start_time` (`start_time`),
                            KEY `idx_status` (`status`),
                            CONSTRAINT `fk_activity_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 8. 活动参与表
CREATE TABLE `activity_participation` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与ID',
                                          `activity_id` bigint NOT NULL COMMENT '活动ID',
                                          `user_id` bigint NOT NULL COMMENT '用户ID',
                                          `status` int DEFAULT '1' COMMENT '状态：1-报名中, 2-已参加, 3-已取消',
                                          `checkin_time` datetime DEFAULT NULL COMMENT '签到时间',
                                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
                                          KEY `idx_user_id` (`user_id`),
                                          CONSTRAINT `fk_participation_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE,
                                          CONSTRAINT `fk_participation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与表';

-- 9. 系统配置表（支持扩展）
CREATE TABLE `system_config` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                 `config_key` varchar(100) NOT NULL COMMENT '配置键',
                                 `config_value` text COMMENT '配置值',
                                 `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
                                 `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 10. 操作日志表
CREATE TABLE `operation_log` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                 `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
                                 `operation` varchar(100) NOT NULL COMMENT '操作内容',
                                 `method` varchar(50) DEFAULT NULL COMMENT '请求方法',
                                 `params` text COMMENT '请求参数',
                                 `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
                                 `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
                                 `status` int DEFAULT '1' COMMENT '状态：0-失败, 1-成功',
                                 `error_message` text COMMENT '错误信息',
                                 `execute_time` bigint DEFAULT NULL COMMENT '执行时间(ms)',
                                 `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';