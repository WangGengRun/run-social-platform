package com.run.runsocialplatform.module.post.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.post.model.dto.CommentCreateDTO;
import com.run.runsocialplatform.module.post.model.dto.PostCreateDTO;
import com.run.runsocialplatform.module.post.model.dto.PostUpdateDTO;
import com.run.runsocialplatform.module.post.model.vo.CommentVO;
import com.run.runsocialplatform.module.post.model.vo.PostVO;
import com.run.runsocialplatform.module.post.service.PostInteractionService;
import com.run.runsocialplatform.module.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态管理控制器
 */
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Tag(name = "动态管理", description = "动态发布与互动相关接口")
public class PostController {

    private final PostService postService;
    private final PostInteractionService interactionService;

    @PostMapping
    @Operation(summary = "发布动态")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO createDTO) {
        Long postId = postService.createPost(createDTO);
        return Result.success(postId);
    }

    @PutMapping("/{postId}")
    @Operation(summary = "更新动态")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> updatePost(@PathVariable Long postId,
                                    @Valid @RequestBody PostUpdateDTO updateDTO) {
        postService.updatePost(postId, updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "删除动态")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.success();
    }

    @GetMapping("/{postId}")
    @Operation(summary = "获取动态详情")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PostVO> getPostDetail(@PathVariable Long postId) {
        PostVO post = postService.getPostDetail(postId);
        return Result.success(post);
    }

    @GetMapping("/following")
    @Operation(summary = "获取关注的人的动态列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<PostVO>> getFollowingPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<PostVO> page = postService.getFollowingPosts(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/recommended")
    @Operation(summary = "获取推荐动态列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<PostVO>> getRecommendedPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<PostVO> page = postService.getRecommendedPosts(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门动态列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<PostVO>> getHotPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<PostVO> page = postService.getHotPosts(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取指定用户的动态列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<PostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<PostVO> page = postService.getUserPosts(userId, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的动态列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<PostVO>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<PostVO> page = postService.getMyPosts(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "点赞动态")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> likePost(@PathVariable Long postId) {
        interactionService.like(postId);
        return Result.success();
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "取消点赞")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> unlikePost(@PathVariable Long postId) {
        interactionService.unlike(postId);
        return Result.success();
    }

    @GetMapping("/{postId}/like/check")
    @Operation(summary = "检查是否已点赞")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Boolean> checkLike(@PathVariable Long postId) {
        boolean isLiked = interactionService.isLiked(postId);
        return Result.success(isLiked);
    }

    @PostMapping("/comment")
    @Operation(summary = "创建评论")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Long> createComment(@Valid @RequestBody CommentCreateDTO createDTO) {
        Long commentId = interactionService.createComment(createDTO);
        return Result.success(commentId);
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "删除评论")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        interactionService.deleteComment(commentId);
        return Result.success();
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "获取评论列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<List<CommentVO>> getComments(@PathVariable Long postId) {
        List<CommentVO> comments = interactionService.getComments(postId);
        return Result.success(comments);
    }
}

