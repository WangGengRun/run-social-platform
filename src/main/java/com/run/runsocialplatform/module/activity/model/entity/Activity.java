package com.run.runsocialplatform.module.activity.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体
 */
@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动标题 */
    private String title;

    /** 活动描述 */
    private String description;

    /** 封面图 */
    private String coverImage;

    /** 活动地点（线上可填“线上”或会议链接） */
    private String location;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 组织者用户ID */
    private Long organizerId;

    /** 最大参与人数，0 表示不限制 */
    private Integer maxParticipants;

    /** 当前参与人数 */
    private Integer currentParticipants;

    /** 状态：0-待发布, 1-进行中, 2-已结束, 3-已取消 */
    @TableField("status") // 避免命中全局逻辑删除字段
    private Integer state;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

