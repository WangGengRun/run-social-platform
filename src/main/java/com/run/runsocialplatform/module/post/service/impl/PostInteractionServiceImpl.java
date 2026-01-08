package com.run.runsocialplatform.module.post.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.post.mapper.PostInteractionMapper;
import com.run.runsocialplatform.module.post.mapper.PostMapper;
import com.run.runsocialplatform.module.post.model.dto.CommentCreateDTO;
import com.run.runsocialplatform.module.post.model.entity.Post;
import com.run.runsocialplatform.module.post.model.entity.PostInteraction;
import com.run.runsocialplatform.module.post.model.vo.CommentVO;
import com.run.runsocialplatform.module.post.service.PostInteractionService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 动态互动服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostInteractionServiceImpl extends ServiceImpl<PostInteractionMapper, PostInteraction>
        implements PostInteractionService {

    private final PostInteractionMapper interactionMapper;
    private final PostMapper postMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 检查动态是否存在
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "动态不存在");
        }

        // 检查是否已点赞
        PostInteraction existingLike = interactionMapper.checkLike(postId, currentUserId);

        if (existingLike != null) {
            if (existingLike.getStatus() == 1) {
                throw new BusinessException(ResultCode.DATA_EXISTS, "已点赞");
            } else {
                // 恢复点赞
                existingLike.setStatus(1);
                updateById(existingLike);
            }
        } else {
            // 创建点赞记录
            PostInteraction like = new PostInteraction();
            like.setPostId(postId);
            like.setUserId(currentUserId);
            like.setType(1); // 1-点赞
            like.setStatus(1);
            save(like);
        }

        // 更新动态点赞数
        updatePostLikeCount(postId);
        log.info("点赞成功，动态ID: {}, 用户ID: {}", postId, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        PostInteraction like = interactionMapper.checkLike(postId, currentUserId);
        if (like == null || like.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "未点赞");
        }

        // 逻辑删除点赞
        like.setStatus(0);
        updateById(like);

        // 更新动态点赞数
        updatePostLikeCount(postId);
        log.info("取消点赞成功，动态ID: {}, 用户ID: {}", postId, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentCreateDTO createDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 检查动态是否存在
        Post post = postMapper.selectById(createDTO.getPostId());
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "动态不存在");
        }

        // 如果是对评论的回复，检查父评论是否存在
        if (createDTO.getParentId() != null && createDTO.getParentId() > 0) {
            PostInteraction parentComment = getById(createDTO.getParentId());
            if (parentComment == null || parentComment.getStatus() == 0 || parentComment.getType() != 2) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "父评论不存在");
            }
        }

        // 创建评论
        PostInteraction comment = new PostInteraction();
        comment.setPostId(createDTO.getPostId());
        comment.setUserId(currentUserId);
        comment.setType(2); // 2-评论
        comment.setContent(createDTO.getContent());
        comment.setParentId(createDTO.getParentId() != null ? createDTO.getParentId() : 0L);
        comment.setStatus(1);
        save(comment);

        // 更新动态评论数
        updatePostCommentCount(createDTO.getPostId());
        log.info("创建评论成功，评论ID: {}, 动态ID: {}", comment.getId(), createDTO.getPostId());
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        PostInteraction comment = getById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该评论");
        }

        // 逻辑删除
        comment.setStatus(0);
        updateById(comment);

        // 更新动态评论数
        updatePostCommentCount(comment.getPostId());
        log.info("删除评论成功，评论ID: {}", commentId);
    }

    @Override
    public List<CommentVO> getComments(Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<CommentVO> comments = interactionMapper.selectComments(postId, currentUserId);
        return comments;
    }

    @Override
    public boolean isLiked(Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        PostInteraction like = interactionMapper.checkLike(postId, currentUserId);
        return like != null && like.getStatus() == 1;
    }

    /**
     * 更新动态点赞数
     */
    private void updatePostLikeCount(Long postId) {
        Long likeCount = interactionMapper.countLikes(postId);
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(likeCount.intValue());
            postMapper.updateById(post);
        }
    }

    /**
     * 更新动态评论数
     */
    private void updatePostCommentCount(Long postId) {
        Long commentCount = interactionMapper.countComments(postId);
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount(commentCount.intValue());
            postMapper.updateById(post);
        }
    }
}

