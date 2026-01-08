package com.run.runsocialplatform.module.post.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.run.runsocialplatform.module.post.model.dto.PostCreateDTO;
import com.run.runsocialplatform.module.post.model.dto.PostUpdateDTO;
import com.run.runsocialplatform.module.post.model.entity.Post;
import com.run.runsocialplatform.module.post.model.vo.PostVO;

/**
 * 动态服务接口
 */
public interface PostService extends IService<Post> {

    /**
     * 发布动态
     */
    Long createPost(PostCreateDTO createDTO);

    /**
     * 更新动态
     */
    void updatePost(Long postId, PostUpdateDTO updateDTO);

    /**
     * 删除动态
     */
    void deletePost(Long postId);

    /**
     * 获取动态详情
     */
    PostVO getPostDetail(Long postId);

    /**
     * 获取关注的人的动态列表
     */
    Page<PostVO> getFollowingPosts(Integer pageNum, Integer pageSize);

    /**
     * 获取推荐动态列表
     */
    Page<PostVO> getRecommendedPosts(Integer pageNum, Integer pageSize);

    /**
     * 获取热门动态列表
     */
    Page<PostVO> getHotPosts(Integer pageNum, Integer pageSize);

    /**
     * 获取指定用户的动态列表
     */
    Page<PostVO> getUserPosts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的动态列表
     */
    Page<PostVO> getMyPosts(Integer pageNum, Integer pageSize);
}

