package com.run.runsocialplatform.module.activity.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动参与记录
 */
@Data
@TableName("activity_participation")
public class ActivityParticipation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long userId;

    /** 状态：1-报名中, 2-已参加(签到), 3-已取消 */
    @TableField("status") // 避免命中全局逻辑删除字段
    private Integer state;

    /** 签到时间 */
    private LocalDateTime checkinTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

