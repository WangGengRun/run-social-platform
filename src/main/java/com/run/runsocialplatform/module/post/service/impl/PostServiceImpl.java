package com.run.runsocialplatform.module.post.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.post.mapper.PostMapper;
import com.run.runsocialplatform.module.post.model.dto.PostCreateDTO;
import com.run.runsocialplatform.module.post.model.dto.PostUpdateDTO;
import com.run.runsocialplatform.module.post.model.entity.Post;
import com.run.runsocialplatform.module.post.model.vo.PostVO;
import com.run.runsocialplatform.module.post.service.PostService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 动态服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostCreateDTO createDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Post post = new Post();
        post.setUserId(currentUserId);
        post.setContent(createDTO.getContent());
        post.setVisibility(createDTO.getVisibility() != null ? createDTO.getVisibility() : 0);
        post.setStatus(1);
        post.setLikeCount(0);
        post.setCommentCount(0);

        // 处理图片URL列表（多个用逗号分隔）
        if (CollUtil.isNotEmpty(createDTO.getImageUrls())) {
            String imageUrls = String.join(",", createDTO.getImageUrls());
            post.setImageUrls(imageUrls);
        }

        save(post);
        log.info("发布动态成功，动态ID: {}, 用户ID: {}", post.getId(), currentUserId);
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long postId, PostUpdateDTO updateDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Post post = getById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "动态不存在");
        }

        // 只能修改自己的动态
        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改该动态");
        }

        post.setContent(updateDTO.getContent());
        if (updateDTO.getVisibility() != null) {
            post.setVisibility(updateDTO.getVisibility());
        }

        // 处理图片URL列表
        if (updateDTO.getImageUrls() != null) {
            if (CollUtil.isEmpty(updateDTO.getImageUrls())) {
                post.setImageUrls(null);
            } else {
                String imageUrls = String.join(",", updateDTO.getImageUrls());
                post.setImageUrls(imageUrls);
            }
        }

        updateById(post);
        log.info("更新动态成功，动态ID: {}", postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Post post = getById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "动态不存在");
        }

        // 只能删除自己的动态
        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该动态");
        }

        // 逻辑删除
        post.setStatus(0);
        updateById(post);
        log.info("删除动态成功，动态ID: {}", postId);
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        PostVO postVO = postMapper.selectPostDetail(postId, currentUserId);
        if (postVO == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "动态不存在");
        }

        // 处理图片URL列表
        if (StrUtil.isNotBlank(postVO.getImageUrls())) {
            postVO.setImageUrlList(java.util.Arrays.asList(postVO.getImageUrls().split(",")));
        }

        return postVO;
    }

    @Override
    public Page<PostVO> getFollowingPosts(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        IPage<PostVO> result = postMapper.selectFollowingPosts(page, currentUserId);
        processPostImageUrls(result.getRecords());
        return (Page<PostVO>) result;
    }

    @Override
    public Page<PostVO> getRecommendedPosts(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        IPage<PostVO> result = postMapper.selectRecommendedPosts(page, currentUserId);
        processPostImageUrls(result.getRecords());
        return (Page<PostVO>) result;
    }

    @Override
    public Page<PostVO> getHotPosts(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        IPage<PostVO> result = postMapper.selectHotPosts(page, currentUserId);
        processPostImageUrls(result.getRecords());
        return (Page<PostVO>) result;
    }

    @Override
    public Page<PostVO> getUserPosts(Long userId, Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        IPage<PostVO> result = postMapper.selectUserPosts(page, userId, currentUserId);
        processPostImageUrls(result.getRecords());
        return (Page<PostVO>) result;
    }

    @Override
    public Page<PostVO> getMyPosts(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return getUserPosts(currentUserId, pageNum, pageSize);
    }

    /**
     * 处理动态图片URL列表
     */
    private void processPostImageUrls(java.util.List<PostVO> posts) {
        if (CollUtil.isEmpty(posts)) {
            return;
        }
        posts.forEach(post -> {
            if (StrUtil.isNotBlank(post.getImageUrls())) {
                post.setImageUrlList(java.util.Arrays.asList(post.getImageUrls().split(",")));
            }
        });
    }
}

