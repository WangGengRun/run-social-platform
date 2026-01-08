package com.run.runsocialplatform.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.run.runsocialplatform.module.post.model.entity.PostInteraction;
import org.apache.ibatis.annotations.Param;

/**
 * 动态互动Mapper
 */
public interface PostInteractionMapper extends BaseMapper<PostInteraction> {

    /**
     * 检查是否已点赞
     */
    PostInteraction checkLike(@Param("postId") Long postId,
                               @Param("userId") Long userId);

    /**
     * 统计动态的点赞数
     */
    Long countLikes(@Param("postId") Long postId);

    /**
     * 统计动态的评论数
     */
    Long countComments(@Param("postId") Long postId);

    /**
     * 查询评论列表（一级评论）
     */
    java.util.List<com.run.runsocialplatform.module.post.model.vo.CommentVO> selectComments(
            @Param("postId") Long postId,
            @Param("currentUserId") Long currentUserId);
}

