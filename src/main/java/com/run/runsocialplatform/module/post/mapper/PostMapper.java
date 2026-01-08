package com.run.runsocialplatform.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.module.post.model.entity.Post;
import com.run.runsocialplatform.module.post.model.vo.PostVO;
import org.apache.ibatis.annotations.Param;

/**
 * 动态Mapper
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询关注的人的动态列表
     */
    IPage<PostVO> selectFollowingPosts(Page<PostVO> page,
                                       @Param("userId") Long userId);

    /**
     * 查询推荐动态列表（公开的动态，按时间倒序）
     */
    IPage<PostVO> selectRecommendedPosts(Page<PostVO> page,
                                         @Param("currentUserId") Long currentUserId);

    /**
     * 查询热门动态列表（按点赞数和评论数排序）
     */
    IPage<PostVO> selectHotPosts(Page<PostVO> page,
                                 @Param("currentUserId") Long currentUserId);

    /**
     * 查询指定用户的动态列表
     */
    IPage<PostVO> selectUserPosts(Page<PostVO> page,
                                  @Param("userId") Long userId,
                                  @Param("currentUserId") Long currentUserId);

    /**
     * 查询动态详情
     */
    PostVO selectPostDetail(@Param("postId") Long postId,
                            @Param("currentUserId") Long currentUserId);
}

