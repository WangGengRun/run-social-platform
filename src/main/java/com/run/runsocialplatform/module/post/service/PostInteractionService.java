package com.run.runsocialplatform.module.post.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.run.runsocialplatform.module.post.model.dto.CommentCreateDTO;
import com.run.runsocialplatform.module.post.model.entity.PostInteraction;
import com.run.runsocialplatform.module.post.model.vo.CommentVO;

import java.util.List;

/**
 * 动态互动服务接口
 */
public interface PostInteractionService extends IService<PostInteraction> {

    /**
     * 点赞
     */
    void like(Long postId);

    /**
     * 取消点赞
     */
    void unlike(Long postId);

    /**
     * 创建评论
     */
    Long createComment(CommentCreateDTO createDTO);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId);

    /**
     * 获取评论列表
     */
    List<CommentVO> getComments(Long postId);

    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long postId);
}

